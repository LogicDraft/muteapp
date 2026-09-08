package com.logicdraftlabs.mute.core

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.quicksettings.TileService
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.notification.MuteNotificationHelper
import com.logicdraftlabs.mute.receiver.AutoRestoreReceiver
import com.logicdraftlabs.mute.data.PrefsManager.MuteSource
import com.logicdraftlabs.mute.tile.MuteTileService
import com.logicdraftlabs.mute.ui.MainActivity
import com.logicdraftlabs.mute.widget.MuteWidgetProvider

/**
 * The whole app is this one toggle. Every entry point - QS tile, widget, notification action,
 * and the (rarely opened) app screen - calls into these three functions and nothing else.
 */
object MuteController {

    /**
     * Do Not Disturb access is not a normal runtime-permission dialog; the user has to flip it
     * on in a system settings screen. Everything here is gated on this check first.
     */
    fun isDndAccessGranted(context: Context): Boolean {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return false
        return nm.isNotificationPolicyAccessGranted
    }

    fun isMuted(context: Context): Boolean = PrefsManager.isMutedByApp(context)

    /**
     * Flips the current state. Volume muting remains available without DND access; DND is an
     * optional enhancement that Android grants separately.
     * Returns the resulting muted state.
     */
    fun toggle(context: Context): Boolean {
        val app = context.applicationContext
        return if (isMuted(app)) {
            unmute(app, MuteSource.Manual)
            false
        } else {
            mute(app, MuteSource.Manual)
            true
        }
    }

    fun mute(
        context: Context,
        source: MuteSource = MuteSource.Manual,
        dndLevelOverride: PrefsManager.DndLevel? = null
    ) {
        val app = context.applicationContext
        val audioManager = app.getSystemService(AudioManager::class.java) ?: return
        val notificationManager = app.getSystemService(NotificationManager::class.java) ?: return

        // 1. Snapshot exactly what the phone was doing, so restore is exact - not a guess.
        val previousFilter = notificationManager.currentInterruptionFilter
        val wantsDnd = PrefsManager.getEnableDnd(app) && isDndAccessGranted(app)
        val targetDndLevel = dndLevelOverride ?: PrefsManager.getDndLevel(app)
        val targetFilter = when (targetDndLevel) {
            PrefsManager.DndLevel.TOTAL_SILENCE -> NotificationManager.INTERRUPTION_FILTER_NONE
            PrefsManager.DndLevel.PRIORITY_ONLY -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
        }
        val snapshot = PrefsManager.SavedAudioState(
            alarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM),
            mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
            notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION),
            ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING),
            systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM),
            ringerMode = audioManager.ringerMode,
            interruptionFilter = previousFilter,
            mutoChangedDnd = wantsDnd && previousFilter == NotificationManager.INTERRUPTION_FILTER_ALL,
            mutoDndFilter = targetFilter
        )
        PrefsManager.saveAudioState(app, snapshot)

        // 2. Zero only the streams explicitly selected by the user.
        val mutedStreams = buildList {
            if (PrefsManager.getMuteMedia(app)) add(AudioManager.STREAM_MUSIC)
            if (PrefsManager.getMuteRingtone(app)) add(AudioManager.STREAM_RING)
            if (PrefsManager.getMuteNotifications(app)) add(AudioManager.STREAM_NOTIFICATION)
            if (PrefsManager.getMuteSystem(app)) add(AudioManager.STREAM_SYSTEM)
            if (PrefsManager.getMuteAlarms(app)) add(AudioManager.STREAM_ALARM)
        }
        val streamsChanged = runCatching {
            mutedStreams.forEach { stream -> audioManager.setStreamVolume(stream, 0, 0) }
            mutedStreams.all { stream -> audioManager.getStreamVolume(stream) == 0 }
        }.getOrDefault(false)
        if (!streamsChanged && mutedStreams.isNotEmpty()) {
            restoreSnapshot(audioManager, snapshot)
            PrefsManager.clearSavedAudioState(app)
            refreshSurfaces(app)
            return
        }

        // 3. DND is never changed when it was already in the desired state or access is absent.
        if (snapshot.mutoChangedDnd) {
            runCatching { notificationManager.setInterruptionFilter(targetFilter) }
        }

        PrefsManager.setMuteSource(app, source)
        PrefsManager.setMutedByApp(app, true)
        scheduleAutoRestore(app)
        if (PrefsManager.getShowPersistentNotification(app)) {
            MuteNotificationHelper.show(app)
        }
        vibrate(app)
        refreshSurfaces(app)
    }

    fun unmute(context: Context, source: MuteSource = MuteSource.Manual) {
        val app = context.applicationContext
        val audioManager = app.getSystemService(AudioManager::class.java) ?: return
        val notificationManager = app.getSystemService(NotificationManager::class.java) ?: return
        val saved = PrefsManager.readSavedAudioState(app)

        // 1. Restore DND only when MUTO enabled it. A pre-existing DND state stays untouched.
        if (
            saved?.mutoChangedDnd == true &&
            isDndAccessGranted(app) &&
            notificationManager.currentInterruptionFilter == saved.mutoDndFilter
        ) {
            runCatching { notificationManager.setInterruptionFilter(saved.interruptionFilter) }
        }

        // 2. Then restore every stream to exactly what it was.
        if (saved != null) {
            runCatching {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, saved.alarmVolume, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, saved.mediaVolume, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, saved.notificationVolume, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_RING, saved.ringVolume, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, saved.systemVolume, 0)
                audioManager.ringerMode = saved.ringerMode
            }
        }

        PrefsManager.setMutedByApp(app, false)
        PrefsManager.clearSavedAudioState(app)
        cancelAutoRestore(app)
        MuteNotificationHelper.cancel(app)
        vibrate(app)
        refreshSurfaces(app)
    }

    // --- Auto-restore safety-net timer -------------------------------------------------------

    private fun scheduleAutoRestore(context: Context) {
        val hours = PrefsManager.getAutoRestoreHours(context)
        if (hours <= 0) {
            PrefsManager.setAutoRestoreAt(context, null)
            return
        }
        val triggerAt = System.currentTimeMillis() + hours * 60L * 60L * 1000L
        PrefsManager.setAutoRestoreAt(context, triggerAt)
        arm(context, triggerAt)
    }

    fun cancelAutoRestore(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        runCatching { alarmManager.cancel(autoRestorePendingIntent(context)) }
        PrefsManager.setAutoRestoreAt(context, null)
    }

    /** Alarms don't survive a reboot - BootReceiver calls this to re-arm from the saved target time. */
    fun rearmAutoRestoreIfNeeded(context: Context) {
        if (!isMuted(context)) return
        val at = PrefsManager.getAutoRestoreAt(context) ?: return
        if (at <= System.currentTimeMillis()) {
            unmute(context)
        } else {
            arm(context, at)
        }
    }

    private fun arm(context: Context, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        runCatching {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                autoRestorePendingIntent(context)
            )
        }
    }

    private fun autoRestorePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AutoRestoreReceiver::class.java)
            .setAction(AutoRestoreReceiver.ACTION_AUTO_RESTORE)
        return PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // --- Helpers ------------------------------------------------------------------------------

    private fun launchAppForPermission(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.EXTRA_REQUEST_PERMISSION, true)
        }
        context.startActivity(intent)
    }

    private fun vibrate(context: Context) {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        vibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun restoreSnapshot(
        audioManager: AudioManager,
        snapshot: PrefsManager.SavedAudioState
    ) {
        runCatching {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, snapshot.alarmVolume, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, snapshot.mediaVolume, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, snapshot.notificationVolume, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, snapshot.ringVolume, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, snapshot.systemVolume, 0)
            audioManager.ringerMode = snapshot.ringerMode
        }
    }

    /** Pushes the new state to every surface that shows it: widgets, the QS tile, and any open UI. */
    private fun refreshSurfaces(context: Context) {
        MuteWidgetProvider.updateAllWidgets(context)
        TileService.requestListeningState(context, ComponentName(context, MuteTileService::class.java))
        MuteStateBus.notifyChanged()
    }
}


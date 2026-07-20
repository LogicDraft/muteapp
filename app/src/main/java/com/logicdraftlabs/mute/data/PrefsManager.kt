package com.logicdraftlabs.mute.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Every piece of state the app needs survives here so it works across app kills and reboots.
 *
 * Two kinds of data live in this one file, deliberately kept apart in the key names:
 *  - SAVED_*   : a snapshot of the phone's audio/DND state taken the moment we mute it,
 *                so a restore is exact rather than a guess at "normal" levels.
 *  - SETTING_* : user preferences from the settings screen (exclude-alarm, DND level, etc).
 *
 * "Currently muted" is tracked with [isMutedByApp] as an explicit flag - never inferred from
 * volume == 0, since the user's normal volume could legitimately already be zero.
 */
object PrefsManager {

    private const val PREFS_NAME = "mute_prefs"

    private const val KEY_MUTED_BY_APP = "muted_by_app"
    private const val KEY_MUTE_SOURCE = "mute_source"
    private const val KEY_SAVED_ALARM_VOL = "saved_alarm_vol"
    private const val KEY_SAVED_MEDIA_VOL = "saved_media_vol"
    private const val KEY_SAVED_NOTIF_VOL = "saved_notif_vol"
    private const val KEY_SAVED_RING_VOL = "saved_ring_vol"
    private const val KEY_SAVED_RINGER_MODE = "saved_ringer_mode"
    private const val KEY_SAVED_INTERRUPTION_FILTER = "saved_interruption_filter"
    private const val KEY_AUTO_RESTORE_AT = "auto_restore_at_millis"

    private const val KEY_SETTING_EXCLUDE_ALARM = "setting_exclude_alarm"
    private const val KEY_SETTING_DND_LEVEL = "setting_dnd_level"
    private const val KEY_SETTING_AUTO_RESTORE_HOURS = "setting_auto_restore_hours"
    private const val KEY_SETTING_PERSISTENT_NOTIFICATION = "setting_persistent_notification"
    private const val KEY_SETTING_SCHEDULE_ENABLED = "schedule_enabled"
    private const val KEY_SETTING_SCHEDULE_START = "schedule_start_minutes"
    private const val KEY_SETTING_SCHEDULE_END = "schedule_end_minutes"

    enum class DndLevel { TOTAL_SILENCE, PRIORITY_ONLY }
    enum class MuteSource { NONE, MANUAL, SCHEDULED }

    /** Snapshot of everything we zero out, taken right before muting. */
    data class SavedAudioState(
        val alarmVolume: Int,
        val mediaVolume: Int,
        val notificationVolume: Int,
        val ringVolume: Int,
        val ringerMode: Int,
        val interruptionFilter: Int
    )

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- Muted flag -----------------------------------------------------------------------

    fun isMutedByApp(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MUTED_BY_APP, false)

    fun setMutedByApp(context: Context, muted: Boolean) {
        prefs(context).edit().putBoolean(KEY_MUTED_BY_APP, muted).apply()
        if (!muted) setMuteSource(context, MuteSource.NONE)
    }

    fun getMuteSource(context: Context): MuteSource = runCatching {
        MuteSource.valueOf(prefs(context).getString(KEY_MUTE_SOURCE, MuteSource.MANUAL.name) ?: MuteSource.MANUAL.name)
    }.getOrDefault(MuteSource.MANUAL)

    fun setMuteSource(context: Context, source: MuteSource) {
        prefs(context).edit().putString(KEY_MUTE_SOURCE, source.name).apply()
    }

    fun isScheduleEnabled(context: Context) = prefs(context).getBoolean(KEY_SETTING_SCHEDULE_ENABLED, false)
    fun setScheduleEnabled(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_SETTING_SCHEDULE_ENABLED, enabled).apply()
    fun getScheduleStartMinutes(context: Context) = prefs(context).getInt(KEY_SETTING_SCHEDULE_START, 22 * 60)
    fun setScheduleStartMinutes(context: Context, minutes: Int) = prefs(context).edit().putInt(KEY_SETTING_SCHEDULE_START, minutes).apply()
    fun getScheduleEndMinutes(context: Context) = prefs(context).getInt(KEY_SETTING_SCHEDULE_END, 7 * 60)
    fun setScheduleEndMinutes(context: Context, minutes: Int) = prefs(context).edit().putInt(KEY_SETTING_SCHEDULE_END, minutes).apply()

    // --- Saved audio snapshot ---------------------------------------------------------------

    fun saveAudioState(context: Context, state: SavedAudioState) {
        prefs(context).edit()
            .putInt(KEY_SAVED_ALARM_VOL, state.alarmVolume)
            .putInt(KEY_SAVED_MEDIA_VOL, state.mediaVolume)
            .putInt(KEY_SAVED_NOTIF_VOL, state.notificationVolume)
            .putInt(KEY_SAVED_RING_VOL, state.ringVolume)
            .putInt(KEY_SAVED_RINGER_MODE, state.ringerMode)
            .putInt(KEY_SAVED_INTERRUPTION_FILTER, state.interruptionFilter)
            .apply()
    }

    /** Returns null if nothing has ever been saved (e.g. fresh install). */
    fun readSavedAudioState(context: Context): SavedAudioState? {
        val p = prefs(context)
        if (!p.contains(KEY_SAVED_ALARM_VOL)) return null
        return SavedAudioState(
            alarmVolume = p.getInt(KEY_SAVED_ALARM_VOL, 0),
            mediaVolume = p.getInt(KEY_SAVED_MEDIA_VOL, 0),
            notificationVolume = p.getInt(KEY_SAVED_NOTIF_VOL, 0),
            ringVolume = p.getInt(KEY_SAVED_RING_VOL, 0),
            ringerMode = p.getInt(KEY_SAVED_RINGER_MODE, 0),
            interruptionFilter = p.getInt(KEY_SAVED_INTERRUPTION_FILTER, 0)
        )
    }

    // --- Auto-restore target time (absolute wall-clock millis, survives reboot) -------------

    fun setAutoRestoreAt(context: Context, atMillis: Long?) {
        val editor = prefs(context).edit()
        if (atMillis == null) editor.remove(KEY_AUTO_RESTORE_AT)
        else editor.putLong(KEY_AUTO_RESTORE_AT, atMillis)
        editor.apply()
    }

    fun getAutoRestoreAt(context: Context): Long? {
        val p = prefs(context)
        return if (p.contains(KEY_AUTO_RESTORE_AT)) p.getLong(KEY_AUTO_RESTORE_AT, 0L) else null
    }

    // --- Settings ---------------------------------------------------------------------------

    fun getExcludeAlarm(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SETTING_EXCLUDE_ALARM, false)

    fun setExcludeAlarm(context: Context, exclude: Boolean) {
        prefs(context).edit().putBoolean(KEY_SETTING_EXCLUDE_ALARM, exclude).apply()
    }

    fun getDndLevel(context: Context): DndLevel {
        val name = prefs(context).getString(KEY_SETTING_DND_LEVEL, DndLevel.TOTAL_SILENCE.name)
        return runCatching { DndLevel.valueOf(name ?: DndLevel.TOTAL_SILENCE.name) }
            .getOrDefault(DndLevel.TOTAL_SILENCE)
    }

    fun setDndLevel(context: Context, level: DndLevel) {
        prefs(context).edit().putString(KEY_SETTING_DND_LEVEL, level.name).apply()
    }

    /** 0 means "off" (no auto-restore timer). */
    fun getAutoRestoreHours(context: Context): Int =
        prefs(context).getInt(KEY_SETTING_AUTO_RESTORE_HOURS, 0)

    fun setAutoRestoreHours(context: Context, hours: Int) {
        prefs(context).edit().putInt(KEY_SETTING_AUTO_RESTORE_HOURS, hours).apply()
    }

    fun getShowPersistentNotification(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SETTING_PERSISTENT_NOTIFICATION, true)

    fun setShowPersistentNotification(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_SETTING_PERSISTENT_NOTIFICATION, show).apply()
    }
}

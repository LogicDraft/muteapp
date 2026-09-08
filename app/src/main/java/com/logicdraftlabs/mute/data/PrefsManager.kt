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
    private const val KEY_SAVED_SYSTEM_VOL = "saved_system_vol"
    private const val KEY_SAVED_MUTO_CHANGED_DND = "saved_muto_changed_dnd"
    private const val KEY_SAVED_MUTO_DND_FILTER = "saved_muto_dnd_filter"
    private const val KEY_AUTO_RESTORE_AT = "auto_restore_at_millis"

    private const val KEY_SETTING_EXCLUDE_ALARM = "setting_exclude_alarm"
    private const val KEY_SETTING_DND_LEVEL = "setting_dnd_level"
    private const val KEY_SETTING_AUTO_RESTORE_HOURS = "setting_auto_restore_hours"
    private const val KEY_SETTING_PERSISTENT_NOTIFICATION = "setting_persistent_notification"
    private const val KEY_SETTING_DYNAMIC_COLOR = "setting_dynamic_color"
    private const val KEY_SETTING_THEME = "setting_theme"
    private const val KEY_SETTING_WIDGET_SHAPE = "setting_widget_shape"
    private const val KEY_SETTING_MUTE_MEDIA = "setting_mute_media"
    private const val KEY_SETTING_MUTE_RINGTONE = "setting_mute_ringtone"
    private const val KEY_SETTING_MUTE_NOTIFICATIONS = "setting_mute_notifications"
    private const val KEY_SETTING_MUTE_SYSTEM = "setting_mute_system"
    private const val KEY_SETTING_MUTE_ALARMS = "setting_mute_alarms"
    private const val KEY_SETTING_ENABLE_DND = "setting_enable_dnd"
    private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    private const val KEY_SCHEDULES = "schedules_json"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    const val SHAPE_ADAPTIVE = "adaptive"
    const val SHAPE_CIRCLE = "circle"
    const val SHAPE_SQUIRCLE = "squircle"
    const val SHAPE_CLOVER = "clover"
    const val SHAPE_TEARDROP = "teardrop"
    const val SHAPE_FLOWER = "flower"
    const val SHAPE_STARBURST = "starburst"

    enum class DndLevel { TOTAL_SILENCE, PRIORITY_ONLY }

    sealed class MuteSource {
        object None : MuteSource()
        object Manual : MuteSource()
        data class Scheduled(val scheduleId: String) : MuteSource()
    }

    /** Snapshot of everything we zero out, taken right before muting. */
    data class SavedAudioState(
        val alarmVolume: Int,
        val mediaVolume: Int,
        val notificationVolume: Int,
        val ringVolume: Int,
        val systemVolume: Int,
        val ringerMode: Int,
        val interruptionFilter: Int,
        val mutoChangedDnd: Boolean,
        val mutoDndFilter: Int
    )

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- Muted flag -----------------------------------------------------------------------

    fun isMutedByApp(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MUTED_BY_APP, false)

    fun setMutedByApp(context: Context, muted: Boolean) {
        prefs(context).edit().putBoolean(KEY_MUTED_BY_APP, muted).apply()
        if (!muted) setMuteSource(context, MuteSource.None)
    }

    fun getMuteSource(context: Context): MuteSource {
        val saved = prefs(context).getString(KEY_MUTE_SOURCE, "MANUAL") ?: "MANUAL"
        return when {
            saved == "NONE" -> MuteSource.None
            saved.startsWith("SCHEDULED:") -> MuteSource.Scheduled(saved.removePrefix("SCHEDULED:"))
            else -> MuteSource.Manual
        }
    }

    fun setMuteSource(context: Context, source: MuteSource) {
        val str = when (source) {
            is MuteSource.None -> "NONE"
            is MuteSource.Manual -> "MANUAL"
            is MuteSource.Scheduled -> "SCHEDULED:${source.scheduleId}"
        }
        prefs(context).edit().putString(KEY_MUTE_SOURCE, str).apply()
    }

    fun getSchedules(context: Context): List<Schedule> {
        val jsonStr = prefs(context).getString(KEY_SCHEDULES, null) ?: return emptyList()
        return runCatching { kotlinx.serialization.json.Json.decodeFromString<List<Schedule>>(jsonStr) }.getOrDefault(emptyList())
    }

    fun saveSchedules(context: Context, schedules: List<Schedule>) {
        val jsonStr = kotlinx.serialization.json.Json.encodeToString(kotlinx.serialization.builtins.ListSerializer(Schedule.serializer()), schedules)
        prefs(context).edit().putString(KEY_SCHEDULES, jsonStr).apply()
    }

    // --- Saved audio snapshot ---------------------------------------------------------------

    fun saveAudioState(context: Context, state: SavedAudioState) {
        prefs(context).edit()
            .putInt(KEY_SAVED_ALARM_VOL, state.alarmVolume)
            .putInt(KEY_SAVED_MEDIA_VOL, state.mediaVolume)
            .putInt(KEY_SAVED_NOTIF_VOL, state.notificationVolume)
            .putInt(KEY_SAVED_RING_VOL, state.ringVolume)
            .putInt(KEY_SAVED_SYSTEM_VOL, state.systemVolume)
            .putInt(KEY_SAVED_RINGER_MODE, state.ringerMode)
            .putInt(KEY_SAVED_INTERRUPTION_FILTER, state.interruptionFilter)
            .putBoolean(KEY_SAVED_MUTO_CHANGED_DND, state.mutoChangedDnd)
            .putInt(KEY_SAVED_MUTO_DND_FILTER, state.mutoDndFilter)
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
            systemVolume = p.getInt(KEY_SAVED_SYSTEM_VOL, 0),
            ringerMode = p.getInt(KEY_SAVED_RINGER_MODE, 0),
            interruptionFilter = p.getInt(KEY_SAVED_INTERRUPTION_FILTER, 0),
            mutoChangedDnd = p.getBoolean(KEY_SAVED_MUTO_CHANGED_DND, false),
            mutoDndFilter = p.getInt(KEY_SAVED_MUTO_DND_FILTER, 0)
        )
    }

    fun clearSavedAudioState(context: Context) {
        prefs(context).edit()
            .remove(KEY_SAVED_ALARM_VOL)
            .remove(KEY_SAVED_MEDIA_VOL)
            .remove(KEY_SAVED_NOTIF_VOL)
            .remove(KEY_SAVED_RING_VOL)
            .remove(KEY_SAVED_SYSTEM_VOL)
            .remove(KEY_SAVED_RINGER_MODE)
            .remove(KEY_SAVED_INTERRUPTION_FILTER)
            .remove(KEY_SAVED_MUTO_CHANGED_DND)
            .remove(KEY_SAVED_MUTO_DND_FILTER)
            .apply()
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
        prefs(context).getBoolean(KEY_SETTING_EXCLUDE_ALARM, true)

    fun setExcludeAlarm(context: Context, exclude: Boolean) {
        prefs(context).edit().putBoolean(KEY_SETTING_EXCLUDE_ALARM, exclude).apply()
    }

    fun getMuteMedia(context: Context): Boolean = prefs(context).getBoolean(KEY_SETTING_MUTE_MEDIA, true)
    fun setMuteMedia(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_SETTING_MUTE_MEDIA, enabled).apply()

    fun getMuteRingtone(context: Context): Boolean = prefs(context).getBoolean(KEY_SETTING_MUTE_RINGTONE, true)
    fun setMuteRingtone(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_SETTING_MUTE_RINGTONE, enabled).apply()

    fun getMuteNotifications(context: Context): Boolean = prefs(context).getBoolean(KEY_SETTING_MUTE_NOTIFICATIONS, true)
    fun setMuteNotifications(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_SETTING_MUTE_NOTIFICATIONS, enabled).apply()

    fun getMuteSystem(context: Context): Boolean = prefs(context).getBoolean(KEY_SETTING_MUTE_SYSTEM, true)
    fun setMuteSystem(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_SETTING_MUTE_SYSTEM, enabled).apply()

    fun getMuteAlarms(context: Context): Boolean = prefs(context).getBoolean(KEY_SETTING_MUTE_ALARMS, false)
    fun setMuteAlarms(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_SETTING_MUTE_ALARMS, enabled).apply()

    fun getEnableDnd(context: Context): Boolean = prefs(context).getBoolean(KEY_SETTING_ENABLE_DND, true)
    fun setEnableDnd(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean(KEY_SETTING_ENABLE_DND, enabled).apply()

    fun isOnboardingComplete(context: Context): Boolean = prefs(context).getBoolean(KEY_ONBOARDING_COMPLETE, false)
    fun setOnboardingComplete(context: Context, complete: Boolean) = prefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply()

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
        prefs(context).getBoolean(KEY_SETTING_PERSISTENT_NOTIFICATION, false)

    fun setShowPersistentNotification(context: Context, show: Boolean) {
        prefs(context).edit().putBoolean(KEY_SETTING_PERSISTENT_NOTIFICATION, show).apply()
    }
    
    fun isDynamicColorEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SETTING_DYNAMIC_COLOR, true)

    fun setDynamicColorEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SETTING_DYNAMIC_COLOR, enabled).apply()
    }

    fun getThemePreference(context: Context): String =
        prefs(context).getString(KEY_SETTING_THEME, THEME_SYSTEM) ?: THEME_SYSTEM

    fun setThemePreference(context: Context, theme: String) {
        prefs(context).edit().putString(KEY_SETTING_THEME, theme).apply()
    }

    fun getWidgetShapePreference(context: Context): String =
        prefs(context).getString(KEY_SETTING_WIDGET_SHAPE, SHAPE_ADAPTIVE) ?: SHAPE_ADAPTIVE

    fun setWidgetShapePreference(context: Context, shape: String) {
        prefs(context).edit().putString(KEY_SETTING_WIDGET_SHAPE, shape).apply()
    }
}

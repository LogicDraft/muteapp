package com.logicdraftlabs.mute.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicdraftlabs.mute.core.MuteController
import com.logicdraftlabs.mute.core.MuteStateBus
import com.logicdraftlabs.mute.core.ScheduleManager
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.data.Schedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isDndGranted = MutableStateFlow(false)
    val isDndGranted: StateFlow<Boolean> = _isDndGranted.asStateFlow()

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    private val _dndLevel = MutableStateFlow(PrefsManager.DndLevel.TOTAL_SILENCE)
    val dndLevel: StateFlow<PrefsManager.DndLevel> = _dndLevel.asStateFlow()

    private val _autoRestoreHours = MutableStateFlow(0)
    val autoRestoreHours: StateFlow<Int> = _autoRestoreHours.asStateFlow()

    private val _autoRestoreAt = MutableStateFlow<Long?>(null)
    val autoRestoreAt: StateFlow<Long?> = _autoRestoreAt.asStateFlow()

    private val _themePreference = MutableStateFlow(PrefsManager.THEME_SYSTEM)
    val themePreference: StateFlow<String> = _themePreference.asStateFlow()

    private val _widgetShapePreference = MutableStateFlow(PrefsManager.SHAPE_ADAPTIVE)
    val widgetShapePreference: StateFlow<String> = _widgetShapePreference.asStateFlow()

    private val _dynamicColorsEnabled = MutableStateFlow(true)
    val dynamicColorsEnabled: StateFlow<Boolean> = _dynamicColorsEnabled.asStateFlow()

    private val _excludeAlarm = MutableStateFlow(true)
    val excludeAlarm: StateFlow<Boolean> = _excludeAlarm.asStateFlow()

    private val _showPersistentNotification = MutableStateFlow(false)
    val showPersistentNotification: StateFlow<Boolean> = _showPersistentNotification.asStateFlow()

    init {
        viewModelScope.launch {
            MuteStateBus.changes.collect {
                // Bus changed from external event (e.g. tile, receiver, widget)
            }
        }
    }

    fun load(context: Context) {
        val app = context.applicationContext
        _isDndGranted.value = MuteController.isDndAccessGranted(app)
        _isMuted.value = MuteController.isMuted(app)
        _schedules.value = PrefsManager.getSchedules(app)
        _dndLevel.value = PrefsManager.getDndLevel(app)
        _autoRestoreHours.value = PrefsManager.getAutoRestoreHours(app)
        _autoRestoreAt.value = PrefsManager.getAutoRestoreAt(app)
        _themePreference.value = PrefsManager.getThemePreference(app)
        _widgetShapePreference.value = PrefsManager.getWidgetShapePreference(app)
        _dynamicColorsEnabled.value = PrefsManager.isDynamicColorEnabled(app)
        _excludeAlarm.value = PrefsManager.getExcludeAlarm(app)
        _showPersistentNotification.value = PrefsManager.getShowPersistentNotification(app)
    }

    fun toggleMute(context: Context): Boolean {
        val newState = MuteController.toggle(context)
        load(context)
        return newState
    }

    fun setDndLevel(context: Context, level: PrefsManager.DndLevel) {
        PrefsManager.setDndLevel(context, level)
        _dndLevel.value = level
        if (_isMuted.value) {
            MuteController.mute(context, dndLevelOverride = level)
        }
    }

    fun setAutoRestoreHours(context: Context, hours: Int) {
        PrefsManager.setAutoRestoreHours(context, hours)
        _autoRestoreHours.value = hours
        if (_isMuted.value) {
            if (hours <= 0) {
                MuteController.cancelAutoRestore(context)
            } else {
                val triggerAt = System.currentTimeMillis() + hours * 60L * 60L * 1000L
                PrefsManager.setAutoRestoreAt(context, triggerAt)
            }
        }
        _autoRestoreAt.value = PrefsManager.getAutoRestoreAt(context)
    }

    fun setThemePreference(context: Context, pref: String) {
        PrefsManager.setThemePreference(context, pref)
        _themePreference.value = pref
    }

    fun setWidgetShapePreference(context: Context, shape: String) {
        PrefsManager.setWidgetShapePreference(context, shape)
        _widgetShapePreference.value = shape
        com.logicdraftlabs.mute.widget.MuteWidgetProvider.updateAllWidgets(context)
    }

    fun setDynamicColors(context: Context, enabled: Boolean) {
        PrefsManager.setDynamicColorEnabled(context, enabled)
        _dynamicColorsEnabled.value = enabled
    }

    fun setExcludeAlarm(context: Context, exclude: Boolean) {
        PrefsManager.setExcludeAlarm(context, exclude)
        _excludeAlarm.value = exclude
    }

    fun setShowPersistentNotification(context: Context, show: Boolean) {
        PrefsManager.setShowPersistentNotification(context, show)
        _showPersistentNotification.value = show
    }

    fun toggleSchedule(context: Context, scheduleId: String, enabled: Boolean) {
        val current = _schedules.value.toMutableList()
        val index = current.indexOfFirst { it.id == scheduleId }
        if (index != -1) {
            current[index] = current[index].copy(enabled = enabled)
            PrefsManager.saveSchedules(context, current)
            ScheduleManager.reschedule(context)
            _schedules.value = current
        }
    }

    fun saveSchedule(context: Context, schedule: Schedule) {
        val current = _schedules.value.toMutableList()
        val index = current.indexOfFirst { it.id == schedule.id }
        if (index != -1) {
            current[index] = schedule
        } else {
            current.add(schedule)
        }
        PrefsManager.saveSchedules(context, current)
        ScheduleManager.reschedule(context)
        _schedules.value = current
    }

    fun deleteSchedule(context: Context, scheduleId: String): Schedule? {
        val current = _schedules.value.toMutableList()
        val item = current.find { it.id == scheduleId }
        if (item != null) {
            current.remove(item)
            PrefsManager.saveSchedules(context, current)
            ScheduleManager.reschedule(context)
            _schedules.value = current
        }
        return item
    }

    fun restoreSchedule(context: Context, schedule: Schedule) {
        saveSchedule(context, schedule)
    }
}

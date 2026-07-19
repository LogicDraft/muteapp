package com.logicdraftlabs.mute.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * MuteController is the source of truth (backed by SharedPreferences); this is just a ping so
 * a visible MainActivity can refresh immediately instead of waiting for onResume. Widgets and
 * the QS tile don't need this - they're updated directly by MuteController.
 */
object MuteStateBus {
    private val _changes = MutableStateFlow(0L)
    val changes: StateFlow<Long> = _changes

    fun notifyChanged() {
        _changes.value = System.nanoTime()
    }
}

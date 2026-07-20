package com.logicdraftlabs.mute.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Schedule(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val days: Set<Int>, // Calendar.SUNDAY = 1, Calendar.MONDAY = 2, etc.
    val enabled: Boolean,
    val dndLevel: PrefsManager.DndLevel = PrefsManager.DndLevel.TOTAL_SILENCE
)

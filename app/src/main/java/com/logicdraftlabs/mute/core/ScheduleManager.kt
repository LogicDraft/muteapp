package com.logicdraftlabs.mute.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.data.Schedule
import com.logicdraftlabs.mute.receiver.ScheduleReceiver
import java.util.Calendar

object ScheduleManager {
    
    data class Window(val start: Long, val end: Long)

    fun reschedule(context: Context) {
        cancelAll(context)
        val schedules = PrefsManager.getSchedules(context).filter { it.enabled }
        val now = System.currentTimeMillis()

        for (schedule in schedules) {
            val windows = getNextWindows(schedule)
            val nextStart = windows.map { it.start }.filter { it > now }.minOrNull()
            val nextEnd = windows.map { it.end }.filter { it > now }.minOrNull()

            if (nextStart != null) {
                arm(context, schedule.id, ScheduleReceiver.TRIGGER_START, nextStart)
            }
            if (nextEnd != null) {
                arm(context, schedule.id, ScheduleReceiver.TRIGGER_END, nextEnd)
            }
        }
    }

    fun onAlarm(context: Context, scheduleId: String, triggerType: String) {
        val schedules = PrefsManager.getSchedules(context).filter { it.enabled }
        val schedule = schedules.find { it.id == scheduleId }

        when (triggerType) {
            ScheduleReceiver.TRIGGER_START -> {
                if (schedule != null && !MuteController.isMuted(context)) {
                    MuteController.mute(
                        context,
                        source = PrefsManager.MuteSource.Scheduled(scheduleId),
                        dndLevelOverride = schedule.dndLevel
                    )
                }
            }
            ScheduleReceiver.TRIGGER_END -> {
                // If a manual mute or another schedule is active, don't unmute.
                val currentSource = PrefsManager.getMuteSource(context)
                if (currentSource is PrefsManager.MuteSource.Scheduled && currentSource.scheduleId == scheduleId) {
                    
                    // Check if any OTHER enabled schedule window covers "now".
                    val now = System.currentTimeMillis()
                    val overlappingSchedule = schedules.find { other ->
                        other.id != scheduleId && getNextWindows(other).any { w -> now in w.start..w.end }
                    }
                    
                    if (overlappingSchedule != null) {
                        // Hand off ownership to the overlapping schedule instead of unmuting
                        PrefsManager.setMuteSource(context, PrefsManager.MuteSource.Scheduled(overlappingSchedule.id))
                        // We also need to re-apply its DND level just in case it's different
                        val filter = when (overlappingSchedule.dndLevel) {
                            PrefsManager.DndLevel.TOTAL_SILENCE -> android.app.NotificationManager.INTERRUPTION_FILTER_NONE
                            PrefsManager.DndLevel.PRIORITY_ONLY -> android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY
                        }
                        runCatching {
                            context.getSystemService(android.app.NotificationManager::class.java)
                                ?.setInterruptionFilter(filter)
                        }
                    } else {
                        MuteController.unmute(context)
                    }
                }
            }
        }
        
        // Re-arm alarms for this schedule to catch its next occurrence
        reschedule(context)
    }

    /**
     * Calculates the explicit start/end timestamp pairs for this schedule for the next 8 days.
     */
    fun getNextWindows(schedule: Schedule): List<Window> {
        if (schedule.days.isEmpty()) return emptyList()
        val windows = mutableListOf<Window>()
        
        // Check today and the next 7 days
        for (i in 0..7) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, i) }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            
            if (schedule.days.contains(dayOfWeek)) {
                val startCal = cal.clone() as Calendar
                startCal.set(Calendar.HOUR_OF_DAY, schedule.startMinuteOfDay / 60)
                startCal.set(Calendar.MINUTE, schedule.startMinuteOfDay % 60)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)
                
                val endCal = cal.clone() as Calendar
                endCal.set(Calendar.HOUR_OF_DAY, schedule.endMinuteOfDay / 60)
                endCal.set(Calendar.MINUTE, schedule.endMinuteOfDay % 60)
                endCal.set(Calendar.SECOND, 0)
                endCal.set(Calendar.MILLISECOND, 0)
                
                if (schedule.endMinuteOfDay <= schedule.startMinuteOfDay) {
                    endCal.add(Calendar.DAY_OF_YEAR, 1)
                }
                
                windows.add(Window(startCal.timeInMillis, endCal.timeInMillis))
            }
        }
        return windows
    }

    private fun arm(context: Context, scheduleId: String, triggerType: String, at: Long) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        runCatching {
            alarm.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                at,
                pendingIntent(context, scheduleId, triggerType)
            )
        }
    }

    fun cancelAll(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val schedules = PrefsManager.getSchedules(context)
        for (schedule in schedules) {
            alarm.cancel(pendingIntent(context, schedule.id, ScheduleReceiver.TRIGGER_START))
            alarm.cancel(pendingIntent(context, schedule.id, ScheduleReceiver.TRIGGER_END))
        }
    }

    private fun pendingIntent(context: Context, scheduleId: String, triggerType: String): PendingIntent {
        val requestCode = (scheduleId.hashCode() * 31) + triggerType.hashCode()
        val intent = Intent(context, ScheduleReceiver::class.java).apply {
            action = ScheduleReceiver.ACTION_SCHEDULE_ALARM
            putExtra(ScheduleReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(ScheduleReceiver.EXTRA_TRIGGER_TYPE, triggerType)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
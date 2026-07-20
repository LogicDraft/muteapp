package com.logicdraftlabs.mute.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.receiver.ScheduleReceiver

object ScheduleManager {
    fun reschedule(context: Context) {
        cancel(context)
        if (!PrefsManager.isScheduleEnabled(context)) return
        arm(context, ScheduleReceiver.ACTION_SCHEDULE_START, nextOccurrence(context, PrefsManager.getScheduleStartMinutes(context)))
        arm(context, ScheduleReceiver.ACTION_SCHEDULE_END, nextOccurrence(context, PrefsManager.getScheduleEndMinutes(context)))
    }

    fun onAlarm(context: Context, action: String) {
        if (!PrefsManager.isScheduleEnabled(context)) return
        when (action) {
            ScheduleReceiver.ACTION_SCHEDULE_START -> {
                // A schedule may only claim an unmuted phone. Manual mutes remain manual.
                if (!MuteController.isMuted(context)) MuteController.mute(context, PrefsManager.MuteSource.SCHEDULED)
                arm(context, action, nextOccurrence(context, PrefsManager.getScheduleStartMinutes(context)))
            }
            ScheduleReceiver.ACTION_SCHEDULE_END -> {
                // Never undo a manual mute or a manual override during this window.
                if (PrefsManager.getMuteSource(context) == PrefsManager.MuteSource.SCHEDULED && MuteController.isMuted(context)) {
                    MuteController.unmute(context)
                }
                arm(context, action, nextOccurrence(context, PrefsManager.getScheduleEndMinutes(context)))
            }
        }
    }

    private fun nextOccurrence(context: Context, minutes: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    private fun arm(context: Context, action: String, at: Long) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        runCatching { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent(context, action)) }
    }

    fun cancel(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        alarm.cancel(pendingIntent(context, ScheduleReceiver.ACTION_SCHEDULE_START))
        alarm.cancel(pendingIntent(context, ScheduleReceiver.ACTION_SCHEDULE_END))
    }

    private fun pendingIntent(context: Context, action: String) = PendingIntent.getBroadcast(
        context, action.hashCode(), Intent(context, ScheduleReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
package com.logicdraftlabs.mute.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.logicdraftlabs.mute.core.ScheduleManager

class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SCHEDULE_ALARM) {
            val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID) ?: return
            val triggerType = intent.getStringExtra(EXTRA_TRIGGER_TYPE) ?: return
            ScheduleManager.onAlarm(context, scheduleId, triggerType)
        }
    }
    
    companion object {
        const val ACTION_SCHEDULE_ALARM = "com.logicdraftlabs.mute.action.SCHEDULE_ALARM"
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_TRIGGER_TYPE = "extra_trigger_type"
        const val TRIGGER_START = "START"
        const val TRIGGER_END = "END"
    }
}
package com.logicdraftlabs.mute.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.logicdraftlabs.mute.core.ScheduleManager

class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SCHEDULE_START || intent.action == ACTION_SCHEDULE_END) ScheduleManager.onAlarm(context, intent.action!!)
    }
    companion object {
        const val ACTION_SCHEDULE_START = "com.logicdraftlabs.mute.action.SCHEDULE_START"
        const val ACTION_SCHEDULE_END = "com.logicdraftlabs.mute.action.SCHEDULE_END"
    }
}
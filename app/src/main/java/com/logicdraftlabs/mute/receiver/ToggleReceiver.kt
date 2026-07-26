package com.logicdraftlabs.mute.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.logicdraftlabs.mute.core.MuteController

class ToggleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MUTE -> MuteController.mute(context)
            ACTION_UNMUTE -> MuteController.unmute(context)
            ACTION_TOGGLE_MUTE -> MuteController.toggle(context)
        }
    }

    companion object {
        const val ACTION_TOGGLE_MUTE = "com.logicdraftlabs.mute.action.TOGGLE_MUTE"
        const val ACTION_MUTE = "com.logicdraftlabs.mute.action.MUTE"
        const val ACTION_UNMUTE = "com.logicdraftlabs.mute.action.UNMUTE"
    }
}

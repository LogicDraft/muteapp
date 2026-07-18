package com.gowtham.mute.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gowtham.mute.core.MuteController

class ToggleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE_MUTE) return
        MuteController.toggle(context)
    }

    companion object {
        const val ACTION_TOGGLE_MUTE = "com.gowtham.mute.action.TOGGLE_MUTE"
    }
}

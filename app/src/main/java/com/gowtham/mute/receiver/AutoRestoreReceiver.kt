package com.gowtham.mute.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gowtham.mute.core.MuteController

class AutoRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_AUTO_RESTORE) return
        // Only restore if we're still the ones holding it muted - the user may have already
        // unmuted manually, in which case this alarm is stale and should just be a no-op.
        if (MuteController.isMuted(context)) {
            MuteController.unmute(context)
        }
    }

    companion object {
        const val ACTION_AUTO_RESTORE = "com.gowtham.mute.action.AUTO_RESTORE"
    }
}

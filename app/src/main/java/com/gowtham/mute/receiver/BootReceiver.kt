package com.gowtham.mute.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gowtham.mute.core.MuteController
import com.gowtham.mute.widget.MuteWidgetProvider

/**
 * The mute/unmute state itself lives in SharedPreferences and in the phone's own audio/DND
 * settings, both of which already survive a reboot on their own. The only thing that doesn't
 * survive is a scheduled AlarmManager alarm, so that's the only thing this receiver re-arms.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        MuteController.rearmAutoRestoreIfNeeded(context)
        MuteWidgetProvider.updateAllWidgets(context)
    }
}

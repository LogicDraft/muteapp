package com.logicdraftlabs.mute

import android.app.Application
import com.logicdraftlabs.mute.notification.MuteNotificationHelper

class MuteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MuteNotificationHelper.ensureChannel(this)
    }
}

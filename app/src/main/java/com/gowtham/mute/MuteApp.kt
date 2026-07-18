package com.gowtham.mute

import android.app.Application
import com.gowtham.mute.notification.MuteNotificationHelper

class MuteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MuteNotificationHelper.ensureChannel(this)
    }
}

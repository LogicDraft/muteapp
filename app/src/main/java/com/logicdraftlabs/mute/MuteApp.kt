package com.logicdraftlabs.mute

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.logicdraftlabs.mute.notification.MuteNotificationHelper

class MuteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MuteNotificationHelper.ensureChannel(this)
        registerAppShortcuts()
    }

    private fun registerAppShortcuts() {
        runCatching {
            val muteShortcut = ShortcutInfoCompat.Builder(this, "shortcut_mute")
                .setShortLabel(getString(R.string.shortcut_mute_short))
                .setLongLabel(getString(R.string.shortcut_mute_long))
                .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher))
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("muto://mute")).setPackage(packageName))
                .build()

            val unmuteShortcut = ShortcutInfoCompat.Builder(this, "shortcut_unmute")
                .setShortLabel(getString(R.string.shortcut_unmute_short))
                .setLongLabel(getString(R.string.shortcut_unmute_long))
                .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher))
                .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("muto://unmute")).setPackage(packageName))
                .build()

            ShortcutManagerCompat.addDynamicShortcuts(this, listOf(muteShortcut, unmuteShortcut))
        }
    }
}

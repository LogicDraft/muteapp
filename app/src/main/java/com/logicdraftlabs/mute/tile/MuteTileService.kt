package com.logicdraftlabs.mute.tile

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import java.util.Calendar
import com.logicdraftlabs.mute.data.PrefsManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.core.MuteController

/**
 * Swipe down, tap once - no app screen involved. This is the intended everyday entry point.
 */
class MuteTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        if (!MuteController.isDndAccessGranted(this)) {
            openPermissionSettings()
            return
        }
        MuteController.toggle(this)
        refreshTile()
    }

    private data class TileVisual(
        val state: Int,
        val iconRes: Int,
        val statusText: String
    )

    private fun refreshTile() {
        val tile = qsTile ?: return
        val granted = MuteController.isDndAccessGranted(this)
        val muted = MuteController.isMuted(this)

        val visual = when {
            !granted -> TileVisual(Tile.STATE_INACTIVE, R.drawable.ic_tile_muted, getString(R.string.tile_label_permission))
            muted -> TileVisual(Tile.STATE_ACTIVE, R.drawable.ic_tile_muted, scheduledSubtitle())
            else -> TileVisual(Tile.STATE_INACTIVE, R.drawable.ic_tile_active, getString(R.string.tile_label_active))
        }

        tile.state = visual.state
        tile.icon = Icon.createWithResource(this, visual.iconRes)
        // Tile.subtitle only exists from API 29 (Q) - below that, fold the status into the label.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.label = getString(R.string.app_name)
            tile.subtitle = visual.statusText
        } else {
            tile.label = visual.statusText
        }
        tile.updateTile()
    }

    private fun scheduledSubtitle(): String {
        if (PrefsManager.getMuteSource(this) != PrefsManager.MuteSource.SCHEDULED) return getString(R.string.tile_label_muted)
        val minutes = PrefsManager.getScheduleEndMinutes(this)
        val c = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, minutes / 60); set(Calendar.MINUTE, minutes % 60) }
        return getString(R.string.tile_label_scheduled, DateFormat.getTimeFormat(this).format(c.time))
    }

    private fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}





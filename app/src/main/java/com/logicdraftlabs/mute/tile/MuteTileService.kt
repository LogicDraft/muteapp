package com.logicdraftlabs.mute.tile

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
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
        val muted = MuteController.isMuted(this)

        val visual = when {
            muted -> TileVisual(Tile.STATE_ACTIVE, R.drawable.ic_tile_muted, getString(R.string.tile_label_muted))
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

}





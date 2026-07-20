package com.logicdraftlabs.mute.controls

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.actions.BooleanAction
import android.service.controls.actions.ControlAction
import android.service.controls.templates.ControlButton
import android.service.controls.templates.ToggleTemplate
import androidx.annotation.RequiresApi
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.core.MuteController
import com.logicdraftlabs.mute.core.MuteStateBus
import com.logicdraftlabs.mute.ui.MainActivity
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.jdk9.asPublisher
import java.util.concurrent.Flow
import java.util.function.Consumer

@RequiresApi(Build.VERSION_CODES.R)
class MuteControlsProviderService : ControlsProviderService() {

    override fun createPublisherForAllAvailable(): Flow.Publisher<Control> {
        val control = createControl(MuteController.isMuted(this))
        return kotlinx.coroutines.flow.flowOf(control).asPublisher()
    }

    override fun createPublisherFor(controlIds: MutableList<String>): Flow.Publisher<Control> {
        return MuteStateBus.changes.map {
            createControl(MuteController.isMuted(this))
        }.asPublisher()
    }

    override fun performControlAction(
        controlId: String,
        action: ControlAction,
        consumer: Consumer<Int>
    ) {
        if (action is BooleanAction) {
            if (action.newState) {
                MuteController.mute(this)
            } else {
                MuteController.unmute(this)
            }
            consumer.accept(ControlAction.RESPONSE_OK)
        } else {
            consumer.accept(ControlAction.RESPONSE_UNKNOWN)
        }
    }

    private fun createControl(isMuted: Boolean): Control {
        val pi = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Control.StatefulBuilder(CONTROL_ID, pi)
            .setTitle(getString(R.string.app_name))
            .setSubtitle(getString(if (isMuted) R.string.widget_status_muted else R.string.widget_status_active))
            .setDeviceType(android.service.controls.DeviceTypes.TYPE_GENERIC_ON_OFF)
            .setStatus(Control.STATUS_OK)
            .setControlTemplate(
                ToggleTemplate(
                    CONTROL_ID,
                    ControlButton(isMuted, "Mute Toggle")
                )
            )
            .build()
    }

    companion object {
        const val CONTROL_ID = "mute_toggle_control"
    }
}

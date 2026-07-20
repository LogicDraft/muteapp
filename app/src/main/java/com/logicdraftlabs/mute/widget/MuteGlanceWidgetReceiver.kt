package com.logicdraftlabs.mute.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import com.logicdraftlabs.mute.core.MuteController

/**
 * GlanceAppWidgetReceiver wiring [MuteGlanceWidget] to the AppWidget system.
 *
 * This is the additive Glance entry point; the classic RemoteViews widgets
 * ([MuteWidgetProvider], [MuteWideWidgetProvider]) are kept in parallel.
 */
class MuteGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: MuteGlanceWidget = MuteGlanceWidget()
}

/**
 * Toggle action invoked when the Glance widget is tapped.
 * Mirrors ToggleReceiver's behavior but routed through Glance's action system.
 */
class GlanceToggleAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        MuteController.toggle(context)
        // Trigger an immediate update so the widget reflects the new state.
        MuteGlanceWidget().update(context, glanceId)
    }
}

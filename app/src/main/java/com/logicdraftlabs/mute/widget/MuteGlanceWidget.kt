package com.logicdraftlabs.mute.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.logicdraftlabs.mute.core.MuteController

class MuteGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isMuted = MuteController.isMuted(context)

        provideContent {
            GlanceTheme {
                WidgetContent(isMuted = isMuted)
            }
        }
    }
}

@Composable
private fun WidgetContent(isMuted: Boolean) {
    val bgColor = if (isMuted) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.background
    val accentColor = if (isMuted) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onBackground
    val labelText = if (isMuted) "MUTED" else "MUTE."

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionRunCallback<GlanceToggleAction>()),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = labelText,
                style = TextStyle(
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

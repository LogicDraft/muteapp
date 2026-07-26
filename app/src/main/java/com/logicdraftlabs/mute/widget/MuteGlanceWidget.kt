package com.logicdraftlabs.mute.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.core.MuteController
import com.logicdraftlabs.mute.data.PrefsManager

class MuteGlanceWidget : GlanceAppWidget() {

    companion object {
        private val SMALL_SQUARE = DpSize(40.dp, 40.dp)
        private val WIDE_RECTANGLE = DpSize(110.dp, 40.dp)
        private val LARGE_GRID = DpSize(180.dp, 100.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, WIDE_RECTANGLE, LARGE_GRID)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isMuted = MuteController.isMuted(context)
        val shapePref = PrefsManager.getWidgetShapePreference(context)

        provideContent {
            GlanceTheme {
                WidgetContent(
                    context = context,
                    isMuted = isMuted,
                    shapePref = shapePref
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    context: Context,
    isMuted: Boolean,
    shapePref: String
) {
    val bgColor = if (isMuted) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.surfaceVariant
    val accentColor = if (isMuted) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSurfaceVariant
    val labelText = if (isMuted) "MUTED" else "UNMUTED"

    val shapeDrawable = if (isMuted) {
        when (shapePref) {
            PrefsManager.SHAPE_CIRCLE -> R.drawable.widget_shape_circle
            PrefsManager.SHAPE_CLOVER -> R.drawable.widget_shape_clover
            PrefsManager.SHAPE_TEARDROP -> R.drawable.widget_shape_teardrop
            PrefsManager.SHAPE_FLOWER -> R.drawable.widget_shape_flower
            PrefsManager.SHAPE_STARBURST -> R.drawable.widget_shape_starburst
            else -> R.drawable.widget_bg_muted
        }
    } else {
        when (shapePref) {
            PrefsManager.SHAPE_CIRCLE -> R.drawable.widget_shape_circle
            PrefsManager.SHAPE_CLOVER -> R.drawable.widget_shape_clover
            PrefsManager.SHAPE_TEARDROP -> R.drawable.widget_shape_teardrop
            PrefsManager.SHAPE_FLOWER -> R.drawable.widget_shape_flower
            PrefsManager.SHAPE_STARBURST -> R.drawable.widget_shape_starburst
            else -> R.drawable.widget_bg_active
        }
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(shapeDrawable))
            .clickable(actionRunCallback<GlanceToggleAction>()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.padding(12.dp)
        ) {
            Text(
                text = labelText,
                style = TextStyle(
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = if (isMuted) "Tap to unmute" else "Tap to silence",
                style = TextStyle(
                    color = accentColor,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

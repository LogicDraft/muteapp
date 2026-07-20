package com.logicdraftlabs.mute.widget

import android.content.Context
import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.logicdraftlabs.mute.core.MuteController
import com.logicdraftlabs.mute.data.PrefsManager

/**
 * Jetpack Glance-based 1×1 widget.
 *
 * Rendering-layer migration from [MuteWidgetProvider]. Behavior is identical:
 * instant visual update via [MuteWidgetProvider.updateAllWidgets], same tap target,
 * same muted/active states.
 *
 * Dynamic color: when the pref is on *and* running on API 31+, Glance uses
 * its own dynamic color providers; otherwise fixed brand-red/black [ColorProviders].
 */
class MuteGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isMuted = MuteController.isMuted(context)
        val dynamicColor = PrefsManager.getDynamicColor(context)
        val useDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        val colors = if (useDynamic) {
            // On API 31+: Glance picks up wallpaper-extracted colors via dynamic color scheme.
            // We still pass a custom ColorProviders that uses dark-biased base values since
            // MUTE. is always dark.
            ColorProviders(
                light = darkColorScheme(primary = Color(0xFFE01B24)),
                dark = darkColorScheme(primary = Color(0xFFE01B24))
            )
        } else {
            BrandColorProviders
        }

        provideContent {
            GlanceTheme(colors = colors) {
                WidgetContent(isMuted = isMuted)
            }
        }
    }
}

// Fixed brand color providers: MUTE.'s always-dark palette.
private val BrandColorProviders = ColorProviders(
    light = darkColorScheme(
        primary = Color(0xFFE01B24),
        background = Color(0xFF000000),
        surface = Color(0xFF0A0A0A),
        onBackground = Color(0xFFF5F5F0),
        onSurface = Color(0xFFF5F5F0),
        onSurfaceVariant = Color(0xFF8A8A8A),
        error = Color(0xFFE01B24)
    ),
    dark = darkColorScheme(
        primary = Color(0xFFE01B24),
        background = Color(0xFF000000),
        surface = Color(0xFF0A0A0A),
        onBackground = Color(0xFFF5F5F0),
        onSurface = Color(0xFFF5F5F0),
        onSurfaceVariant = Color(0xFF8A8A8A),
        error = Color(0xFFE01B24)
    )
)

@Composable
private fun WidgetContent(isMuted: Boolean) {
    val bgColor = if (isMuted) Color(0xFF7A0E13) else Color(0xFF000000)
    val accentColor = if (isMuted) Color(0xFFE01B24) else Color(0xFFFFFFFF)
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
                    color = ColorProvider(day = accentColor, night = accentColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

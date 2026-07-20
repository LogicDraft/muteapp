package com.logicdraftlabs.mute.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.logicdraftlabs.mute.data.PrefsManager

private val MuteColorScheme = darkColorScheme(
    primary = SignalRed,
    onPrimary = PureWhite,
    secondary = DimGrey,
    onSecondary = TrueBlack,
    background = TrueBlack,
    onBackground = OffWhite,
    surface = NearBlack,
    onSurface = OffWhite,
    surfaceVariant = InkGrey,
    onSurfaceVariant = DimGrey,
    surfaceContainer = InkGrey,
    outline = LineGrey,
    error = SignalRed,
    onError = PureWhite
)

@Composable
fun MuteTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dynamicColorEnabled = PrefsManager.getDynamicColor(context)
    val colorScheme = if (dynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        MuteColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MuteTypography,
        content = content
    )
}


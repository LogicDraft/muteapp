package com.logicdraftlabs.mute.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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
    outline = LineGrey,
    error = SignalRed,
    onError = PureWhite
)

@Composable
fun MuteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MuteColorScheme,
        typography = MuteTypography,
        content = content
    )
}

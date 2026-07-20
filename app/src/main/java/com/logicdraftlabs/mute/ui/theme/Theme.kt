package com.logicdraftlabs.mute.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.logicdraftlabs.mute.data.PrefsManager

private val DarkColors = darkColorScheme(
    primary = Pine,
    onPrimary = DeepForest,
    primaryContainer = PineDark,
    onPrimaryContainer = Mist,
    secondary = Mint,
    tertiary = Aqua,
    background = DeepForest,
    surface = Slate,
    surfaceContainer = ColorTokens.darkContainer,
    surfaceContainerHigh = ColorTokens.darkContainerHigh,
    surfaceContainerHighest = ColorTokens.darkContainerHighest,
    onSurface = Mist,
    onSurfaceVariant = Ash
)

private val LightColors = lightColorScheme(
    primary = PineDark,
    onPrimary = Mist,
    primaryContainer = Mint,
    onPrimaryContainer = DeepForest,
    secondary = Pine,
    tertiary = Aqua,
    background = ColorTokens.lightBackground,
    surface = ColorTokens.lightSurface,
    surfaceContainer = ColorTokens.lightContainer,
    surfaceContainerHigh = ColorTokens.lightContainerHigh,
    surfaceContainerHighest = ColorTokens.lightContainerHighest,
    onSurface = ColorTokens.lightOnSurface,
    onSurfaceVariant = ColorTokens.lightOnSurfaceVariant
)

@Composable
fun MuteTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    
    val themePref = PrefsManager.getThemePreference(context)
    val dynamicColorsEnabled = PrefsManager.isDynamicColorEnabled(context)
    
    val darkTheme = when (themePref) {
        PrefsManager.THEME_SYSTEM -> isSystemDark
        PrefsManager.THEME_LIGHT -> false
        PrefsManager.THEME_DARK -> true
        else -> isSystemDark
    }

    val colorScheme = when {
        dynamicColorsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MdifyTypography,
        content = content
    )
}

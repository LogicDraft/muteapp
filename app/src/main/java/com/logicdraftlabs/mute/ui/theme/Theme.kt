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
    primary = M3PrimaryDark,
    onPrimary = M3OnPrimaryDark,
    primaryContainer = M3PrimaryContainerDark,
    onPrimaryContainer = M3OnPrimaryContainerDark,
    secondary = M3SecondaryDark,
    onSecondary = M3OnSecondaryDark,
    secondaryContainer = M3SecondaryContainerDark,
    onSecondaryContainer = M3OnSecondaryContainerDark,
    tertiary = M3TertiaryDark,
    onTertiary = M3OnTertiaryDark,
    tertiaryContainer = M3TertiaryContainerDark,
    onTertiaryContainer = M3OnTertiaryContainerDark,
    background = M3BackgroundDark,
    onBackground = M3OnBackgroundDark,
    surface = M3SurfaceDark,
    onSurface = M3OnSurfaceDark,
    surfaceContainerLowest = M3SurfaceContainerLowestDark,
    surfaceContainerLow = M3SurfaceContainerLowDark,
    surfaceContainer = M3SurfaceContainerDark,
    surfaceContainerHigh = M3SurfaceContainerHighDark,
    surfaceContainerHighest = M3SurfaceContainerHighestDark,
    onSurfaceVariant = M3OnSurfaceVariantDark,
    outline = M3OutlineDark
)

private val LightColors = lightColorScheme(
    primary = M3PrimaryLight,
    onPrimary = M3OnPrimaryLight,
    primaryContainer = M3PrimaryContainerLight,
    onPrimaryContainer = M3OnPrimaryContainerLight,
    secondary = M3SecondaryLight,
    onSecondary = M3OnSecondaryLight,
    secondaryContainer = M3SecondaryContainerLight,
    onSecondaryContainer = M3OnSecondaryContainerLight,
    tertiary = M3TertiaryLight,
    onTertiary = M3OnTertiaryLight,
    tertiaryContainer = M3TertiaryContainerLight,
    onTertiaryContainer = M3OnTertiaryContainerLight,
    background = M3BackgroundLight,
    onBackground = M3OnBackgroundLight,
    surface = M3SurfaceLight,
    onSurface = M3OnSurfaceLight,
    surfaceContainerLowest = M3SurfaceContainerLowestLight,
    surfaceContainerLow = M3SurfaceContainerLowLight,
    surfaceContainer = M3SurfaceContainerLight,
    surfaceContainerHigh = M3SurfaceContainerHighLight,
    surfaceContainerHighest = M3SurfaceContainerHighestLight,
    onSurfaceVariant = M3OnSurfaceVariantLight,
    outline = M3OutlineLight
)

@Composable
fun MuteTheme(
    themePref: String = PrefsManager.getThemePreference(LocalContext.current),
    dynamicColorsEnabled: Boolean = PrefsManager.isDynamicColorEnabled(LocalContext.current),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    
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

package com.logicdraftlabs.mute.ui.theme

import androidx.compose.ui.graphics.Color

// Primary & Secondary Brand Tokens (Pixel Emerald / Pine Palette)
val M3PrimaryDark = Color(0xFF56DCA2)
val M3OnPrimaryDark = Color(0xFF003822)
val M3PrimaryContainerDark = Color(0xFF005234)
val M3OnPrimaryContainerDark = Color(0xFF76F9BD)

val M3SecondaryDark = Color(0xFFB4CCBC)
val M3OnSecondaryDark = Color(0xFF20352A)
val M3SecondaryContainerDark = Color(0xFF364B3F)
val M3OnSecondaryContainerDark = Color(0xFFCFE8D7)

val M3TertiaryDark = Color(0xFFA5CCE8)
val M3OnTertiaryDark = Color(0xFF07354B)
val M3TertiaryContainerDark = Color(0xFF244B63)
val M3OnTertiaryContainerDark = Color(0xFFC1E8FF)

val M3BackgroundDark = Color(0xFF0F1512)
val M3OnBackgroundDark = Color(0xFFDEE4DF)
val M3SurfaceDark = Color(0xFF0F1512)
val M3OnSurfaceDark = Color(0xFFDEE4DF)

val M3SurfaceContainerLowestDark = Color(0xFF0A0F0D)
val M3SurfaceContainerLowDark = Color(0xFF171D1A)
val M3SurfaceContainerDark = Color(0xFF1B211E)
val M3SurfaceContainerHighDark = Color(0xFF262C28)
val M3SurfaceContainerHighestDark = Color(0xFF313733)
val M3OnSurfaceVariantDark = Color(0xFFBEC9C2)
val M3OutlineDark = Color(0xFF88938C)

// Light Theme Tokens
val M3PrimaryLight = Color(0xFF006C47)
val M3OnPrimaryLight = Color(0xFFFFFFFF)
val M3PrimaryContainerLight = Color(0xFF76F9BD)
val M3OnPrimaryContainerLight = Color(0xFF002112)

val M3SecondaryLight = Color(0xFF4E6356)
val M3OnSecondaryLight = Color(0xFFFFFFFF)
val M3SecondaryContainerLight = Color(0xFFCFE8D7)
val M3OnSecondaryContainerLight = Color(0xFF0B1F15)

val M3TertiaryLight = Color(0xFF3C637C)
val M3OnTertiaryLight = Color(0xFFFFFFFF)
val M3TertiaryContainerLight = Color(0xFFC1E8FF)
val M3OnTertiaryContainerLight = Color(0xFF001E2C)

val M3BackgroundLight = Color(0xFFF5FAF5)
val M3OnBackgroundLight = Color(0xFF171D1A)
val M3SurfaceLight = Color(0xFFF5FAF5)
val M3OnSurfaceLight = Color(0xFF171D1A)

val M3SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val M3SurfaceContainerLowLight = Color(0xFFEFF5F0)
val M3SurfaceContainerLight = Color(0xFFE9EFEA)
val M3SurfaceContainerHighLight = Color(0xFFE3EAE4)
val M3SurfaceContainerHighestLight = Color(0xFFDDE4DE)
val M3OnSurfaceVariantLight = Color(0xFF3F4943)
val M3OutlineLight = Color(0xFF707973)

// Legacy backward-compatibility aliases
val Pine = M3PrimaryDark
val PineDark = M3PrimaryLight
val Mint = M3PrimaryContainerDark
val DeepForest = M3BackgroundDark
val Slate = M3SurfaceContainerDark
val Mist = M3OnBackgroundDark
val Ash = M3OnSurfaceVariantDark
val Aqua = M3TertiaryDark

object ColorTokens {
    val lightBackground = M3BackgroundLight
    val lightSurface = M3SurfaceLight
    val lightContainer = M3SurfaceContainerLight
    val lightContainerHigh = M3SurfaceContainerHighLight
    val lightContainerHighest = M3SurfaceContainerHighestLight
    val lightOnSurface = M3OnSurfaceLight
    val lightOnSurfaceVariant = M3OnSurfaceVariantLight

    val darkContainer = M3SurfaceContainerDark
    val darkContainerHigh = M3SurfaceContainerHighDark
    val darkContainerHighest = M3SurfaceContainerHighestDark
}


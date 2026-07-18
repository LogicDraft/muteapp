package com.gowtham.mute.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * NOTE ON THE FONT: Nothing's own "Ndot" dot-matrix face is proprietary to Nothing Technology
 * and isn't safe to bundle here. FontFamily.Monospace is used as a stand-in with wide letter
 * spacing to approximate that LCD/dot-matrix numeral feel.
 *
 * To get closer to the real thing, drop an open-licensed dot-matrix face (e.g. "DSEG7 Classic",
 * SIL OFL-licensed, or Google Fonts "Silkscreen") into res/font/ as e.g. dot_matrix.ttf, then:
 *   val DotMatrix = FontFamily(Font(R.font.dot_matrix))
 * and swap it in below.
 */
private val DisplayFace = FontFamily.Monospace
private val BodyFace = FontFamily.SansSerif

val MuteTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFace,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        letterSpacing = 4.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 3.sp
    ),
    titleLarge = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = DisplayFace,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
)

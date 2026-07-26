package com.logicdraftlabs.mute.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.logicdraftlabs.mute.ui.theme.M3PrimaryDark
import java.util.Calendar

/**
 * Material 3 Expressive Hero Ring-to-Disc Mute Dial Button.
 * Implements sonar ripple pings, diagonal glare sweep, radial center flash,
 * ring-to-disc stroke morph, spring scale physics, and tactile haptics.
 */
@Composable
fun RingToDiscHeroButton(
    isMuted: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val reducedMotion = rememberReducedMotion()

    val transition = updateTransition(targetState = isMuted, label = "dial_state_transition")

    // Morph factor from 0f (thin outline ring) to 1f (solid filled disc)
    val fillFactor by transition.animateFloat(
        transitionSpec = {
            if (reducedMotion) snap() else tween(durationMillis = 450, easing = FastOutSlowInEasing)
        },
        label = "fill_factor"
    ) { muted -> if (muted) 1f else 0f }

    // Spring scale effect on tap
    val dialScale by transition.animateFloat(
        transitionSpec = {
            if (reducedMotion) snap() else spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "dial_scale"
    ) { muted -> if (muted) 1.06f else 1.0f }

    // One-shot ripple/flash animation progress (0f to 1f) on toggle
    val effectProgress = remember { Animatable(1f) }
    LaunchedEffect(isMuted) {
        if (!reducedMotion) {
            effectProgress.snapTo(0f)
            effectProgress.animateTo(1f, tween(550, easing = LinearEasing))
        } else {
            effectProgress.snapTo(1f)
        }
    }

    val p = effectProgress.value

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val outlineColor = MaterialTheme.colorScheme.outline

    val accessibilityLabel = if (isMuted) "Muted. Tap to unmute." else "Unmuted. Tap to mute."

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .semantics {
                    contentDescription = accessibilityLabel
                }
        ) {
            // Background subtle ambient aura
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        scaleX = dialScale * 1.05f
                        scaleY = dialScale * 1.05f
                        alpha = if (isMuted) 0.15f else 0.05f
                    }
                    .background(primaryColor, CircleShape)
            )

            // Main Canvas for Ring-to-Disc Morph, Sonar Pings, Glare Sweep & Flash
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = dialScale
                        scaleY = dialScale
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            triggerTactileHaptic(context, haptic)
                            onTap()
                        }
                    )
            ) {
                val radius = size.minDimension / 2.0f

                // 1. Sonar Ripple Pings (expanding concentric rings with fading opacity)
                if (p < 1f) {
                    val rippleCount = 4
                    for (i in 0 until rippleCount) {
                        val delay = i * 0.12f
                        val rippleP = ((p - delay) / (1f - delay)).coerceIn(0f, 1f)
                        if (rippleP > 0f) {
                            val rippleRadius = radius + (rippleP * 80.dp.toPx())
                            val rippleAlpha = (1f - rippleP) * 0.45f
                            drawCircle(
                                color = primaryColor.copy(alpha = rippleAlpha),
                                radius = rippleRadius,
                                style = Stroke(width = (2.5).dp.toPx())
                            )
                        }
                    }
                }

                // 2. Base Ring / Solid Disc Morph
                val minStrokePx = 4.dp.toPx()
                val currentStrokePx = minStrokePx + (radius - minStrokePx) * fillFactor

                // Base container background ring
                drawCircle(
                    color = containerColor,
                    radius = radius - (minStrokePx / 2.0f),
                    style = Stroke(width = minStrokePx)
                )

                // Animated Primary Fill Morph
                drawCircle(
                    color = primaryColor,
                    radius = radius - (currentStrokePx / 2.0f),
                    style = Stroke(width = currentStrokePx)
                )

                // 3. Diagonal Glare Light Sweep across button surface
                if (p < 1f) {
                    val sweepOffset = -radius + (p * radius * 3.2f)
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            start = Offset(sweepOffset, sweepOffset),
                            end = Offset(sweepOffset + 140f, sweepOffset + 140f)
                        ),
                        radius = radius - 2.dp.toPx()
                    )
                }

                // 4. Center Radial Camera Flash at peak transition (midpoint ~0.3 - 0.7)
                if (p in 0.25f..0.75f) {
                    val flashP = if (p < 0.5f) (p - 0.25f) / 0.25f else (0.75f - p) / 0.25f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = flashP * 0.65f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * 0.85f
                        ),
                        radius = radius
                    )
                }
            }

            // Central State Label with Crossfade
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = dialScale
                        scaleY = dialScale
                    }
            ) {
                val textColor = if (fillFactor > 0.5f) onPrimaryColor else MaterialTheme.colorScheme.onSurface

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isMuted) "MUTED" else "ACTIVE",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = textColor,
                        modifier = Modifier.graphicsLayer {
                            alpha = if (p in 0.3f..0.7f) {
                                if (p < 0.5f) 1f - ((p - 0.3f) / 0.2f) else ((p - 0.5f) / 0.2f)
                            } else 1f
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isMuted) "Tap to unmute" else "Tap to silence",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Expressive Container Card with squircle shape and Material 3 Container colors.
 */
@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(20.dp)
    } else {
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .padding(20.dp)
    }

    Column(modifier = cardModifier) {
        content()
    }
}

/**
 * Shimmer Loading Skeleton for smooth content loading feedback.
 */
@Composable
fun ShimmerSkeletonLoader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha))
    )
}

/**
 * Multi-select Day Selector Pills (Mon - Sun).
 */
@Composable
fun DayPickerPills(
    selectedDays: Set<Int>,
    onDayToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayMap = listOf(
        Calendar.MONDAY to "M",
        Calendar.TUESDAY to "T",
        Calendar.WEDNESDAY to "W",
        Calendar.THURSDAY to "T",
        Calendar.FRIDAY to "F",
        Calendar.SATURDAY to "S",
        Calendar.SUNDAY to "S"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dayMap.forEach { (dayInt, label) ->
            val isSelected = selectedDays.contains(dayInt)
            val pillBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
            val pillText = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(pillBg)
                    .clickable { onDayToggle(dayInt) }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = pillText
                )
            }
        }
    }
}

/**
 * System check for Reduce Motion accessibility setting.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}

/**
 * Tactile haptic feedback helper using Android Vibrator API with Compose fallback.
 */
private fun triggerTactileHaptic(context: Context, composeHaptic: androidx.compose.ui.hapticfeedback.HapticFeedback) {
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val vibrator = vibratorManager?.defaultVibrator
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }.onFailure {
        composeHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

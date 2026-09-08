package com.logicdraftlabs.mute.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object MutoMotion {
    const val Fast = 120
    const val Normal = 300
    const val Medium = 400
}

@Composable
fun MutoHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "M U T O.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "One tap. Total silence.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MutoToggle(isMuted: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) .96f else 1f,
        animationSpec = tween(MutoMotion.Fast, easing = FastOutSlowInEasing),
        label = "muto_toggle_press"
    )
    val haptics = LocalHapticFeedback.current
    val outline = if (isMuted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val container = if (isMuted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    val iconColor = if (isMuted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val description = if (isMuted) "Restore phone sound" else "Mute phone"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(264.dp)
            .scale(scale)
            .border(width = 2.dp, color = outline, shape = CircleShape)
            .semantics { contentDescription = description }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(232.dp)
                .border(width = 1.dp, color = outline.copy(alpha = .35f), shape = CircleShape)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(targetState = isMuted, label = "muto_icon") { silent ->
                    Icon(
                        imageVector = if (silent) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(54.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                AnimatedContent(targetState = isMuted, label = "muto_state") { silent ->
                    Text(
                        text = if (silent) "SILENT" else "READY",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = iconColor
                    )
                }
            }
        }
    }
}

@Composable
fun StatusCard(title: String, body: String, modifier: Modifier = Modifier, icon: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            icon()
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MuteStateLabel(isMuted: Boolean) {
    Text(
        text = if (isMuted) "Silence is active." else "Your sound is ready.",
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

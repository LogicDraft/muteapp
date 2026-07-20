package com.logicdraftlabs.mute.ui

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.ui.theme.MuteTheme


class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent { MuteTheme { SettingsHubScreen(onBack = { finish() }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsHubScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated decorative header
            AnimatedRingHeader()

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_hub_tagline),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 28.dp)
            )

            // Hub card rows
            HubCard(
                icon = Icons.Default.VolumeOff,
                title = stringResource(R.string.settings_dnd_title),
                subtitle = stringResource(R.string.settings_dnd_subtitle),
                onClick = { context.startActivity(Intent(context, DndSettingsActivity::class.java)) }
            )
            Spacer(Modifier.height(12.dp))
            HubCard(
                icon = Icons.Default.Schedule,
                title = stringResource(R.string.schedules_title),
                subtitle = stringResource(R.string.schedules_subtitle),
                onClick = { context.startActivity(Intent(context, SchedulesActivity::class.java)) }
            )
            Spacer(Modifier.height(12.dp))
            HubCard(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.settings_notifications_title),
                subtitle = stringResource(R.string.settings_notifications_subtitle),
                onClick = { context.startActivity(Intent(context, NotificationsSettingsActivity::class.java)) }
            )
            Spacer(Modifier.height(12.dp))

            HubCard(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.settings_look_feel_title),
                subtitle = stringResource(R.string.settings_look_feel_subtitle),
                onClick = { context.startActivity(Intent(context, LookAndFeelActivity::class.java)) }
            )
            Spacer(Modifier.height(12.dp))

            HubCard(
                icon = Icons.Default.Info,
                title = stringResource(R.string.settings_about_title),
                subtitle = stringResource(R.string.settings_about_subtitle),
                onClick = { context.startActivity(Intent(context, AboutActivity::class.java)) }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Two offset rings, slowly counter-rotating — MUTE.'s own ring/dot motif. */
@Composable
private fun AnimatedRingHeader() {
    val reducedMotion = rememberReducedMotion()
    val durationMs = if (reducedMotion) Int.MAX_VALUE else 14_000

    val infiniteTransition = rememberInfiniteTransition(label = "rings")

    val outerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_ring"
    )
    val innerAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (durationMs * 0.65f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_ring"
    )

    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 16.dp)
            .size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring — large, slow
        RingArc(
            size = 100.dp,
            strokeWidth = 2.dp,
            rotation = outerAngle,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            dotCount = 3
        )
        // Middle ring
        RingArc(
            size = 66.dp,
            strokeWidth = 1.5f.dp,
            rotation = innerAngle,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.70f),
            dotCount = 2
        )
        // Center dot
        Surface(
            modifier = Modifier.size(10.dp),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary
        ) {}
    }
}

@Composable
private fun RingArc(
    size: Dp,
    strokeWidth: Dp,
    rotation: Float,
    color: Color,
    dotCount: Int
) {
    Canvas(
        modifier = Modifier
            .size(size)
            .rotate(rotation)
    ) {
        val radius = (this.size.minDimension / 2f) - strokeWidth.toPx()
        val sw = strokeWidth.toPx()

        // Draw ring arc (dashed — 2/3 of circle)
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 240f,
            useCenter = false,
            style = Stroke(width = sw)
        )

        // Draw dots evenly spaced
        val dotR = sw * 1.6f
        val cx = this.size.minDimension / 2f
        val cy = this.size.minDimension / 2f
        val step = 360f / dotCount
        for (i in 0 until dotCount) {
            val angle = Math.toRadians((i * step).toDouble())
            val dx = (cx + radius * Math.cos(angle)).toFloat()
            val dy = (cy + radius * Math.sin(angle)).toFloat()
            drawCircle(color = color, radius = dotR, center = Offset(dx, dy))
        }
    }
}

@Composable
private fun HubCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        onClick = onClick
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            },
            headlineContent = {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            },
            supportingContent = {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}  // end HubCard

@Composable
private fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}

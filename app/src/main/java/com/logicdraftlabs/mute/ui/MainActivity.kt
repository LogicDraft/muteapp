package com.logicdraftlabs.mute.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.SolidColor
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.core.MuteController
import com.logicdraftlabs.mute.core.MuteStateBus
import com.logicdraftlabs.mute.ui.theme.MuteTheme
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width


class MainActivity : ComponentActivity() {

    private val refreshTick = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            MuteTheme {
                MainScreen(refreshTick = refreshTick.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        var targetAction: String? = intent.getStringExtra("voice_action")

        // Parse search / typed queries from Gemini / Google Assistant
        val query = intent.getStringExtra(android.app.SearchManager.QUERY)
            ?: intent.getStringExtra("query")
            ?: intent.dataString

        if (targetAction == null && query != null) {
            val lowerQuery = query.lowercase()
            when {
                lowerQuery.contains("unmute") || lowerQuery.contains("turn off") || lowerQuery.contains("stop") || lowerQuery.endsWith("/unmute") || lowerQuery.endsWith("off") -> {
                    targetAction = "unmute"
                }
                lowerQuery.contains("mute") || lowerQuery.contains("turn on") || lowerQuery.contains("silent") || lowerQuery.endsWith("/mute") || lowerQuery.endsWith("on") -> {
                    targetAction = "mute"
                }
                lowerQuery.contains("toggle") || lowerQuery.endsWith("/toggle") -> {
                    targetAction = "toggle"
                }
            }
        }

        // Check Uri data scheme / host / path
        val uriData = intent.data
        if (targetAction == null && uriData != null) {
            val path = uriData.path ?: uriData.host ?: ""
            when {
                path.contains("unmute") || path.contains("off") -> targetAction = "unmute"
                path.contains("mute") || path.contains("on") -> targetAction = "mute"
                path.contains("toggle") -> targetAction = "toggle"
            }
        }

        if (targetAction != null) {
            val isCurrentlyMuted = MuteController.isMuted(this)
            when (targetAction) {
                "mute", "on" -> {
                    if (!isCurrentlyMuted) {
                        MuteController.mute(this)
                    }
                    android.widget.Toast.makeText(this, R.string.widget_status_muted, android.widget.Toast.LENGTH_SHORT).show()
                }
                "unmute", "off" -> {
                    if (isCurrentlyMuted) {
                        MuteController.unmute(this)
                    }
                    android.widget.Toast.makeText(this, R.string.tile_label_active, android.widget.Toast.LENGTH_SHORT).show()
                }
                "toggle" -> {
                    MuteController.toggle(this)
                    val newStatus = if (MuteController.isMuted(this)) R.string.widget_status_muted else R.string.tile_label_active
                    android.widget.Toast.makeText(this, newStatus, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            refreshTick.value++
        }
    }

    override fun onResume() {
        super.onResume()
        refreshTick.value++
    }

    companion object {
        const val EXTRA_REQUEST_PERMISSION = "extra_request_permission"
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(refreshTick: Int) {
    val context = LocalContext.current
    var isGranted by remember { mutableStateOf(MuteController.isDndAccessGranted(context)) }
    var isMuted by remember { mutableStateOf(MuteController.isMuted(context)) }
    val busTick by MuteStateBus.changes.collectAsState()

    LaunchedEffect(refreshTick, busTick) {
        isGranted = MuteController.isDndAccessGranted(context)
        isMuted = MuteController.isMuted(context)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) }) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings_link))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isGranted) {
                PermissionPrompt(
                    onGrant = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        )
                    }
                )
            } else {
                AnimatedCircularToggleButton(
                    isMuted = isMuted,
                    onTap = {
                        isMuted = MuteController.toggle(context)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            StatusHintText(
                context = context,
                isMuted = isMuted,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
private fun PermissionPrompt(onGrant: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.permission_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp, bottom = 26.dp)
        )
        Button(
            onClick = onGrant,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = stringResource(R.string.permission_grant),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}





@Composable
private fun AnimatedCircularToggleButton(isMuted: Boolean, onTap: () -> Unit) {
    val reducedMotion = rememberReducedMotion()
    
    val transition = updateTransition(targetState = isMuted, label = "dial_transition")
    
    // Animate fill factor from 0f (outline) to 1f (solid disc)
    val fillFactor by transition.animateFloat(
        transitionSpec = { if (reducedMotion) snap() else tween(500, easing = LinearEasing) },
        label = "fill_factor"
    ) { muted -> 
        if (muted) 1f else 0f 
    }
    
    // One-shot effect progress from 0f to 1f on every toggle
    val effectProgress = remember { Animatable(1f) }
    LaunchedEffect(isMuted) {
        if (!reducedMotion) {
            effectProgress.snapTo(0f)
            effectProgress.animateTo(1f, tween(500, easing = LinearEasing))
        } else {
            effectProgress.snapTo(1f)
        }
    }
    
    val p = effectProgress.value
    
    val dialScale by transition.animateFloat(
        transitionSpec = { if (reducedMotion) snap() else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "dial_scale"
    ) { muted -> if (muted) 1.05f else 1f }
    
    val actionLabel = if (isMuted) stringResource(R.string.hint_muted) else stringResource(R.string.hint_active)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val outlineColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center, 
            modifier = Modifier.fillMaxWidth().heightIn(min = 350.dp)
        ) {
            // Main Button Canvas
            Canvas(
                modifier = Modifier
                    .size(190.dp)
                    .graphicsLayer {
                        scaleX = dialScale
                        scaleY = dialScale
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // Disable default ripple since we draw our own
                        onClick = onTap
                    )
                    .semantics {
                        contentDescription = actionLabel
                    }
            ) {
                val radius = size.minDimension / 2
                
                // 1. Ring/Disc Fill Morph
                val minStroke = 3.dp.toPx()
                val currentStroke = minStroke + (radius - minStroke) * fillFactor
                drawCircle(
                    color = primaryColor,
                    radius = radius - (currentStroke / 2),
                    style = Stroke(width = currentStroke)
                )
                
                // 2. Ripple Burst (Sonar Pings)
                if (p < 1f) {
                    for (i in 0 until 4) {
                        val delay = i * 0.12f
                        val rippleP = ((p - delay) / (1f - delay)).coerceIn(0f, 1f)
                        if (rippleP > 0f) {
                            val rippleRadius = radius + (rippleP * 120.dp.toPx())
                            val rippleAlpha = (1f - rippleP) * 0.5f
                            drawCircle(
                                color = primaryColor.copy(alpha = rippleAlpha),
                                radius = rippleRadius,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
                
                // 3. Diagonal Light Sweep
                if (p < 1f) {
                    val sweepOffset = -radius + (p * radius * 3)
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent),
                            start = Offset(sweepOffset, sweepOffset),
                            end = Offset(sweepOffset + 150f, sweepOffset + 150f)
                        ),
                        radius = radius
                    )
                }
                
                // 4. Center Flash
                if (p in 0.3f..0.7f) {
                    val flashP = if (p < 0.5f) (p - 0.3f) / 0.2f else (0.7f - p) / 0.2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = flashP * 0.6f), Color.Transparent),
                            center = center,
                            radius = radius * 0.8f
                        ),
                        radius = radius
                    )
                }
            }
            
            // Text Label with Crossfade
            // We use Box to center the text over the canvas
            Box(
                contentAlignment = Alignment.Center, 
                modifier = Modifier
                    .size(190.dp)
                    .graphicsLayer {
                        scaleX = dialScale
                        scaleY = dialScale
                    }
            ) {
                // Determine text color based on fill factor to ensure contrast
                // If fillFactor > 0.5, we're mostly filled, use onPrimaryColor, else use onSurfaceColor
                val textColor = if (fillFactor > 0.5f) onPrimaryColor else onSurfaceColor
                
                Text(
                    text = if (isMuted) stringResource(R.string.status_muted) else stringResource(R.string.status_active),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    // Alpha crossfade during transition midpoint
                    modifier = Modifier.graphicsLayer {
                        alpha = if (p in 0.3f..0.7f) {
                            if (p < 0.5f) 1f - ((p - 0.3f) / 0.2f) else ((p - 0.5f) / 0.2f)
                        } else 1f
                    }
                )
            }
        }

        Text(
            text = if (isMuted) stringResource(R.string.hint_muted) else stringResource(R.string.hint_active),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 18.dp)
        )
    }
}

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

@Composable
private fun StatusHintText(context: Context, isMuted: Boolean, modifier: Modifier = Modifier) {
    val schedules = remember(isMuted) { com.logicdraftlabs.mute.data.PrefsManager.getSchedules(context).filter { it.enabled } }
    
    val hint = remember(schedules, isMuted) {
        val now = System.currentTimeMillis()
        val allWindows = schedules.flatMap { com.logicdraftlabs.mute.core.ScheduleManager.getNextWindows(it).map { w -> it to w } }
        
        val currentSource = com.logicdraftlabs.mute.data.PrefsManager.getMuteSource(context)
        
        if (isMuted && currentSource is com.logicdraftlabs.mute.data.PrefsManager.MuteSource.Scheduled) {
            val activeSchedule = schedules.find { it.id == currentSource.scheduleId }
            if (activeSchedule != null) {
                val currentWindow = allWindows.find { it.first.id == activeSchedule.id && now in it.second.start..it.second.end }
                if (currentWindow != null) {
                    val endCal = java.util.Calendar.getInstance().apply { timeInMillis = currentWindow.second.end }
                    val formatter = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT)
                    return@remember "Muted by ${activeSchedule.label} until ${formatter.format(endCal.time)}"
                }
            }
        }
        
        val nextWindow = allWindows.filter { it.second.start > now }.minByOrNull { it.second.start }
        if (nextWindow != null) {
            val startCal = java.util.Calendar.getInstance().apply { timeInMillis = nextWindow.second.start }
            val formatter = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT)
            
            val dayStr = if (startCal.get(java.util.Calendar.DAY_OF_YEAR) == java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)) {
                "today"
            } else if (startCal.get(java.util.Calendar.DAY_OF_YEAR) == java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR) + 1) {
                "tomorrow"
            } else {
                "soon" // simplified
            }
            return@remember "Next: ${nextWindow.first.label} $dayStr at ${formatter.format(startCal.time)}"
        }
        
        return@remember context.getString(R.string.usage_hint)
    }
    
    Text(
        text = hint,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}


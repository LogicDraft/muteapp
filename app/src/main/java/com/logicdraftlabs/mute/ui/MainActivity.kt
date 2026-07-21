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
    
    var shockwaves by remember { mutableStateOf(listOf<Long>()) }

    val transition = updateTransition(targetState = isMuted, label = "dial_transition")
    
    val containerColor by transition.animateColor(
        transitionSpec = { if (reducedMotion) snap() else tween(300) },
        label = "dial_container_color"
    ) { muted -> 
        if (muted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant 
    }
    
    val contentColor by transition.animateColor(
        transitionSpec = { if (reducedMotion) snap() else tween(300) },
        label = "dial_content_color"
    ) { muted -> 
        if (muted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant 
    }
    
    val dialScale by transition.animateFloat(
        transitionSpec = { if (reducedMotion) snap() else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "dial_scale"
    ) { muted -> if (muted) 1.05f else 1f }
    
    val actionLabel = if (isMuted) stringResource(R.string.hint_muted) else stringResource(R.string.hint_active)

    val infiniteTransition = rememberInfiniteTransition(label = "animations")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_rotation"
    )

    // Time driver forces recomposition for the Canvas animations
    val timeDriver by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, 
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "time_driver"
    )

    // Reusable Path to avoid GC thrashing and RAM issues
    val wavePath = remember { Path() }
    val glowColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(timeDriver) {
        if (shockwaves.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            val filtered = shockwaves.filter { currentTime - it < 1500 }
            if (filtered.size != shockwaves.size) {
                shockwaves = filtered
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center, 
            modifier = Modifier.fillMaxWidth().heightIn(min = 350.dp)
        ) {
            // Shockwave Canvas - Low CPU footprint, no new object allocations in draw loop
            if (!reducedMotion && shockwaves.isNotEmpty()) {
                Canvas(modifier = Modifier.size(350.dp)) {
                    val currentTime = System.currentTimeMillis()
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    for (startT in shockwaves) {
                        val age = (currentTime - startT) / 1500f
                        if (age > 1f) continue
                        
                        val baseRadius = 95.dp.toPx() + (age * 120.dp.toPx())
                        val alpha = (1f - age).coerceIn(0f, 1f)
                        val color = glowColor.copy(alpha = alpha * 0.6f)
                        
                        for (layer in 0..2) {
                            wavePath.rewind()
                            val points = 60
                            val angleStep = (Math.PI * 2) / points
                            val frequency = 4 + layer
                            val amplitude = 15.dp.toPx() * (1f - age)
                            val phase = (currentTime / 300f) + (layer * 1f)
                            
                            for (i in 0..points) {
                                val angle = i * angleStep
                                val waveOffset = kotlin.math.sin((angle * frequency) + phase).toFloat() * amplitude
                                val r = baseRadius + waveOffset
                                val x = center.x + r * kotlin.math.cos(angle).toFloat()
                                val y = center.y + r * kotlin.math.sin(angle).toFloat()
                                if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                            }
                            wavePath.close()
                            drawPath(wavePath, color, style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                }
            }

            // Glowing Rotating Border
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer { 
                        rotationZ = rotation 
                        scaleX = dialScale
                        scaleY = dialScale
                    }
            ) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        0.0f to Color.Transparent,
                        0.7f to Color.Transparent,
                        0.95f to glowColor,
                        1.0f to Color.Transparent
                    ),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            // Central Circular Button
            Surface(
                onClick = { 
                    onTap()
                    if (!reducedMotion) {
                        shockwaves = shockwaves + System.currentTimeMillis()
                    }
                },
                shape = CircleShape,
                color = containerColor,
                contentColor = contentColor,
                modifier = Modifier
                    .size(190.dp)
                    .graphicsLayer {
                        scaleX = dialScale
                        scaleY = dialScale
                    }
                    .semantics {
                        contentDescription = actionLabel
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = if (isMuted) stringResource(R.string.status_muted) else stringResource(R.string.status_active),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
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


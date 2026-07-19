package com.logicdraftlabs.mute.ui

import android.Manifest
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
import com.logicdraftlabs.mute.ui.theme.SignalRed

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
    ) { /* no-op either way - the persistent notification is a nice-to-have, not required */ }

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

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (!isGranted) {
                PermissionPrompt(
                    onGrant = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        )
                    }
                )
            } else {
                ToggleDial(
                    isMuted = isMuted,
                    onTap = {
                        isMuted = MuteController.toggle(context)
                    }
                )
            }

            Column(
                modifier = Modifier.padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.usage_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                )
                Text(
                    text = stringResource(R.string.settings_link),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clickable {
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        }
                        .padding(12.dp)
                )
            }
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
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SignalRed,
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.permission_grant),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ToggleDial(isMuted: Boolean, onTap: () -> Unit) {
    val reducedMotion = rememberReducedMotion()
    val activeRingColor = MaterialTheme.colorScheme.onBackground
    val transition = updateTransition(targetState = isMuted, label = "dial_transition")
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    val ringColor by transition.animateColor(
        transitionSpec = { if (reducedMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow) },
        label = "dial_ring_color"
    ) { muted -> if (muted) SignalRed else activeRingColor }
    val fillColor by transition.animateColor(
        transitionSpec = { if (reducedMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow) },
        label = "dial_fill_color"
    ) { muted -> if (muted) SignalRed.copy(alpha = 0.10f) else Color.Transparent }
    val dialScale by transition.animateFloat(
        transitionSpec = { if (reducedMotion) snap() else springSpec },
        label = "dial_scale"
    ) { muted -> if (muted) 1.04f else 1f }
    val actionLabel = if (isMuted) stringResource(R.string.hint_muted) else stringResource(R.string.hint_active)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .graphicsLayer {
                    scaleX = dialScale
                    scaleY = dialScale
                }
                .border(width = 1.5.dp, color = ringColor, shape = CircleShape)
                .padding(14.dp)
                .background(
                    color = fillColor,
                    shape = CircleShape
                )
                .semantics {
                    contentDescription = actionLabel
                }
                .clickable(
                    onClickLabel = actionLabel,
                    role = Role.Button,
                    onClick = onTap
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isMuted) stringResource(R.string.status_muted) else stringResource(R.string.status_active),
                style = MaterialTheme.typography.labelLarge,
                color = ringColor,
                fontWeight = FontWeight.Bold
            )
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

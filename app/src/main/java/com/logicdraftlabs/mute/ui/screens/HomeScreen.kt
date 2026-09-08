package com.logicdraftlabs.mute.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.logicdraftlabs.mute.core.MuteController
import com.logicdraftlabs.mute.ui.components.MuteStateLabel
import com.logicdraftlabs.mute.ui.components.MutoHeader
import com.logicdraftlabs.mute.ui.components.MutoToggle
import com.logicdraftlabs.mute.ui.components.StatusCard
import com.logicdraftlabs.mute.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    val onboardingComplete by viewModel.onboardingComplete.collectAsState()
    AnimatedContent(targetState = onboardingComplete, label = "onboarding") { complete ->
        if (complete) MutoHome(viewModel, onNavigateToSettings) else OnboardingScreen(
            onContinue = {
                viewModel.completeOnboarding(context)
                if (!MuteController.isDndAccessGranted(context)) {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                }
            }
        )
    }
}

@Composable
private fun OnboardingScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        MutoHeader()
        Spacer(Modifier.height(40.dp))
        Text("Make silence one tap away.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "MUTO can change sound levels and, if you allow it, control Do Not Disturb. Nothing is uploaded. Your sound preferences stay on this device.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Text("Set up MUTO") }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MutoHome(viewModel: MainViewModel, onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    val isMuted by viewModel.isMuted.collectAsState()
    val dndGranted by viewModel.isDndGranted.collectAsState()
    val muteAlarms by viewModel.muteAlarms.collectAsState()
    val audio = remember(isMuted) { soundSummary(context, muteAlarms) }
    val dndBody = when {
        !dndGranted -> "Permission needed. MUTO will still mute sound levels."
        isMuted -> "On when MUTO needs it. Your previous setting is preserved."
        else -> "MUTO enables it when silencing, if needed."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("M U T O.", fontWeight = FontWeight.Black) },
                actions = { IconButton(onClick = onNavigateToSettings) { Icon(Icons.Outlined.Settings, "Settings") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MutoHeader()
            Spacer(Modifier.height(28.dp))
            MutoToggle(isMuted = isMuted, onToggle = { viewModel.toggleMute(context) })
            Spacer(Modifier.height(20.dp))
            MuteStateLabel(isMuted)
            Spacer(Modifier.height(28.dp))
            SoundStatusCard(audio)
            Spacer(Modifier.height(12.dp))
            StatusCard("DO NOT DISTURB", dndBody, icon = { Icon(Icons.Outlined.DoNotDisturbOn, null, tint = MaterialTheme.colorScheme.primary) })
            if (!dndGranted) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)) }) { Text("Open settings") }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

private data class SoundSummary(val media: String, val ringtone: String, val notifications: String, val alarms: String)

private fun soundSummary(context: Context, muteAlarms: Boolean): SoundSummary {
    val manager = context.getSystemService(AudioManager::class.java)
    fun volume(stream: Int): String {
        val value = manager?.getStreamVolume(stream) ?: 0
        val max = manager?.getStreamMaxVolume(stream) ?: 1
        return "${(value * 100 / max.coerceAtLeast(1))}%"
    }
    return SoundSummary(volume(AudioManager.STREAM_MUSIC), volume(AudioManager.STREAM_RING), volume(AudioManager.STREAM_NOTIFICATION), if (muteAlarms) volume(AudioManager.STREAM_ALARM) else "Kept audible")
}

@Composable
private fun SoundStatusCard(summary: SoundSummary) {
    androidx.compose.material3.Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), shape = MaterialTheme.shapes.extraLarge) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("SOUND", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            SoundLine(Icons.Outlined.MusicNote, "Media", summary.media)
            SoundLine(Icons.Outlined.Phone, "Ringtone", summary.ringtone)
            SoundLine(Icons.Outlined.Notifications, "Notifications", summary.notifications)
            SoundLine(Icons.Outlined.Alarm, "Alarms", summary.alarms)
        }
    }
}

@Composable
private fun SoundLine(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, modifier = Modifier.padding(end = 12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

package com.logicdraftlabs.mute.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.logicdraftlabs.mute.ui.components.ExpressiveCard
import com.logicdraftlabs.mute.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onNavigateToLookAndFeel: () -> Unit,
    onNavigateToSchedules: () -> Unit
) {
    val context = LocalContext.current
    val media by viewModel.muteMedia.collectAsState()
    val ringtone by viewModel.muteRingtone.collectAsState()
    val notifications by viewModel.muteNotifications.collectAsState()
    val system by viewModel.muteSystem.collectAsState()
    val alarms by viewModel.muteAlarms.collectAsState()
    val enableDnd by viewModel.enableDnd.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsSection("SOUND") {
                SettingToggle("Mute media", "Music, podcasts, and video", media) { viewModel.setMuteMedia(context, it) }
                SettingToggle("Mute ringtone", "Incoming call ring volume", ringtone) { viewModel.setMuteRingtone(context, it) }
                SettingToggle("Mute notifications", "Notification sounds", notifications) { viewModel.setMuteNotifications(context, it) }
                SettingToggle("Mute system sounds", "Touch and interface sounds", system) { viewModel.setMuteSystem(context, it) }
                SettingToggle("Mute alarms", "Off by default to protect your alarms", alarms) { viewModel.setMuteAlarms(context, it) }
            }
            SettingsSection("DO NOT DISTURB") {
                SettingToggle("Enable DND when muting", "Only changes DND when MUTO needs to", enableDnd) { viewModel.setEnableDnd(context, it) }
                Text("Grant or review access", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                Text("Android controls this permission.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                androidx.compose.material3.OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)) }) { Text("Open DND settings") }
            }
            SettingsSection("SHORTCUTS") {
                Text("Quick Settings tile", style = MaterialTheme.typography.bodyLarge)
                Text("Add MUTO from your device's Quick Settings editor.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Home-screen widget", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                Text("Long press your home screen, then choose Widgets and MUTO.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SettingsSection("SCHEDULES") {
                Text("Scheduled silence", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Create recurring quiet times.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.material3.TextButton(onClick = onNavigateToSchedules) { Text("Manage schedules") }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    .setData(Uri.parse("package:${context.packageName}"))
                            )
                        }
                    ) { Text("Allow precise schedules") }
                }
            }
            SettingsSection("APPEARANCE") {
                Text("Look & Feel", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Theme and dynamic colors", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.material3.TextButton(onClick = onNavigateToLookAndFeel) { Text("Customize") }
            }
            SettingsSection("ABOUT") {
                Text("MUTO", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("One action. Total silence. One action. Everything back.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Version 1.0.1 · Works entirely on your device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        ExpressiveCard { content() }
    }
}

@Composable
private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

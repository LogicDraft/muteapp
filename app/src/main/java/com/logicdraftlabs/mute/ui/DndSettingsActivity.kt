package com.logicdraftlabs.mute.ui

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.core.ScheduleManager
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.ui.theme.MuteTheme
import com.logicdraftlabs.mute.ui.theme.SignalRed
import java.text.DateFormat
import java.util.Calendar

class DndSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent { MuteTheme { DndSettingsScreen(onBack = { finish() }) } }
    }
}

private val autoRestoreOptions = listOf(0, 1, 2, 4, 8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DndSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var excludeAlarm by remember { mutableStateOf(PrefsManager.getExcludeAlarm(context)) }
    var dndLevel by remember { mutableStateOf(PrefsManager.getDndLevel(context)) }
    var autoRestoreHours by remember { mutableStateOf(PrefsManager.getAutoRestoreHours(context)) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_dnd_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Alarm exclusion card
            SettingsCard {
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = SignalRed, modifier = Modifier.size(24.dp))
                    },
                    headlineContent = {
                        Text(stringResource(R.string.setting_exclude_alarm_title), style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text(stringResource(R.string.setting_exclude_alarm_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingContent = {
                        Switch(
                            checked = excludeAlarm,
                            onCheckedChange = { excludeAlarm = it; PrefsManager.setExcludeAlarm(context, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SignalRed, checkedTrackColor = SignalRed.copy(alpha = 0.35f))
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // DND Level card
            SettingsCard {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                        Icon(Icons.Default.VolumeOff, contentDescription = null, tint = SignalRed, modifier = Modifier.size(24.dp).padding(end = 0.dp))
                        Text(
                            stringResource(R.string.setting_dnd_level_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ChoiceChip(
                            label = stringResource(R.string.setting_dnd_total_silence),
                            isSelected = dndLevel == PrefsManager.DndLevel.TOTAL_SILENCE,
                            onClick = { dndLevel = PrefsManager.DndLevel.TOTAL_SILENCE; PrefsManager.setDndLevel(context, PrefsManager.DndLevel.TOTAL_SILENCE) },
                            modifier = Modifier.weight(1f)
                        )
                        ChoiceChip(
                            label = stringResource(R.string.setting_dnd_priority),
                            isSelected = dndLevel == PrefsManager.DndLevel.PRIORITY_ONLY,
                            onClick = { dndLevel = PrefsManager.DndLevel.PRIORITY_ONLY; PrefsManager.setDndLevel(context, PrefsManager.DndLevel.PRIORITY_ONLY) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = if (dndLevel == PrefsManager.DndLevel.TOTAL_SILENCE)
                            stringResource(R.string.setting_dnd_total_silence_desc) + ". This also overrides \"Keep alarm audible\" — pick Priority Only if alarms must ring."
                        else stringResource(R.string.setting_dnd_priority_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Auto-restore card
            SettingsCard {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = SignalRed, modifier = Modifier.size(24.dp))
                        Text(stringResource(R.string.setting_auto_restore_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp))
                    }
                    Text(stringResource(R.string.setting_auto_restore_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        autoRestoreOptions.forEach { h ->
                            ChoiceChip(
                                label = if (h == 0) stringResource(R.string.setting_auto_restore_off) else "${h}h",
                                isSelected = autoRestoreHours == h,
                                onClick = { autoRestoreHours = h; PrefsManager.setAutoRestoreHours(context, h) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        content()
    }
}

@Composable
private fun ChoiceChip(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor = if (isSelected) SignalRed else MaterialTheme.colorScheme.outline
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) SignalRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) SignalRed else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}

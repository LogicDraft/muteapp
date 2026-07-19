package com.logicdraftlabs.mute.ui

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.ui.theme.MuteTheme
import com.logicdraftlabs.mute.ui.theme.SignalRed

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            MuteTheme {
                SettingsScreen(onBack = { finish() })
            }
        }
    }
}

private val autoRestoreOptions = listOf(0, 1, 2, 4, 8)

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var excludeAlarm by remember { mutableStateOf(PrefsManager.getExcludeAlarm(context)) }
    var dndLevel by remember { mutableStateOf(PrefsManager.getDndLevel(context)) }
    var autoRestoreHours by remember { mutableStateOf(PrefsManager.getAutoRestoreHours(context)) }
    var showNotification by remember { mutableStateOf(PrefsManager.getShowPersistentNotification(context)) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Text(
                        text = "\u2039",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                SettingSwitchRow(
                    title = stringResource(R.string.setting_exclude_alarm_title),
                    description = stringResource(R.string.setting_exclude_alarm_desc),
                    checked = excludeAlarm,
                    onCheckedChange = {
                        excludeAlarm = it
                        PrefsManager.setExcludeAlarm(context, it)
                    }
                )

                SectionDivider()

                Text(
                    text = stringResource(R.string.setting_dnd_level_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                )
                DndLevelChoice(
                    selected = dndLevel,
                    onSelect = {
                        dndLevel = it
                        PrefsManager.setDndLevel(context, it)
                    }
                )
                Text(
                    text = if (dndLevel == PrefsManager.DndLevel.TOTAL_SILENCE) {
                        stringResource(R.string.setting_dnd_total_silence_desc) +
                            ". This also overrides \u201cKeep alarm audible\u201d above \u2014 pick Priority Only if alarms must ring."
                    } else {
                        stringResource(R.string.setting_dnd_priority_desc)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                SectionDivider()

                Text(
                    text = stringResource(R.string.setting_auto_restore_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.setting_auto_restore_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                AutoRestoreChoice(
                    selectedHours = autoRestoreHours,
                    onSelect = {
                        autoRestoreHours = it
                        PrefsManager.setAutoRestoreHours(context, it)
                    }
                )

                SectionDivider()

                SettingSwitchRow(
                    title = stringResource(R.string.setting_notification_title),
                    description = stringResource(R.string.setting_notification_desc),
                    checked = showNotification,
                    onCheckedChange = {
                        showNotification = it
                        PrefsManager.setShowPersistentNotification(context, it)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, end = 8.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SignalRed,
                checkedTrackColor = SignalRed.copy(alpha = 0.35f)
            )
        )
    }
}

@Composable
private fun DndLevelChoice(
    selected: PrefsManager.DndLevel,
    onSelect: (PrefsManager.DndLevel) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ChoiceChip(
            label = stringResource(R.string.setting_dnd_total_silence),
            isSelected = selected == PrefsManager.DndLevel.TOTAL_SILENCE,
            onClick = { onSelect(PrefsManager.DndLevel.TOTAL_SILENCE) },
            modifier = Modifier.weight(1f)
        )
        ChoiceChip(
            label = stringResource(R.string.setting_dnd_priority),
            isSelected = selected == PrefsManager.DndLevel.PRIORITY_ONLY,
            onClick = { onSelect(PrefsManager.DndLevel.PRIORITY_ONLY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AutoRestoreChoice(selectedHours: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        autoRestoreOptions.forEach { hours ->
            ChoiceChip(
                label = if (hours == 0) stringResource(R.string.setting_auto_restore_off) else "${hours}h",
                isSelected = selectedHours == hours,
                onClick = { onSelect(hours) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) SignalRed else MaterialTheme.colorScheme.outline
    val textColor = if (isSelected) SignalRed else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )
    }
}

package com.logicdraftlabs.mute.ui

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.logicdraftlabs.mute.core.ScheduleManager
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.ui.theme.MuteTheme
import com.logicdraftlabs.mute.ui.theme.SignalRed
import java.text.DateFormat
import java.util.Calendar

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT))
        setContent { MuteTheme { SettingsScreen(onBack = { finish() }) } }
    }
}

private val autoRestoreOptions = listOf(0, 1, 2, 4, 8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var excludeAlarm by remember { mutableStateOf(PrefsManager.getExcludeAlarm(context)) }
    var dndLevel by remember { mutableStateOf(PrefsManager.getDndLevel(context)) }
    var autoRestoreHours by remember { mutableStateOf(PrefsManager.getAutoRestoreHours(context)) }
    var showNotification by remember { mutableStateOf(PrefsManager.getShowPersistentNotification(context)) }
    var scheduleEnabled by remember { mutableStateOf(PrefsManager.isScheduleEnabled(context)) }
    var scheduleStart by remember { mutableStateOf(PrefsManager.getScheduleStartMinutes(context)) }
    var scheduleEnd by remember { mutableStateOf(PrefsManager.getScheduleEndMinutes(context)) }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }
    val scheduleActive = scheduleEnabled && isWithinWindow(scheduleStart, scheduleEnd)

    pickerTarget?.let { target ->
        val initial = if (target == PickerTarget.START) scheduleStart else scheduleEnd
        TimePickerDialog(initial, onDismiss = { pickerTarget = null }, onConfirm = { h, m ->
            val value = h * 60 + m
            if (target == PickerTarget.START) { scheduleStart = value; PrefsManager.setScheduleStartMinutes(context, value) }
            else { scheduleEnd = value; PrefsManager.setScheduleEndMinutes(context, value) }
            ScheduleManager.reschedule(context)
            pickerTarget = null
        })
    }

    Surface(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(start = 4.dp, top = 4.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground) }
                Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            }
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp)) {
                SettingSwitchRow(stringResource(R.string.setting_exclude_alarm_title), stringResource(R.string.setting_exclude_alarm_desc), excludeAlarm) { excludeAlarm = it; PrefsManager.setExcludeAlarm(context, it) }
                SectionDivider()
                Text(stringResource(R.string.setting_dnd_level_title), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp, bottom = 10.dp))
                DndLevelChoice(dndLevel) { dndLevel = it; PrefsManager.setDndLevel(context, it) }
                Text(if (dndLevel == PrefsManager.DndLevel.TOTAL_SILENCE) stringResource(R.string.setting_dnd_total_silence_desc) + ". This also overrides “Keep alarm audible” above — pick Priority Only if alarms must ring." else stringResource(R.string.setting_dnd_priority_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                SectionDivider()
                Text(stringResource(R.string.setting_auto_restore_title), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                Text(stringResource(R.string.setting_auto_restore_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp))
                AutoRestoreChoice(autoRestoreHours) { autoRestoreHours = it; PrefsManager.setAutoRestoreHours(context, it) }
                SectionDivider()
                Text(stringResource(R.string.setting_schedule_title), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                Text(if (scheduleActive) stringResource(R.string.setting_schedule_active) else if (scheduleEnabled) stringResource(R.string.setting_schedule_configured) else stringResource(R.string.setting_schedule_desc), style = MaterialTheme.typography.bodyMedium, color = if (scheduleActive) SignalRed else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                SettingSwitchRow(stringResource(R.string.setting_schedule_enabled), stringResource(R.string.setting_schedule_daily), scheduleEnabled) { scheduleEnabled = it; PrefsManager.setScheduleEnabled(context, it); ScheduleManager.reschedule(context) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ChoiceChip(stringResource(R.string.setting_schedule_start, formatTime(context, scheduleStart)), false, { pickerTarget = PickerTarget.START }, Modifier.weight(1f))
                    ChoiceChip(stringResource(R.string.setting_schedule_end, formatTime(context, scheduleEnd)), false, { pickerTarget = PickerTarget.END }, Modifier.weight(1f))
                }
                SectionDivider()
                SettingSwitchRow(stringResource(R.string.setting_notification_title), stringResource(R.string.setting_notification_desc), showNotification) { showNotification = it; PrefsManager.setShowPersistentNotification(context, it) }
                SectionDivider()
                Text(stringResource(R.string.setting_developer_title), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                Text(stringResource(R.string.setting_developer_tagline), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceChip(stringResource(R.string.setting_developer_email), false, { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:logicdraftlabs@gmail.com"))) }, Modifier.weight(1f))
                    ChoiceChip(stringResource(R.string.setting_developer_github), false, { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LogicDraft"))) }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private enum class PickerTarget { START, END }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initialMinutes: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = initialMinutes / 60, initialMinute = initialMinutes % 60, is24Hour = android.text.format.DateFormat.is24HourFormat(LocalContext.current))
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.setting_schedule_pick_time)) }, text = { TimePicker(state) }, confirmButton = { TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text(stringResource(R.string.dialog_ok)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } })
}

private fun formatTime(context: android.content.Context, minutes: Int): String {
    val c = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, minutes / 60); set(Calendar.MINUTE, minutes % 60) }
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
}

private fun isWithinWindow(start: Int, end: Int): Boolean {
    val now = Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
    return if (start == end) false else if (start < end) now in start until end else now >= start || now < end
}

@Composable private fun SectionDivider() { Spacer(Modifier.fillMaxWidth().padding(top = 16.dp).height(1.dp).background(MaterialTheme.colorScheme.outline)) }

@Composable private fun SettingSwitchRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground); Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp, end = 8.dp)) }
        Switch(checked, onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = SignalRed, checkedTrackColor = SignalRed.copy(alpha = .35f)))
    }
}

@Composable private fun DndLevelChoice(selected: PrefsManager.DndLevel, onSelect: (PrefsManager.DndLevel) -> Unit) { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { ChoiceChip(stringResource(R.string.setting_dnd_total_silence), selected == PrefsManager.DndLevel.TOTAL_SILENCE, { onSelect(PrefsManager.DndLevel.TOTAL_SILENCE) }, Modifier.weight(1f)); ChoiceChip(stringResource(R.string.setting_dnd_priority), selected == PrefsManager.DndLevel.PRIORITY_ONLY, { onSelect(PrefsManager.DndLevel.PRIORITY_ONLY) }, Modifier.weight(1f)) } }
@Composable private fun AutoRestoreChoice(selectedHours: Int, onSelect: (Int) -> Unit) { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { autoRestoreOptions.forEach { h -> ChoiceChip(if (h == 0) stringResource(R.string.setting_auto_restore_off) else "${h}h", selectedHours == h, { onSelect(h) }, Modifier.weight(1f)) } } }
@Composable private fun ChoiceChip(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) { val borderColor = if (isSelected) SignalRed else MaterialTheme.colorScheme.outline; Surface(modifier.heightIn(min = 48.dp).clickable(onClick = onClick), shape = RoundedCornerShape(2.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, borderColor)) { Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) SignalRed else MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) } }


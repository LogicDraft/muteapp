package com.logicdraftlabs.mute.ui

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.core.ScheduleManager
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.data.Schedule
import com.logicdraftlabs.mute.ui.theme.MuteTheme

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ScheduleEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        
        val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID)
        
        setContent {
            MuteTheme {
                ScheduleEditScreen(
                    scheduleId = scheduleId,
                    onFinish = { deletedId ->
                        if (deletedId != null) {
                            setResult(RESULT_OK, Intent().putExtra(EXTRA_DELETED_ID, deletedId))
                        } else {
                            setResult(RESULT_OK)
                        }
                        finish()
                    }
                )
            }
        }
    }
    
    companion object {
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_DELETED_ID = "extra_deleted_id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleEditScreen(scheduleId: String?, onFinish: (String?) -> Unit) {
    val context = LocalContext.current
    val isEditing = scheduleId != null
    
    // Load existing or set defaults
    val existingSchedules = remember { PrefsManager.getSchedules(context) }
    val existing = existingSchedules.find { it.id == scheduleId }
    
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var startMinutes by remember { mutableStateOf(existing?.startMinuteOfDay ?: (22 * 60)) }
    var endMinutes by remember { mutableStateOf(existing?.endMinuteOfDay ?: (7 * 60)) }
    var selectedDays by remember { 
        mutableStateOf(existing?.days ?: setOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY)) 
    }
    var dndLevel by remember { mutableStateOf(existing?.dndLevel ?: PrefsManager.getDndLevel(context)) }
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var timePickerTarget by remember { mutableStateOf<String?>(null) } // "start" or "end"
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.schedule_add)) },
                navigationIcon = {
                    IconButton(onClick = { onFinish(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.schedule_delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(stringResource(R.string.schedule_label_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            // Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimeCard(
                    title = "Start",
                    minutes = startMinutes,
                    modifier = Modifier.weight(1f),
                    onClick = { timePickerTarget = "start" }
                )
                TimeCard(
                    title = "End",
                    minutes = endMinutes,
                    modifier = Modifier.weight(1f),
                    onClick = { timePickerTarget = "end" }
                )
            }
            
            // Days
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Days",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val daysOfWeek = listOf(
                        Calendar.SUNDAY to "S", Calendar.MONDAY to "M", Calendar.TUESDAY to "T",
                        Calendar.WEDNESDAY to "W", Calendar.THURSDAY to "T", Calendar.FRIDAY to "F", Calendar.SATURDAY to "S"
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeek.forEach { (calDay, letter) ->
                            val isSelected = selectedDays.contains(calDay)
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        val newDays = selectedDays.toMutableSet()
                                        if (isSelected) newDays.remove(calDay) else newDays.add(calDay)
                                        selectedDays = newDays
                                    },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = letter, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip("Every day", selectedDays.size == 7) {
                            selectedDays = setOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY)
                        }
                        PresetChip("Weekdays", selectedDays.size == 5 && !selectedDays.contains(Calendar.SUNDAY) && !selectedDays.contains(Calendar.SATURDAY)) {
                            selectedDays = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)
                        }
                        PresetChip("Weekends", selectedDays.size == 2 && selectedDays.contains(Calendar.SUNDAY) && selectedDays.contains(Calendar.SATURDAY)) {
                            selectedDays = setOf(Calendar.SUNDAY, Calendar.SATURDAY)
                        }
                    }
                }
            }
            
            // DND Level (followup §1)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.schedule_dnd_level),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LevelChip(
                            label = stringResource(R.string.setting_dnd_total_silence),
                            selected = dndLevel == PrefsManager.DndLevel.TOTAL_SILENCE,
                            onClick = { dndLevel = PrefsManager.DndLevel.TOTAL_SILENCE },
                            modifier = Modifier.weight(1f)
                        )
                        LevelChip(
                            label = stringResource(R.string.setting_dnd_priority),
                            selected = dndLevel == PrefsManager.DndLevel.PRIORITY_ONLY,
                            onClick = { dndLevel = PrefsManager.DndLevel.PRIORITY_ONLY },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val finalLabel = if (label.isBlank()) "Schedule" else label
                    val newSchedule = Schedule(
                        id = scheduleId ?: java.util.UUID.randomUUID().toString(),
                        label = finalLabel,
                        startMinuteOfDay = startMinutes,
                        endMinuteOfDay = endMinutes,
                        days = selectedDays,
                        enabled = existing?.enabled ?: true,
                        dndLevel = dndLevel
                    )
                    
                    val updated = existingSchedules.toMutableList()
                    val index = updated.indexOfFirst { it.id == newSchedule.id }
                    if (index != -1) updated[index] = newSchedule else updated.add(newSchedule)
                    
                    PrefsManager.saveSchedules(context, updated)
                    ScheduleManager.reschedule(context)
                    
                    onFinish(null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save", style = MaterialTheme.typography.titleMedium)
            }
        }
        
        // Time Picker Dialog
        if (timePickerTarget != null) {
            val isStart = timePickerTarget == "start"
            val initialMinutes = if (isStart) startMinutes else endMinutes
            val timePickerState = rememberTimePickerState(
                initialHour = initialMinutes / 60,
                initialMinute = initialMinutes % 60,
                is24Hour = android.text.format.DateFormat.is24HourFormat(context)
            )
            
            AlertDialog(
                onDismissRequest = { timePickerTarget = null },
                confirmButton = {
                    TextButton(onClick = {
                        val mins = timePickerState.hour * 60 + timePickerState.minute
                        if (isStart) startMinutes = mins else endMinutes = mins
                        timePickerTarget = null
                    }) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { timePickerTarget = null }) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
        
        // Delete Confirm Dialog
        if (showDeleteConfirm && scheduleId != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(stringResource(R.string.schedule_delete_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val updated = existingSchedules.filter { it.id != scheduleId }
                            PrefsManager.saveSchedules(context, updated)
                            ScheduleManager.reschedule(context)
                            showDeleteConfirm = false
                            onFinish(scheduleId)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun TimeCard(title: String, minutes: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val time = LocalTime.of(minutes / 60, minutes % 60)
    
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = time.format(formatter), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun PresetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun LevelChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

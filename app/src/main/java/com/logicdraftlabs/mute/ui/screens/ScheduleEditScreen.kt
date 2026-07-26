package com.logicdraftlabs.mute.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.data.Schedule
import com.logicdraftlabs.mute.ui.components.DayPickerPills
import com.logicdraftlabs.mute.ui.components.ExpressiveCard
import com.logicdraftlabs.mute.ui.viewmodel.MainViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditScreen(
    viewModel: MainViewModel,
    scheduleId: String?,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val schedules by viewModel.schedules.collectAsState()

    val existingSchedule = remember(schedules, scheduleId) {
        schedules.find { it.id == scheduleId }
    }

    var label by remember { mutableStateOf(existingSchedule?.label ?: "Quiet Time") }
    var startMinute by remember { mutableIntStateOf(existingSchedule?.startMinuteOfDay ?: 1320) } // Default 10:00 PM
    var endMinute by remember { mutableIntStateOf(existingSchedule?.endMinuteOfDay ?: 420) }     // Default 7:00 AM
    var selectedDays by remember {
        mutableStateOf(
            existingSchedule?.days ?: setOf(
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
            )
        )
    }
    var overrideDnd by remember { mutableStateOf(existingSchedule?.dndLevelOverride != null) }
    var dndOverrideLevel by remember {
        mutableStateOf(existingSchedule?.dndLevelOverride ?: PrefsManager.DndLevel.TOTAL_SILENCE)
    }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (existingSchedule != null) "Edit Schedule" else "New Schedule",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (existingSchedule != null) {
                        IconButton(onClick = {
                            viewModel.deleteSchedule(context, existingSchedule.id)
                            onSaveSuccess()
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Schedule Name Card
            ExpressiveCard {
                Text(
                    text = "Schedule Name",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    placeholder = { Text("e.g. Work, Sleep, Focus") },
                    leadingIcon = { Icon(Icons.Outlined.Label, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Time Selector Card
            ExpressiveCard {
                Text(
                    text = "Quiet Hours",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Time Box
                    TimeTile(
                        title = "Start Time",
                        minuteOfDay = startMinute,
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f)
                    )

                    // End Time Box
                    TimeTile(
                        title = "End Time",
                        minuteOfDay = endMinute,
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Days Selection Card
            ExpressiveCard {
                Text(
                    text = "Repeat Days",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                DayPickerPills(
                    selectedDays = selectedDays,
                    onDayToggle = { day ->
                        selectedDays = if (selectedDays.contains(day)) {
                            if (selectedDays.size > 1) selectedDays - day else selectedDays
                        } else {
                            selectedDays + day
                        }
                    }
                )
            }

            // DND Override Option
            ExpressiveCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Custom DND Level",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Override app default during this schedule",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = overrideDnd,
                        onCheckedChange = { overrideDnd = it }
                    )
                }

                if (overrideDnd) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = dndOverrideLevel == PrefsManager.DndLevel.TOTAL_SILENCE,
                            onClick = { dndOverrideLevel = PrefsManager.DndLevel.TOTAL_SILENCE },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Total Silence")
                        }
                        SegmentedButton(
                            selected = dndOverrideLevel == PrefsManager.DndLevel.PRIORITY_ONLY,
                            onClick = { dndOverrideLevel = PrefsManager.DndLevel.PRIORITY_ONLY },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Priority Only")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    val finalSchedule = Schedule(
                        id = existingSchedule?.id ?: UUID.randomUUID().toString(),
                        label = label.ifBlank { "Quiet Time" },
                        startMinuteOfDay = startMinute,
                        endMinuteOfDay = endMinute,
                        days = selectedDays,
                        enabled = existingSchedule?.enabled ?: true,
                        dndLevelOverride = if (overrideDnd) dndOverrideLevel else null
                    )
                    viewModel.saveSchedule(context, finalSchedule)
                    onSaveSuccess()
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (existingSchedule != null) "Update Schedule" else "Save Schedule",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    // Material 3 Time Picker Dialogs
    if (showStartTimePicker) {
        M3TimePickerDialog(
            initialMinuteOfDay = startMinute,
            onDismiss = { showStartTimePicker = false },
            onTimeSelected = { startMinute = it }
        )
    }

    if (showEndTimePicker) {
        M3TimePickerDialog(
            initialMinuteOfDay = endMinute,
            onDismiss = { showEndTimePicker = false },
            onTimeSelected = { endMinute = it }
        )
    }
}

@Composable
private fun TimeTile(
    title: String,
    minuteOfDay: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val time = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = time.format(formatter),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun M3TimePickerDialog(
    initialMinuteOfDay: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int) -> Unit
) {
    val initialHour = initialMinuteOfDay / 60
    val initialMinute = initialMinuteOfDay % 60
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedMinuteOfDay = (timePickerState.hour * 60) + timePickerState.minute
                onTimeSelected(selectedMinuteOfDay)
                onDismiss()
            }) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

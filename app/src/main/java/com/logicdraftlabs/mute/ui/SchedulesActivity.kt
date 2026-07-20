package com.logicdraftlabs.mute.ui

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.logicdraftlabs.mute.ui.theme.SignalRed
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import java.util.Calendar

class SchedulesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            MuteTheme {
                SchedulesScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchedulesScreen() {
    val context = LocalContext.current
    var schedules by remember { mutableStateOf(PrefsManager.getSchedules(context)) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val undoMessage = stringResource(R.string.schedule_deleted)
    val undoActionLabel = stringResource(R.string.schedule_undo)

    val editLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val updated = PrefsManager.getSchedules(context)
        
        // Check if something was deleted (it would be in the old list but not the new list)
        val deletedScheduleId = result.data?.getStringExtra(ScheduleEditActivity.EXTRA_DELETED_ID)
        val deletedSchedule = schedules.find { it.id == deletedScheduleId }
        
        schedules = updated

        if (deletedSchedule != null) {
            scope.launch {
                val snackbarResult = snackbarHostState.showSnackbar(
                    message = undoMessage,
                    actionLabel = undoActionLabel,
                    duration = SnackbarDuration.Short
                )
                if (snackbarResult == SnackbarResult.ActionPerformed) {
                    // Undo the deletion
                    val currentSchedules = PrefsManager.getSchedules(context).toMutableList()
                    currentSchedules.add(deletedSchedule)
                    PrefsManager.saveSchedules(context, currentSchedules)
                    ScheduleManager.reschedule(context)
                    schedules = PrefsManager.getSchedules(context)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.schedules_title)) },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editLauncher.launch(Intent(context, ScheduleEditActivity::class.java))
                },
                containerColor = SignalRed,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.schedule_add))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.schedule_empty_state),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(schedules, key = { it.id }) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        onClick = {
                            val intent = Intent(context, ScheduleEditActivity::class.java)
                                .putExtra(ScheduleEditActivity.EXTRA_SCHEDULE_ID, schedule.id)
                            editLauncher.launch(intent)
                        },
                        onToggle = { enabled ->
                            val currentSchedules = PrefsManager.getSchedules(context).toMutableList()
                            val index = currentSchedules.indexOfFirst { it.id == schedule.id }
                            if (index != -1) {
                                currentSchedules[index] = currentSchedules[index].copy(enabled = enabled)
                                PrefsManager.saveSchedules(context, currentSchedules)
                                ScheduleManager.reschedule(context)
                                schedules = currentSchedules
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule: Schedule,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val startTime = LocalTime.of(schedule.startMinuteOfDay / 60, schedule.startMinuteOfDay % 60)
    val endTime = LocalTime.of(schedule.endMinuteOfDay / 60, schedule.endMinuteOfDay % 60)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${startTime.format(timeFormatter)} – ${endTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatDays(schedule.days),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = schedule.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SignalRed,
                    checkedTrackColor = SignalRed.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun formatDays(days: Set<Int>): String {
    if (days.size == 7) return stringResource(R.string.schedule_days_every)
    if (days.size == 5 && !days.contains(Calendar.SATURDAY) && !days.contains(Calendar.SUNDAY)) {
        return stringResource(R.string.schedule_days_weekdays)
    }
    if (days.size == 2 && days.contains(Calendar.SATURDAY) && days.contains(Calendar.SUNDAY)) {
        return stringResource(R.string.schedule_days_weekends)
    }
    
    // Sort and join short names
    val sorted = days.sorted()
    val names = sorted.map {
        when (it) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }
    }
    return names.joinToString(", ")
}

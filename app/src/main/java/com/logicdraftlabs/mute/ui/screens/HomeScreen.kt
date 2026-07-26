package com.logicdraftlabs.mute.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.logicdraftlabs.mute.R
import com.logicdraftlabs.mute.core.ScheduleManager
import com.logicdraftlabs.mute.data.PrefsManager
import com.logicdraftlabs.mute.ui.components.ExpressiveCard
import com.logicdraftlabs.mute.ui.components.RingToDiscHeroButton
import com.logicdraftlabs.mute.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSchedules: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val isMuted by viewModel.isMuted.collectAsState()
    val isDndGranted by viewModel.isDndGranted.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val dndLevel by viewModel.dndLevel.collectAsState()
    val autoRestoreHours by viewModel.autoRestoreHours.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isDndGranted) {
                PermissionCard(
                    onGrantClick = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Hero Ring-to-Disc Dial Button
            RingToDiscHeroButton(
                isMuted = isMuted,
                onTap = {
                    viewModel.toggleMute(context)
                },
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Hint Card
            StatusHintCard(
                isMuted = isMuted,
                context = context,
                schedules = schedules
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Action Grid (Material 3 Expressive Containers)
            Text(
                text = "Quick Controls",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Schedules Quick Action Card
                ExpressiveCard(
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSchedules
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Schedules",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${schedules.count { it.enabled }} active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // DND Mode Quick Action Card
                ExpressiveCard(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val newLevel = if (dndLevel == PrefsManager.DndLevel.TOTAL_SILENCE) {
                            PrefsManager.DndLevel.PRIORITY_ONLY
                        } else {
                            PrefsManager.DndLevel.TOTAL_SILENCE
                        }
                        viewModel.setDndLevel(context, newLevel)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DoNotDisturbOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "DND Mode",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (dndLevel == PrefsManager.DndLevel.TOTAL_SILENCE) "Total Silence" else "Priority Only",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-Restore Quick Timer Card
            ExpressiveCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val nextHours = when (autoRestoreHours) {
                        0 -> 1
                        1 -> 2
                        2 -> 4
                        4 -> 8
                        else -> 0
                    }
                    viewModel.setAutoRestoreHours(context, nextHours)
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Restore Safety Net",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (autoRestoreHours > 0) "Unmute after $autoRestoreHours hour(s)" else "Off (Stay muted indefinitely)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionCard(onGrantClick: () -> Unit) {
    ExpressiveCard(
        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.permission_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.permission_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.permission_grant),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatusHintCard(
    isMuted: Boolean,
    context: Context,
    schedules: List<com.logicdraftlabs.mute.data.Schedule>
) {
    val enabledSchedules = remember(schedules) { schedules.filter { it.enabled } }

    val hintText = remember(enabledSchedules, isMuted) {
        val now = System.currentTimeMillis()
        val allWindows = enabledSchedules.flatMap {
            ScheduleManager.getNextWindows(it).map { w -> it to w }
        }

        val currentSource = PrefsManager.getMuteSource(context)

        if (isMuted && currentSource is PrefsManager.MuteSource.Scheduled) {
            val activeSchedule = enabledSchedules.find { it.id == currentSource.scheduleId }
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
                "soon"
            }
            return@remember "Next: ${nextWindow.first.label} $dayStr at ${formatter.format(startCal.time)}"
        }

        return@remember context.getString(R.string.usage_hint)
    }

    ExpressiveCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp,
                contentDescription = null,
                tint = if (isMuted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = hintText,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

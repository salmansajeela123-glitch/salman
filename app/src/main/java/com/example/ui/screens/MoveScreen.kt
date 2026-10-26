package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ActivityLogEntity
import com.example.data.model.DateKeyUtils
import com.example.data.model.MovementRoutine
import com.example.data.model.SleepLogEntity
import com.example.data.model.WellnessCatalog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SafetyNoticeBanner
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.WellnessUiState
import kotlinx.coroutines.delay

@Composable
fun MoveScreen(
    uiState: WellnessUiState,
    initialTab: Int = 0,
    onLogActivity: (type: String, title: String, durationMinutes: Int, intensity: String, notes: String) -> Unit,
    onDeleteActivity: (Long) -> Unit,
    onAdjustHydration: (Int) -> Unit,
    onUpdateHydrationGoal: (Int) -> Unit,
    onLogSleep: (bedTime: String, wakeTime: String, approxHours: Float, quality: Int, notes: String) -> Unit,
    onDeleteSleep: (Long) -> Unit
) {
    var selectedTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    val tabs = listOf("Routines & Timer", "Activity Log", "Sleep & Water")

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = stringResource(R.string.move_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gentle movement, hydration, and rest routines — no extreme rules.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(label) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("move_tab_$idx")
                    )
                }
            }

            when (selectedTab) {
                0 -> RoutinesAndTimerSection(
                    onCompleteRoutine = { routine, minutesSpent ->
                        onLogActivity(
                            routine.category,
                            routine.title,
                            minutesSpent.coerceAtLeast(1),
                            "Gentle",
                            "Completed guided routine"
                        )
                    }
                )

                1 -> ActivityLogSection(
                    logs = uiState.activityLogs,
                    onSaveActivity = onLogActivity,
                    onDeleteActivity = onDeleteActivity
                )

                else -> SleepAndHydrationSection(
                    todayGlasses = uiState.todayHydrationGlasses,
                    hydrationGoal = uiState.preferences.hydrationGoalGlasses,
                    sleepLogs = uiState.sleepLogs,
                    onAdjustHydration = onAdjustHydration,
                    onUpdateHydrationGoal = onUpdateHydrationGoal,
                    onLogSleep = onLogSleep,
                    onDeleteSleep = onDeleteSleep
                )
            }
        }
    }
}

@Composable
private fun RoutinesAndTimerSection(
    onCompleteRoutine: (MovementRoutine, Int) -> Unit
) {
    var activeRoutineId by rememberSaveable { mutableStateOf(WellnessCatalog.movementRoutines.first().id) }
    var isTimerRunning by rememberSaveable { mutableStateOf(false) }
    var isRestInterval by rememberSaveable { mutableStateOf(false) }
    var restSecondsRemaining by rememberSaveable { mutableIntStateOf(0) }
    var elapsedRoutineSec by rememberSaveable { mutableIntStateOf(0) }

    val selectedRoutine = WellnessCatalog.movementRoutines.find { it.id == activeRoutineId }
        ?: WellnessCatalog.movementRoutines.first()
    val totalRoutineSeconds = selectedRoutine.steps.sumOf { it.durationSeconds }

    LaunchedEffect(isTimerRunning, isRestInterval, restSecondsRemaining, totalRoutineSeconds) {
        while (isTimerRunning) {
            delay(1000L)
            if (isRestInterval) {
                if (restSecondsRemaining > 1) {
                    restSecondsRemaining -= 1
                } else {
                    restSecondsRemaining = 0
                    isRestInterval = false
                }
            } else {
                elapsedRoutineSec += 1
                if (elapsedRoutineSec >= totalRoutineSeconds) {
                    isTimerRunning = false
                    onCompleteRoutine(selectedRoutine, (elapsedRoutineSec + 30) / 60)
                    elapsedRoutineSec = 0
                }
            }
        }
    }

    // Determine active step from elapsedRoutineSec
    var acc = 0
    var activeStepIdx = 0
    for (i in selectedRoutine.steps.indices) {
        acc += selectedRoutine.steps[i].durationSeconds
        if (elapsedRoutineSec < acc) {
            activeStepIdx = i
            break
        }
    }
    val currentStep = selectedRoutine.steps[activeStepIdx]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SafetyNoticeBanner(
                text = stringResource(R.string.move_safety_banner),
                icon = Icons.Outlined.HealthAndSafety
            )
        }

        // Active Workout Timer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = selectedRoutine.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = selectedRoutine.levelLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    val displaySec = if (isRestInterval) {
                        restSecondsRemaining
                    } else {
                        (totalRoutineSeconds - elapsedRoutineSec).coerceAtLeast(0)
                    }
                    Text(
                        text = if (isRestInterval) {
                            "Rest Interval • ${displaySec}s"
                        } else {
                            String.format("%d:%02d remaining", displaySec / 60, displaySec % 60)
                        },
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("workout_timer_display")
                    )

                    LinearProgressIndicator(
                        progress = {
                            if (totalRoutineSeconds > 0) {
                                (elapsedRoutineSec.toFloat() / totalRoutineSeconds.toFloat()).coerceIn(0f, 1f)
                            } else 0f
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Current: ${currentStep.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currentStep.instruction, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Low-impact option: ${currentStep.easierAlternative}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isTimerRunning = !isTimerRunning },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("workout_start_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                when {
                                    isTimerRunning -> "Pause"
                                    elapsedRoutineSec > 0 -> "Resume"
                                    else -> "Start"
                                }
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                isRestInterval = true
                                restSecondsRemaining = 30
                                isTimerRunning = true
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("workout_rest_button")
                        ) {
                            Text("+30s Rest")
                        }

                        if (elapsedRoutineSec > 0) {
                            FilledTonalButton(
                                onClick = {
                                    val mins = ((elapsedRoutineSec + 59) / 60).coerceAtLeast(1)
                                    isTimerRunning = false
                                    isRestInterval = false
                                    elapsedRoutineSec = 0
                                    onCompleteRoutine(selectedRoutine, mins)
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("workout_finish_button")
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Finish")
                            }
                        }
                    }
                }
            }
        }

        // Warm-up & Cool-down details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Warm-Up", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text(selectedRoutine.warmUp, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Cool-Down", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text(selectedRoutine.coolDown, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            SectionHeader(
                title = "Choose a Beginner-Friendly Plan",
                subtitle = "Tap any plan to load it into the timer above."
            )
        }

        items(WellnessCatalog.movementRoutines, key = { it.id }) { routine ->
            val selected = routine.id == activeRoutineId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        activeRoutineId = routine.id
                        isTimerRunning = false
                        isRestInterval = false
                        elapsedRoutineSec = 0
                    }
                    .testTag("routine_card_${routine.id}"),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(routine.title, style = MaterialTheme.typography.titleMedium)
                        Text("${routine.durationMinutes} min", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(routine.levelLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityLogSection(
    logs: List<ActivityLogEntity>,
    onSaveActivity: (type: String, title: String, durationMinutes: Int, intensity: String, notes: String) -> Unit,
    onDeleteActivity: (Long) -> Unit
) {
    var selectedType by rememberSaveable { mutableStateOf("Walking") }
    var customTitle by rememberSaveable { mutableStateOf("") }
    var durationText by rememberSaveable { mutableStateOf("15") }
    var intensity by rememberSaveable { mutableStateOf("Gentle") }
    var notes by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Log Movement", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "No calorie counting or body metrics — just celebrating time spent moving.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Walking", "Stretching", "Mobility", "Bodyweight", "Yoga", "Cycling", "Other").forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type) },
                                modifier = Modifier.testTag("activity_type_${type.lowercase()}")
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { raw -> durationText = raw.filter { it.isDigit() }.take(3) },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("activity_duration_input")
                        )
                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { customTitle = it },
                            label = { Text("Label (Optional)") },
                            placeholder = { Text("e.g., Afternoon walk") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.6f)
                                .testTag("activity_title_input")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Gentle", "Moderate", "Energizing").forEach { level ->
                            FilterChip(
                                selected = intensity == level,
                                onClick = { intensity = level },
                                label = { Text(level) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("How did your body feel? (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val parsedMinutes = durationText.toIntOrNull() ?: 0
                    Button(
                        onClick = {
                            onSaveActivity(
                                selectedType,
                                customTitle.ifBlank { selectedType },
                                parsedMinutes,
                                intensity,
                                notes
                            )
                            customTitle = ""
                            notes = ""
                        },
                        enabled = parsedMinutes in 1..360,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_activity_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Activity")
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Recent Movement Logs")
        }

        if (logs.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.DirectionsWalk,
                    title = "No movement logged yet",
                    message = "Even a 5-minute stretch or walk to get fresh air counts."
                )
            }
        } else {
            items(logs.take(15), key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${log.title} • ${log.durationMinutes} min",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${log.activityType} • ${log.intensity} • ${DateKeyUtils.readableDateTime(log.timestamp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (log.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(log.notes, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        IconButton(
                            onClick = { onDeleteActivity(log.id) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete activity log"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepAndHydrationSection(
    todayGlasses: Int,
    hydrationGoal: Int,
    sleepLogs: List<SleepLogEntity>,
    onAdjustHydration: (Int) -> Unit,
    onUpdateHydrationGoal: (Int) -> Unit,
    onLogSleep: (bedTime: String, wakeTime: String, approxHours: Float, quality: Int, notes: String) -> Unit,
    onDeleteSleep: (Long) -> Unit
) {
    var bedTime by rememberSaveable { mutableStateOf("22:30") }
    var wakeTime by rememberSaveable { mutableStateOf("06:45") }
    var hoursText by rememberSaveable { mutableStateOf("7.5") }
    var qualityRating by rememberSaveable { mutableIntStateOf(4) }
    var sleepNotes by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hydration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optional Hydration Log", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = stringResource(R.string.move_hydration_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$todayGlasses / $hydrationGoal glasses today",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onAdjustHydration(-1) },
                                enabled = todayGlasses > 0,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("move_water_minus_button")
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Remove one glass")
                            }
                            Button(
                                onClick = { onAdjustHydration(1) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("move_water_plus_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+1 Glass")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Personal daily goal: $hydrationGoal glasses", style = MaterialTheme.typography.labelLarge)
                        Row {
                            IconButton(
                                onClick = { onUpdateHydrationGoal(hydrationGoal - 1) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Decrease water goal")
                            }
                            IconButton(
                                onClick = { onUpdateHydrationGoal(hydrationGoal + 1) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Increase water goal")
                            }
                        }
                    }
                }
            }
        }

        // Sleep Routine Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Bedtime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sleep & Wind-Down Routine", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = stringResource(R.string.move_sleep_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = bedTime,
                            onValueChange = { bedTime = it.take(10) },
                            label = { Text("Approx. Bedtime") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = wakeTime,
                            onValueChange = { wakeTime = it.take(10) },
                            label = { Text("Approx. Wake") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { raw ->
                            hoursText = raw.filter { it.isDigit() || it == '.' }.take(4)
                        },
                        label = { Text("Approximate hours rested") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sleep_hours_input")
                    )

                    Text("How rested do you feel? (1–5 self-rating)", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..5).forEach { rating ->
                            FilterChip(
                                selected = qualityRating == rating,
                                onClick = { qualityRating = rating },
                                label = { Text("$rating/5") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = sleepNotes,
                        onValueChange = { sleepNotes = it },
                        label = { Text("Wind-down note (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val parsedHours = hoursText.toFloatOrNull() ?: 0f
                    Button(
                        onClick = {
                            onLogSleep(bedTime, wakeTime, parsedHours, qualityRating, sleepNotes)
                            sleepNotes = ""
                        },
                        enabled = parsedHours in 0.5f..20f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_sleep_button")
                    ) {
                        Text("Save Sleep Routine")
                    }
                }
            }
        }

        if (sleepLogs.isNotEmpty()) {
            item { SectionHeader(title = "Recent Sleep Logs") }
            items(sleepLogs.take(8), key = { it.id }) { sl ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "~${sl.approxHours} hrs • Rested ${sl.qualityRating}/5",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${sl.bedTimeText} → ${sl.wakeTimeText} (${sl.dateKey})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onDeleteSleep(sl.id) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete sleep log")
                        }
                    }
                }
            }
        }
    }
}

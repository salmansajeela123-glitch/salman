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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ScreenLockPortrait
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DateKeyUtils
import com.example.data.model.StudySessionEntity
import com.example.data.model.StudyTaskEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SafetyNoticeBanner
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.WellnessUiState

private data class StudyPreset(
    val id: String,
    val label: String,
    val subtitle: String,
    val focusMin: Int,
    val breakMin: Int
)

private val studyPresets = listOf(
    StudyPreset("pomodoro_25", "Pomodoro 25 / 5", "Classic balanced focus + short eye break", 25, 5),
    StudyPreset("deep_50", "Deep Study 50 / 10", "Extended study block + stretch & water break", 50, 10),
    StudyPreset("gentle_15", "Quick Start 15 / 3", "Low-friction sprint when starting feels hard", 15, 3),
    StudyPreset("exam_45", "Exam Block 45 / 5", "Structured practice block without burnout", 45, 5)
)

private val studySubjects = listOf(
    "Deep Study", "Math & Logic", "Coding & CS", "Reading", "Writing", "Science", "Languages", "Exam Prep"
)

@Composable
fun StudyScreen(
    uiState: WellnessUiState,
    initialTab: Int = 0,
    onStartStudySession: (
        linkedTaskId: Long?,
        subject: String,
        taskTitle: String,
        focusMinutes: Int,
        breakMinutes: Int,
        lockPhone: Boolean,
        requestOsPinning: Boolean,
        keepScreenAwake: Boolean
    ) -> Unit,
    onTogglePauseActive: () -> Unit,
    onFinishActive: () -> Unit,
    onAddStudyTask: (subject: String, title: String, plannedMinutes: Int, timeSlot: String) -> Unit,
    onToggleStudyTaskCompleted: (Long, Boolean) -> Unit,
    onDeleteStudyTask: (Long) -> Unit,
    onDeleteStudySession: (Long) -> Unit,
    onUpdateStudyGoalMinutes: (Int) -> Unit
) {
    var selectedTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    val tabs = listOf("Focus & Lock", "Time Planner", "Study Log")

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
                    text = stringResource(R.string.study_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.study_subtitle),
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
                            .testTag("study_tab_$idx")
                    )
                }
            }

            when (selectedTab) {
                0 -> FocusAndPhoneLockTab(
                    uiState = uiState,
                    onStartStudySession = onStartStudySession,
                    onTogglePauseActive = onTogglePauseActive,
                    onFinishActive = onFinishActive,
                    onUpdateStudyGoalMinutes = onUpdateStudyGoalMinutes,
                    onOpenPlannerTab = { selectedTab = 1 }
                )

                1 -> StudyTimePlannerTab(
                    tasks = uiState.studyTasks,
                    todayStudyMinutes = uiState.todayStudyMinutes,
                    dailyStudyGoalMinutes = uiState.preferences.dailyStudyGoalMinutes,
                    onAddStudyTask = onAddStudyTask,
                    onToggleCompleted = onToggleStudyTaskCompleted,
                    onDeleteTask = onDeleteStudyTask,
                    onStartTaskLocked = { task ->
                        onStartStudySession(
                            task.id,
                            task.subject,
                            task.title,
                            task.plannedMinutes,
                            5,
                            true,
                            true,
                            true
                        )
                    }
                )

                else -> StudySessionLogTab(
                    uiState = uiState,
                    sessions = uiState.studySessions,
                    onDeleteSession = onDeleteStudySession
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FocusAndPhoneLockTab(
    uiState: WellnessUiState,
    onStartStudySession: (
        linkedTaskId: Long?,
        subject: String,
        taskTitle: String,
        focusMinutes: Int,
        breakMinutes: Int,
        lockPhone: Boolean,
        requestOsPinning: Boolean,
        keepScreenAwake: Boolean
    ) -> Unit,
    onTogglePauseActive: () -> Unit,
    onFinishActive: () -> Unit,
    onUpdateStudyGoalMinutes: (Int) -> Unit,
    onOpenPlannerTab: () -> Unit
) {
    var selectedPresetId by rememberSaveable { mutableStateOf(studyPresets.first().id) }
    var customFocusText by rememberSaveable { mutableStateOf("25") }
    var customBreakText by rememberSaveable { mutableStateOf("5") }
    var selectedSubject by rememberSaveable { mutableStateOf(studySubjects.first()) }
    var topicText by rememberSaveable { mutableStateOf("") }
    var selectedTaskId by rememberSaveable { mutableStateOf<Long?>(null) }

    var lockPhoneInStudy by rememberSaveable { mutableStateOf(true) }
    var useOsPinning by rememberSaveable { mutableStateOf(true) }
    var keepScreenAwake by rememberSaveable { mutableStateOf(true) }

    val pendingTasks = uiState.studyTasks.filter { !it.isCompleted }
    val activeSession = uiState.activeStudySession
    val goalMinutes = uiState.preferences.dailyStudyGoalMinutes

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("study_focus_list"),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Study Time Budget Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S STUDY BUDGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${uiState.todayStudyMinutes} / $goalMinutes min focused",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        FilledTonalButton(
                            onClick = onOpenPlannerTab,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Plan Tasks")
                        }
                    }

                    LinearProgressIndicator(
                        progress = {
                            (uiState.todayStudyMinutes.toFloat() / goalMinutes.toFloat().coerceAtLeast(1f))
                                .coerceIn(0f, 1f)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                    )

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(45, 60, 90, 120, 180).forEach { target ->
                            FilterChip(
                                selected = goalMinutes == target,
                                onClick = { onUpdateStudyGoalMinutes(target) },
                                label = { Text("${target}m goal") }
                            )
                        }
                    }
                }
            }
        }

        // If an unlocked study session is running inline, show controls here
        if (activeSession.isActive && !activeSession.phoneLockEnabled) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (activeSession.isBreakPhase) "Break Active" else "Study Timer Running (Unlocked)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${activeSession.subject} • ${activeSession.taskTitle}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = String.format(
                                "%02d:%02d",
                                activeSession.remainingSeconds / 60,
                                activeSession.remainingSeconds % 60
                            ),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = onTogglePauseActive) {
                                Icon(
                                    if (activeSession.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (activeSession.isPaused) "Resume" else "Pause")
                            }
                            OutlinedButton(onClick = onFinishActive) {
                                Icon(Icons.Filled.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Finish & Log")
                            }
                        }
                    }
                }
            }
        }

        // Study Block Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("1. Choose Study Rhythm", style = MaterialTheme.typography.titleLarge)

                    studyPresets.forEach { preset ->
                        val isSelected = selectedPresetId == preset.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPresetId = preset.id
                                    customFocusText = preset.focusMin.toString()
                                    customBreakText = preset.breakMin.toString()
                                }
                                .testTag("study_preset_${preset.id}"),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(preset.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(preset.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${preset.focusMin}m", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = customFocusText,
                            onValueChange = { raw ->
                                customFocusText = raw.filter { it.isDigit() }.take(3)
                                selectedPresetId = "custom"
                            },
                            label = { Text("Study min (5–180)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("study_focus_minutes_input")
                        )
                        OutlinedTextField(
                            value = customBreakText,
                            onValueChange = { raw ->
                                customBreakText = raw.filter { it.isDigit() }.take(2)
                                selectedPresetId = "custom"
                            },
                            label = { Text("Break min (0–30)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Subject & Study Goal Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("2. Subject & Study Goal", style = MaterialTheme.typography.titleLarge)

                    if (pendingTasks.isNotEmpty()) {
                        Text(
                            text = "Or pick from your planned tasks:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            pendingTasks.take(5).forEach { task ->
                                val chosen = selectedTaskId == task.id
                                FilterChip(
                                    selected = chosen,
                                    onClick = {
                                        if (chosen) {
                                            selectedTaskId = null
                                        } else {
                                            selectedTaskId = task.id
                                            selectedSubject = task.subject
                                            topicText = task.title
                                            customFocusText = task.plannedMinutes.toString()
                                        }
                                    },
                                    label = { Text("${task.title} (${task.plannedMinutes}m)") }
                                )
                            }
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        studySubjects.forEach { subj ->
                            FilterChip(
                                selected = selectedSubject == subj,
                                onClick = { selectedSubject = subj },
                                label = { Text(subj) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = topicText,
                        onValueChange = {
                            topicText = it
                            selectedTaskId = null
                        },
                        label = { Text("What are you studying right now? (Optional)") },
                        placeholder = { Text("e.g., Chapter 4 summary, Calculus practice") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("study_topic_input")
                    )
                }
            }
        }

        // 3. Phone Lock Settings & Start Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("3. Phone Lock Settings", style = MaterialTheme.typography.titleLarge)

                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ScreenLockPortrait,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Lock Phone During Study", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = "Full-screen study lock shield blocking navigation & impulsive switching.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = lockPhoneInStudy,
                                    onCheckedChange = { lockPhoneInStudy = it },
                                    modifier = Modifier.testTag("study_lock_toggle")
                                )
                            }

                            if (lockPhoneInStudy) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Android Screen Pinning", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = "Asks Android OS to pin screen so Home, Recents & other apps are locked.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = useOsPinning,
                                        onCheckedChange = { useOsPinning = it }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Keep Study Clock Awake", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = "Keeps the minimalist countdown visible on your desk.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = keepScreenAwake,
                                        onCheckedChange = { keepScreenAwake = it }
                                    )
                                }
                            }
                        }
                    }

                    val parsedFocus = (customFocusText.toIntOrNull() ?: 25).coerceIn(5, 180)
                    val parsedBreak = (customBreakText.toIntOrNull() ?: 5).coerceIn(0, 30)

                    Button(
                        onClick = {
                            onStartStudySession(
                                selectedTaskId,
                                selectedSubject,
                                topicText.ifBlank { "$selectedSubject Focus Block" },
                                parsedFocus,
                                parsedBreak,
                                lockPhoneInStudy,
                                useOsPinning,
                                keepScreenAwake
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_study_lock_button")
                    ) {
                        Icon(
                            imageVector = if (lockPhoneInStudy) Icons.Filled.Lock else Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lockPhoneInStudy) {
                                "Start ${parsedFocus}m Study & Lock Phone"
                            } else {
                                "Start ${parsedFocus}m Study Timer"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        item {
            SafetyNoticeBanner(text = stringResource(R.string.study_lock_notice))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudyTimePlannerTab(
    tasks: List<StudyTaskEntity>,
    todayStudyMinutes: Int,
    dailyStudyGoalMinutes: Int,
    onAddStudyTask: (subject: String, title: String, plannedMinutes: Int, timeSlot: String) -> Unit,
    onToggleCompleted: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onStartTaskLocked: (StudyTaskEntity) -> Unit
) {
    var subject by rememberSaveable { mutableStateOf("Deep Study") }
    var title by rememberSaveable { mutableStateOf("") }
    var plannedMins by rememberSaveable { mutableIntStateOf(25) }
    var timeSlot by rememberSaveable { mutableStateOf("Afternoon") }

    val totalPlannedPending = tasks.filter { !it.isCompleted }.sumOf { it.plannedMinutes }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("study_planner_list"),
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
                    Text("Plan a Study Time Block", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Planned remaining: ${totalPlannedPending}m • Completed today: ${todayStudyMinutes}m / ${dailyStudyGoalMinutes}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Study task or chapter") },
                        placeholder = { Text("e.g., Read Chapter 5 & summarize key definitions") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_task_title_input")
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        studySubjects.take(6).forEach { s ->
                            FilterChip(
                                selected = subject == s,
                                onClick = { subject = s },
                                label = { Text(s) }
                            )
                        }
                    }

                    Text("Duration & Time of Day", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 25, 45, 60, 90).forEach { mins ->
                            FilterChip(
                                selected = plannedMins == mins,
                                onClick = { plannedMins = mins },
                                label = { Text("${mins}m") }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Morning", "Afternoon", "Evening").forEach { slot ->
                            FilterChip(
                                selected = timeSlot == slot,
                                onClick = { timeSlot = slot },
                                label = { Text(slot) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onAddStudyTask(subject, title, plannedMins, timeSlot)
                            title = ""
                        },
                        enabled = title.trim().length >= 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("planner_add_task_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Study Schedule")
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Scheduled Study Blocks",
                subtitle = "Tap 'Lock & Start' on any task to begin focused study right away."
            )
        }

        if (tasks.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.MenuBook,
                    title = "No study tasks scheduled yet",
                    message = "Break big subjects into 25- or 45-minute study blocks so starting feels calm and manageable."
                )
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                StudyTaskCard(
                    task = task,
                    onToggleCompleted = { onToggleCompleted(task.id, !task.isCompleted) },
                    onDelete = { onDeleteTask(task.id) },
                    onStartLocked = { onStartTaskLocked(task) }
                )
            }
        }
    }
}

@Composable
private fun StudyTaskCard(
    task: StudyTaskEntity,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onStartLocked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleCompleted,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = "Toggle task complete",
                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = "${task.subject} • ${task.timeSlot} • ${task.completedMinutes}/${task.plannedMinutes} min focused",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete study task")
                }
            }

            if (!task.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = onStartLocked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("task_start_lock_${task.id}")
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lock Phone & Study (${task.plannedMinutes}m)")
                }
            }
        }
    }
}

@Composable
private fun StudySessionLogTab(
    uiState: WellnessUiState,
    sessions: List<StudySessionEntity>,
    onDeleteSession: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Focused (${uiState.selectedRangeDays}d)", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.rangeStudyMinutes} min",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Phone-Locked Blocks", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.rangeLockedStudySessions}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Completed Study Sessions",
                subtitle = "Every focused block builds steady momentum."
            )
        }

        if (sessions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.MenuBook,
                    title = "No study sessions logged yet",
                    message = "Start a Pomodoro or Deep Study block on the Focus & Lock tab to track your study hours."
                )
            }
        } else {
            items(sessions, key = { it.id }) { session ->
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
                                text = "${session.taskTitle} • ${session.durationMinutes} min",
                                style = MaterialTheme.typography.titleMedium
                            )
                            val lockLabel = if (session.phoneLocked) "Phone Locked" else "Timer Only"
                            val focusQuality = if (session.distractionCount == 0) {
                                "Unbroken focus"
                            } else {
                                "Returned ${session.distractionCount}x"
                            }
                            Text(
                                text = "${session.subject} • $lockLabel • $focusQuality • ${DateKeyUtils.readableDateTime(session.timestamp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onDeleteSession(session.id) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete study session")
                        }
                    }
                }
            }
        }
    }
}

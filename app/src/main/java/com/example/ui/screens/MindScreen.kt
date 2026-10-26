package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BreathingPattern
import com.example.data.model.DateKeyUtils
import com.example.data.model.JournalEntryEntity
import com.example.data.model.MindfulnessPractice
import com.example.data.model.MoodEntryEntity
import com.example.data.model.MoodLevel
import com.example.data.model.WellnessCatalog
import com.example.ui.components.CrisisSupportBanner
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SafetyNoticeBanner
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.viewmodel.WellnessUiState
import kotlinx.coroutines.delay

@Composable
fun MindScreen(
    uiState: WellnessUiState,
    initialTab: Int = 0,
    onLogMood: (moodScore: Int, stressLevel: Int?, tags: List<String>, note: String) -> Unit,
    onDeleteMood: (Long) -> Unit,
    onRecordMindfulness: (type: String, title: String, durationSeconds: Int) -> Unit,
    onSaveJournal: (prompt: String, content: String) -> Unit,
    onDeleteJournal: (Long) -> Unit,
    onDismissCrisisBanner: () -> Unit
) {
    var selectedTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    val tabs = listOf("Check-In", "Breathe", "Mindfulness", "Journal")

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
                    text = stringResource(R.string.mind_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Private space to notice how you feel, breathe slowly, and reflect.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("mind_tab_$index")
                    )
                }
            }

            when (selectedTab) {
                0 -> MoodCheckInSection(
                    moodEntries = uiState.moodEntries,
                    showCrisisBanner = uiState.showCrisisSupportBanner,
                    onDismissCrisisBanner = onDismissCrisisBanner,
                    onSaveMood = onLogMood,
                    onDeleteMood = onDeleteMood
                )

                1 -> BreathingTimerSection(
                    onCompleteSession = { title, seconds ->
                        onRecordMindfulness("Breathing", title, seconds)
                    }
                )

                2 -> MindfulnessGroundingSection(
                    onCompletePractice = { title, minutes ->
                        onRecordMindfulness("Grounding", title, minutes * 60)
                    }
                )

                else -> JournalingSection(
                    entries = uiState.journalEntries,
                    showCrisisBanner = uiState.showCrisisSupportBanner,
                    onDismissCrisisBanner = onDismissCrisisBanner,
                    onSaveJournal = onSaveJournal,
                    onDeleteJournal = onDeleteJournal
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodCheckInSection(
    moodEntries: List<MoodEntryEntity>,
    showCrisisBanner: Boolean,
    onDismissCrisisBanner: () -> Unit,
    onSaveMood: (Int, Int?, List<String>, String) -> Unit,
    onDeleteMood: (Long) -> Unit
) {
    var selectedMoodScore by rememberSaveable { mutableIntStateOf(3) }
    var includeStress by rememberSaveable { mutableStateOf(false) }
    var stressScore by rememberSaveable { mutableIntStateOf(2) }
    var selectedTags by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var note by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mind_checkin_list"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showCrisisBanner) {
            item { CrisisSupportBanner(onDismiss = onDismissCrisisBanner) }
        }

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
                    Text(
                        text = "How are you feeling right now?",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MoodLevel.entries.forEach { mood ->
                            val isSelected = selectedMoodScore == mood.score
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clickable { selectedMoodScore = mood.score }
                                    .testTag("mood_option_${mood.score}"),
                                shape = MaterialTheme.shapes.medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${mood.score}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = mood.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = MoodLevel.fromScore(selectedMoodScore).supportiveHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Optional Perceived Stress Check-in
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Optional: Log perceived tension/stress",
                            style = MaterialTheme.typography.titleSmall
                        )
                        FilterChip(
                            selected = includeStress,
                            onClick = { includeStress = !includeStress },
                            label = { Text(if (includeStress) "Included" else "Add") },
                            modifier = Modifier.testTag("toggle_stress_chip")
                        )
                    }

                    if (includeStress) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val labels = listOf("1 Calm", "2 Mild", "3 Moderate", "4 High", "5 Heavy")
                            (1..5).forEach { level ->
                                FilterChip(
                                    selected = stressScore == level,
                                    onClick = { stressScore = level },
                                    label = { Text(labels[level - 1], style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Context tags (Optional)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WellnessCatalog.moodContextTags.forEach { tag ->
                            val active = selectedTags.contains(tag)
                            FilterChip(
                                selected = active,
                                onClick = {
                                    selectedTags = if (active) selectedTags - tag else selectedTags + tag
                                },
                                label = { Text(tag) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Short private note (Optional)") },
                        placeholder = { Text("What feels most present right now?") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mood_note_input")
                    )

                    Button(
                        onClick = {
                            onSaveMood(
                                selectedMoodScore,
                                if (includeStress) stressScore else null,
                                selectedTags,
                                note
                            )
                            note = ""
                            selectedTags = emptyList()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_mood_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Private Check-In")
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Recent Check-Ins",
                subtitle = stringResource(R.string.mind_reflection_disclaimer)
            )
        }

        if (moodEntries.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.Mood,
                    title = "No mood check-ins yet",
                    message = "Check in whenever you'd like to notice patterns gently over time."
                )
            }
        } else {
            items(moodEntries.take(12), key = { it.id }) { entry ->
                val level = MoodLevel.fromScore(entry.moodScore)
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
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${level.label} (${entry.moodScore}/5)" +
                                    (entry.stressLevel?.let { " • Stress $it/5" } ?: ""),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = DateKeyUtils.readableDateTime(entry.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (entry.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.tags.joinToString(" • "),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (entry.note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = entry.note,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        IconButton(
                            onClick = { onDeleteMood(entry.id) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete mood check-in"
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BreathingTimerSection(
    onCompleteSession: (title: String, seconds: Int) -> Unit
) {
    val reducedMotion = LocalReducedMotion.current
    var patternIndex by rememberSaveable { mutableIntStateOf(0) }
    var targetMinutes by rememberSaveable { mutableIntStateOf(2) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var elapsedSeconds by rememberSaveable { mutableIntStateOf(0) }

    val pattern: BreathingPattern = WellnessCatalog.breathingPatterns[patternIndex]
    val totalSeconds = targetMinutes * 60
    val remainingSeconds = (totalSeconds - elapsedSeconds).coerceAtLeast(0)

    LaunchedEffect(isRunning, totalSeconds) {
        while (isRunning && elapsedSeconds < totalSeconds) {
            delay(1000L)
            elapsedSeconds += 1
            if (elapsedSeconds >= totalSeconds) {
                isRunning = false
                onCompleteSession(pattern.title, totalSeconds)
            }
        }
    }

    val cyclePos = if (pattern.cycleSeconds > 0) elapsedSeconds % pattern.cycleSeconds else 0
    val (phaseLabel, targetScale) = when {
        !isRunning && elapsedSeconds == 0 -> "Ready when you are" to 0.68f
        cyclePos < pattern.inhaleSec -> "Breathe in gently…" to 0.96f
        cyclePos < pattern.inhaleSec + pattern.holdInSec -> "Pause softly…" to 0.96f
        cyclePos < pattern.inhaleSec + pattern.holdInSec + pattern.exhaleSec -> "Breathe out slowly…" to 0.60f
        else -> "Rest quietly…" to 0.60f
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (reducedMotion) 0.78f else targetScale,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 1200),
        label = "breathingScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            SafetyNoticeBanner(text = stringResource(R.string.mind_breathing_disclaimer))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Breathing Cadence", style = MaterialTheme.typography.titleMedium)
                WellnessCatalog.breathingPatterns.forEachIndexed { idx, p ->
                    val selected = idx == patternIndex
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isRunning) {
                                patternIndex = idx
                                elapsedSeconds = 0
                            }
                            .testTag("breathing_pattern_$idx"),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(p.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(p.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1, 2, 3, 5).forEach { mins ->
                    FilterChip(
                        selected = targetMinutes == mins,
                        onClick = {
                            if (!isRunning) {
                                targetMinutes = mins
                                elapsedSeconds = 0
                            }
                        },
                        label = { Text("$mins min") },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("breathing_duration_$mins")
                    )
                }
            }
        }

        // Visual Breathing Circle
        item {
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val progressFraction = if (totalSeconds > 0) {
                    elapsedSeconds.toFloat() / totalSeconds.toFloat()
                } else 0f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val minDim = size.minDimension
                    val strokePx = 10.dp.toPx()
                    drawCircle(
                        color = trackColor,
                        radius = (minDim / 2f) - strokePx,
                        style = Stroke(width = strokePx)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progressFraction,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                    drawCircle(
                        color = containerColor,
                        radius = (minDim / 2.4f) * animatedScale
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%d:%02d", remainingSeconds / 60, remainingSeconds % 60),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("breathing_timer_display")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = phaseLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Controls
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("breathing_start_pause_button")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when {
                            isRunning -> "Pause"
                            elapsedSeconds > 0 -> "Resume"
                            else -> "Start Breathing"
                        }
                    )
                }

                if (elapsedSeconds >= 10) {
                    FilledTonalButton(
                        onClick = {
                            val completedSec = elapsedSeconds
                            isRunning = false
                            elapsedSeconds = 0
                            onCompleteSession(pattern.title, completedSec)
                        },
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("breathing_finish_button")
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Finish & Log")
                    }
                } else if (elapsedSeconds > 0) {
                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            elapsedSeconds = 0
                        },
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Reset")
                    }
                }
            }
        }
    }
}

@Composable
private fun MindfulnessGroundingSection(
    onCompletePractice: (title: String, durationMinutes: Int) -> Unit
) {
    var activePracticeId by rememberSaveable { mutableStateOf<String?>(null) }
    var currentStep by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "Beginner-Friendly Grounding",
                subtitle = "Short, unhurried practices you can follow step by step."
            )
        }

        items(WellnessCatalog.mindfulnessPractices, key = { it.id }) { practice ->
            val isExpanded = activePracticeId == practice.id
            PracticeGuideCard(
                practice = practice,
                isExpanded = isExpanded,
                currentStep = if (isExpanded) currentStep else 0,
                onExpand = {
                    activePracticeId = if (isExpanded) null else practice.id
                    currentStep = 0
                },
                onNextStep = {
                    if (currentStep < practice.steps.lastIndex) {
                        currentStep += 1
                    } else {
                        onCompletePractice(practice.title, practice.durationMinutes)
                        activePracticeId = null
                        currentStep = 0
                    }
                }
            )
        }
    }
}

@Composable
private fun PracticeGuideCard(
    practice: MindfulnessPractice,
    isExpanded: Boolean,
    currentStep: Int,
    onExpand: () -> Unit,
    onNextStep: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(practice.title, style = MaterialTheme.typography.titleMedium)
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "${practice.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = practice.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Step ${currentStep + 1} of ${practice.steps.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = practice.steps[currentStep],
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onNextStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                ) {
                    Text(
                        if (currentStep < practice.steps.lastIndex) {
                            "Next Step"
                        } else {
                            "Complete & Log Practice"
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalingSection(
    entries: List<JournalEntryEntity>,
    showCrisisBanner: Boolean,
    onDismissCrisisBanner: () -> Unit,
    onSaveJournal: (prompt: String, content: String) -> Unit,
    onDeleteJournal: (Long) -> Unit
) {
    var selectedPrompt by rememberSaveable { mutableStateOf(WellnessCatalog.journalPrompts.first()) }
    var content by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showCrisisBanner) {
            item { CrisisSupportBanner(onDismiss = onDismissCrisisBanner) }
        }

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
                    Text("Optional Reflection Prompts", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WellnessCatalog.journalPrompts.forEachIndexed { idx, prompt ->
                            FilterChip(
                                selected = selectedPrompt == prompt,
                                onClick = { selectedPrompt = prompt },
                                label = { Text(prompt, style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.testTag("journal_prompt_$idx")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Your private reflection") },
                        placeholder = { Text(selectedPrompt) },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_content_input")
                    )

                    Button(
                        onClick = {
                            onSaveJournal(selectedPrompt, content)
                            content = ""
                        },
                        enabled = content.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_journal_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Reflection Locally")
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Saved Reflections",
                subtitle = "Stored strictly on this device. Never shared."
            )
        }

        if (entries.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.EditNote,
                    title = "Your private journal is empty",
                    message = "Even one honest sentence counts. Write whenever it feels helpful."
                )
            }
        } else {
            items(entries, key = { it.id }) { entry ->
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
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = DateKeyUtils.readableDateTime(entry.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (entry.prompt.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.prompt,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = entry.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(
                            onClick = { onDeleteJournal(entry.id) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete reflection"
                            )
                        }
                    }
                }
            }
        }
    }
}

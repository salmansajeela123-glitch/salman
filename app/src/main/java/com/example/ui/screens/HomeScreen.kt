package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MoodLevel
import com.example.ui.components.CrisisSupportBanner
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.DailyPlanItem
import com.example.ui.viewmodel.HabitWithStatus
import com.example.ui.viewmodel.WellnessUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: WellnessUiState,
    onNavigateToStudyTab: (Int) -> Unit,
    onNavigateToMindTab: (Int) -> Unit,
    onNavigateToMoveTab: (Int) -> Unit,
    onAddWaterGlass: () -> Unit,
    onToggleRestDay: (Boolean) -> Unit,
    onMarkPlanStatus: (DailyPlanItem, String) -> Unit,
    onSwapPlanItem: (DailyPlanItem) -> Unit,
    onToggleHabit: (Long, Boolean) -> Unit,
    onCreateHabit: (String, String, String) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onDismissCrisisBanner: () -> Unit
) {
    var showAddHabitDialog by rememberSaveable { mutableStateOf(false) }
    val isRestDay = uiState.preferences.isRestDayToday == uiState.todayKey

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("home_screen_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.showCrisisSupportBanner) {
                item {
                    CrisisSupportBanner(onDismiss = onDismissCrisisBanner)
                }
            }

            // Hero Greeting & Overview Card
            item {
                HeroOverviewCard(
                    greeting = uiState.greeting,
                    displayName = uiState.preferences.displayName,
                    readableDate = uiState.readableDate,
                    moodLabel = uiState.todayMood?.let { MoodLevel.fromScore(it.moodScore).label } ?: "Not yet",
                    studyMinutes = uiState.todayStudyMinutes,
                    moveMinutes = uiState.todayMovementMinutes,
                    waterText = "${uiState.todayHydrationGlasses}/${uiState.preferences.hydrationGoalGlasses}"
                )
            }

            // Quick Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.home_quick_actions),
                        subtitle = "Pick one small moment whenever it fits your day."
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Filled.Lock,
                            label = "Study Lock",
                            tag = "quick_study_button",
                            onClick = { onNavigateToStudyTab(0) }
                        )
                        QuickActionButton(
                            icon = Icons.Filled.Mood,
                            label = "Mood",
                            tag = "quick_mood_button",
                            onClick = { onNavigateToMindTab(0) }
                        )
                        QuickActionButton(
                            icon = Icons.Filled.Air,
                            label = "Breathe",
                            tag = "quick_breathe_button",
                            onClick = { onNavigateToMindTab(1) }
                        )
                        QuickActionButton(
                            icon = Icons.Filled.DirectionsWalk,
                            label = "Move",
                            tag = "quick_move_button",
                            onClick = { onNavigateToMoveTab(0) }
                        )
                        QuickActionButton(
                            icon = Icons.Filled.WaterDrop,
                            label = "Water +1",
                            tag = "quick_water_add_button",
                            onClick = onAddWaterGlass
                        )
                        QuickActionButton(
                            icon = Icons.Filled.EditNote,
                            label = "Journal",
                            tag = "quick_journal_button",
                            onClick = { onNavigateToMindTab(3) }
                        )
                    }
                }
            }

            // Personalized Daily Plan Header + Rest Day Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_todays_plan),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tailored to ~${uiState.preferences.dailyMinutesAvailable} min/day • Edit, swap, or rest anytime.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = isRestDay,
                        onClick = { onToggleRestDay(!isRestDay) },
                        label = { Text(if (isRestDay) "Resting" else "Rest Day") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Bedtime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("rest_day_toggle")
                    )
                }
            }

            items(uiState.dailyPlan, key = { it.key }) { item ->
                DailyPlanCard(
                    item = item,
                    onOpenTarget = {
                        when (item.actionTarget) {
                            "MOOD" -> onNavigateToMindTab(0)
                            "BREATHE" -> onNavigateToMindTab(1)
                            "JOURNAL" -> onNavigateToMindTab(3)
                            "MOVE" -> onNavigateToMoveTab(0)
                            "HYDRATE" -> onAddWaterGlass()
                        }
                    },
                    onComplete = { onMarkPlanStatus(item, "COMPLETED") },
                    onSkip = { onMarkPlanStatus(item, "SKIPPED") },
                    onSwap = { onSwapPlanItem(item) }
                )
            }

            // Habits Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_daily_habits),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.copy_start_again),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = { showAddHabitDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_add_habit_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Habit")
                    }
                }
            }

            if (uiState.habitsWithStatus.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Outlined.Spa,
                        title = "Start with one gentle habit",
                        message = "Try something small like stretching for 2 minutes or pausing for a glass of water.",
                        actionLabel = "Add a Habit",
                        onAction = { showAddHabitDialog = true }
                    )
                }
            } else {
                items(uiState.habitsWithStatus, key = { it.habit.id }) { habitStatus ->
                    HabitRowCard(
                        habitStatus = habitStatus,
                        onToggle = {
                            onToggleHabit(habitStatus.habit.id, !habitStatus.completedToday)
                        },
                        onDelete = { onDeleteHabit(habitStatus.habit.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onSave = { title, category, why ->
                onCreateHabit(title, category, why)
                showAddHabitDialog = false
            }
        )
    }
}

@Composable
private fun HeroOverviewCard(
    greeting: String,
    displayName: String,
    readableDate: String,
    moodLabel: String,
    studyMinutes: Int,
    moveMinutes: Int,
    waterText: String
) {
    val headerText = if (displayName.isNotBlank()) "$greeting, $displayName" else greeting
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(22.dp)
        ) {
            Text(
                text = readableDate.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = headerText,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.copy_small_steps),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverviewMiniStat(label = "Mood", value = moodLabel, modifier = Modifier.weight(1f))
                OverviewMiniStat(label = "Study", value = "${studyMinutes}m", modifier = Modifier.weight(1f))
                OverviewMiniStat(label = "Move", value = "${moveMinutes}m", modifier = Modifier.weight(1f))
                OverviewMiniStat(label = "Water", value = waterText, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OverviewMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(tag)
    )
}

@Composable
private fun DailyPlanCard(
    item: DailyPlanItem,
    onOpenTarget: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onSwap: () -> Unit
) {
    val isCompleted = item.status == "COMPLETED"
    val isSkipped = item.status == "SKIPPED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTarget() }
            .testTag("plan_card_${item.key}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                isSkipped -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${item.category.label} • ${item.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (isCompleted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (isSkipped) {
                    Text(
                        text = "Skipped for today",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.whySuggested,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isCompleted && !isSkipped) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onComplete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("plan_done_${item.key}")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Done")
                    }
                    if (item.key != "rest_day_breath") {
                        OutlinedButton(
                            onClick = onSwap,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("plan_swap_${item.key}")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Swap suggestion", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Swap")
                        }
                    }
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("plan_skip_${item.key}")
                    ) {
                        Text("Skip")
                    }
                }
            }
        }
    }
}

@Composable
fun HabitRowCard(
    habitStatus: HabitWithStatus,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val habit = habitStatus.habit
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("habit_card_${habit.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (habitStatus.completedToday) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("habit_toggle_${habit.id}")
            ) {
                Icon(
                    imageVector = if (habitStatus.completedToday) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Outlined.Circle
                    },
                    contentDescription = if (habitStatus.completedToday) {
                        "Mark ${habit.title} incomplete"
                    } else {
                        "Mark ${habit.title} complete"
                    },
                    tint = if (habitStatus.completedToday) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (habitStatus.completedToday) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
                val sub = buildString {
                    append(habit.category)
                    append(" • Practiced ")
                    append(habitStatus.daysPracticedLast7)
                    append(" of last 7 days")
                }
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("habit_delete_${habit.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete habit ${habit.title}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, whyNote: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Mind") }
    var whyNote by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create a Gentle Habit", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Keep it small and realistic — like '2-minute afternoon stretch' or 'Read 2 pages before sleep'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit action") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_title_input")
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Mind", "Body", "Rest").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = whyNote,
                    onValueChange = { whyNote = it },
                    label = { Text("Why this helps me (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, category, whyNote) },
                enabled = title.trim().length >= 2,
                modifier = Modifier.testTag("habit_save_button")
            ) {
                Text("Save Habit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

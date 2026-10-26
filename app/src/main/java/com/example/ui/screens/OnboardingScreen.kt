package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ReminderScheduler
import com.example.data.model.ExperienceLevel
import com.example.data.model.WellnessGoal
import com.example.ui.components.SafetyNoticeBanner

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (
        displayName: String,
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel,
        remindersEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ) -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    var displayName by rememberSaveable { mutableStateOf("") }
    var selectedGoalNames by rememberSaveable {
        mutableStateOf(
            listOf(
                WellnessGoal.MINDFULNESS.name,
                WellnessGoal.DAILY_MOVEMENT.name,
                WellnessGoal.CONSISTENCY.name
            )
        )
    }
    var dailyMinutes by rememberSaveable { mutableIntStateOf(10) }
    var experienceLevelName by rememberSaveable { mutableStateOf(ExperienceLevel.GENTLE_RETURN.name) }
    var remindersEnabled by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        remindersEnabled = granted
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Top progress & Skip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 0) {
                        IconButton(
                            onClick = { step -= 1 },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("onboarding_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous step"
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("onboarding_skip_button")
                    ) {
                        Text(stringResource(R.string.onboarding_skip))
                    }
                }

                LinearProgressIndicator(
                    progress = { (step + 1) / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (step) {
                    0 -> WelcomeStepContent(
                        displayName = displayName,
                        onNameChange = { displayName = it }
                    )

                    1 -> GoalsStepContent(
                        selectedGoalNames = selectedGoalNames,
                        onToggleGoal = { goalName ->
                            selectedGoalNames = if (selectedGoalNames.contains(goalName)) {
                                if (selectedGoalNames.size > 1) selectedGoalNames - goalName else selectedGoalNames
                            } else {
                                selectedGoalNames + goalName
                            }
                        }
                    )

                    else -> PreferencesStepContent(
                        dailyMinutes = dailyMinutes,
                        onMinutesSelect = { dailyMinutes = it },
                        experienceLevelName = experienceLevelName,
                        onExperienceSelect = { experienceLevelName = it },
                        remindersEnabled = remindersEnabled,
                        onToggleReminders = { wantEnable ->
                            if (wantEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !ReminderScheduler.hasNotificationPermission(context)
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                remindersEnabled = wantEnable
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (step < 2) {
                        step += 1
                    } else {
                        val goals = selectedGoalNames
                            .mapNotNull { raw -> WellnessGoal.entries.find { it.name == raw } }
                            .toSet()
                        val level = ExperienceLevel.entries.find { it.name == experienceLevelName }
                            ?: ExperienceLevel.GENTLE_RETURN
                        if (remindersEnabled) {
                            ReminderScheduler.scheduleDailyReminder(context, 9, 0)
                        }
                        onComplete(displayName, goals, dailyMinutes, level, remindersEnabled, 9, 0)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag(if (step < 2) "onboarding_next_button" else "onboarding_finish_button"),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = if (step < 2) {
                        stringResource(R.string.onboarding_continue)
                    } else {
                        stringResource(R.string.onboarding_start)
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (step < 2) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.Check,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun WelcomeStepContent(
    displayName: String,
    onNameChange: (String) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            imageVector = Icons.Filled.Spa,
            contentDescription = "Mind and Body emblem",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .padding(16.dp)
                .size(36.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = stringResource(R.string.onboarding_welcome_title),
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = stringResource(R.string.onboarding_welcome_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedTextField(
        value = displayName,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.onboarding_name_label)) },
        placeholder = { Text("e.g., Alex (or leave blank)") },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_name_input")
    )

    Spacer(modifier = Modifier.height(20.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.onboarding_disclaimer_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.onboarding_disclaimer_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    SafetyNoticeBanner(
        text = stringResource(R.string.onboarding_privacy_note),
        icon = Icons.Outlined.Lock
    )
}

@Composable
private fun GoalsStepContent(
    selectedGoalNames: List<String>,
    onToggleGoal: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.onboarding_goals_title),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = stringResource(R.string.onboarding_goals_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(18.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WellnessGoal.entries.forEach { goal ->
            val isSelected = selectedGoalNames.contains(goal.name)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleGoal(goal.name) }
                    .testTag("goal_card_${goal.name.lowercase()}"),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Text(
                            text = goal.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferencesStepContent(
    dailyMinutes: Int,
    onMinutesSelect: (Int) -> Unit,
    experienceLevelName: String,
    onExperienceSelect: (String) -> Unit,
    remindersEnabled: Boolean,
    onToggleReminders: (Boolean) -> Unit
) {
    Text(
        text = stringResource(R.string.onboarding_prefs_title),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Start small — even 5 minutes is enough to build steady momentum.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(22.dp))

    Text(
        text = stringResource(R.string.onboarding_time_label),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(5, 10, 15, 20, 30).forEach { mins ->
            FilterChip(
                selected = dailyMinutes == mins,
                onClick = { onMinutesSelect(mins) },
                label = { Text("$mins min / day") },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("time_chip_$mins")
            )
        }
    }

    Spacer(modifier = Modifier.height(22.dp))

    Text(
        text = stringResource(R.string.onboarding_level_label),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ExperienceLevel.entries.forEach { level ->
            val selected = experienceLevelName == level.name
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExperienceSelect(level.name) }
                    .testTag("level_card_${level.name.lowercase()}"),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = level.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = level.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(22.dp))

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Gentle Morning Reminder (9:00 AM)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Optional, supportive nudge — never shows private entries on your lock screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = remindersEnabled,
                onCheckedChange = onToggleReminders,
                modifier = Modifier.testTag("onboarding_reminder_switch")
            )
        }
    }
}

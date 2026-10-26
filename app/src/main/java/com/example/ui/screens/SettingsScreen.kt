package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.ReminderScheduler
import com.example.data.model.ExperienceLevel
import com.example.data.model.WellnessGoal
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.SafetyNoticeBanner
import com.example.ui.viewmodel.WellnessUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: WellnessUiState,
    onUpdateGoalsAndTime: (Set<WellnessGoal>, Int, ExperienceLevel) -> Unit,
    onUpdateReminders: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    onUpdateTheme: (String) -> Unit,
    onUpdateReducedMotion: (Boolean) -> Unit,
    onLoadDemoData: () -> Unit,
    onRemoveDemoData: () -> Unit,
    onRequestExportJson: () -> Unit,
    onDismissExportJson: () -> Unit,
    onDeleteAllData: () -> Unit
) {
    val context = LocalContext.current
    val prefs = uiState.preferences
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var showPrivacyDialog by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            ReminderScheduler.scheduleDailyReminder(context, prefs.reminderHour, prefs.reminderMinute)
            onUpdateReminders(true, prefs.reminderHour, prefs.reminderMinute)
        }
    }

    val saveJsonDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = uiState.exportedJson
        if (uri != null && !json.isNullOrBlank()) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
            }
            onDismissExportJson()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("settings_screen_list"),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Settings & Privacy",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your preferences, accessibility options, and complete control over local data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. Goals & Daily Rhythm
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
                        Text("Focus Goals & Daily Time", style = MaterialTheme.typography.titleLarge)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WellnessGoal.entries.forEach { goal ->
                                val selected = prefs.selectedGoals.contains(goal)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val next = if (selected && prefs.selectedGoals.size > 1) {
                                            prefs.selectedGoals - goal
                                        } else {
                                            prefs.selectedGoals + goal
                                        }
                                        onUpdateGoalsAndTime(next, prefs.dailyMinutesAvailable, prefs.experienceLevel)
                                    },
                                    label = { Text(goal.title) }
                                )
                            }
                        }

                        Text("Daily time commitment", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15, 20, 30).forEach { mins ->
                                FilterChip(
                                    selected = prefs.dailyMinutesAvailable == mins,
                                    onClick = {
                                        onUpdateGoalsAndTime(prefs.selectedGoals, mins, prefs.experienceLevel)
                                    },
                                    label = { Text("$mins min") }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Gentle Local Reminders
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Gentle Local Reminders", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "Supportive nudges with private lock-screen text.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = prefs.remindersEnabled,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                            !ReminderScheduler.hasNotificationPermission(context)
                                        ) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            ReminderScheduler.scheduleDailyReminder(context, prefs.reminderHour, prefs.reminderMinute)
                                            onUpdateReminders(true, prefs.reminderHour, prefs.reminderMinute)
                                        }
                                    } else {
                                        ReminderScheduler.cancelDailyReminder(context)
                                        onUpdateReminders(false, prefs.reminderHour, prefs.reminderMinute)
                                    }
                                },
                                modifier = Modifier.testTag("settings_reminder_switch")
                            )
                        }

                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                8 to 30,
                                9 to 0,
                                13 to 0,
                                19 to 30
                            ).forEach { (hr, min) ->
                                val label = String.format("%02d:%02d", hr, min)
                                FilterChip(
                                    selected = prefs.reminderHour == hr && prefs.reminderMinute == min,
                                    onClick = {
                                        if (prefs.remindersEnabled) {
                                            ReminderScheduler.scheduleDailyReminder(context, hr, min)
                                        }
                                        onUpdateReminders(prefs.remindersEnabled, hr, min)
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }

                        if (prefs.remindersEnabled) {
                            OutlinedButton(
                                onClick = { ReminderScheduler.showGentleNotification(context) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("Send Test Gentle Notification")
                            }
                        }
                    }
                }
            }

            // 3. Appearance & Accessibility
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
                            Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Theme & Accessibility", style = MaterialTheme.typography.titleLarge)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (key, label) ->
                                FilterChip(
                                    selected = prefs.themeMode == key,
                                    onClick = { onUpdateTheme(key) },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("theme_chip_${key.lowercase()}")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reduce Motion", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "Replaces expanding breathing circles with a calm static progress ring.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = prefs.reducedMotion,
                                onCheckedChange = onUpdateReducedMotion,
                                modifier = Modifier.testTag("reduced_motion_switch")
                            )
                        }
                    }
                }
            }

            // 4. Clearly Marked Demo Data
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sample / Demo Data", style = MaterialTheme.typography.titleLarge)
                        }
                        Text(
                            text = "Want to preview weekly charts right away? Load clearly marked [Demo] entries and remove them in one tap anytime.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(
                                onClick = onLoadDemoData,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("load_demo_data_button")
                            ) {
                                Text("Load [Demo] Data")
                            }
                            if (prefs.hasDemoData) {
                                OutlinedButton(
                                    onClick = onRemoveDemoData,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("remove_demo_data_button")
                                ) {
                                    Text("Remove [Demo] Data")
                                }
                            }
                        }
                    }
                }
            }

            // 5. Privacy, Export & Data Deletion
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
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Local Storage & Your Data", style = MaterialTheme.typography.titleLarge)
                        }

                        SafetyNoticeBanner(
                            text = "Where your data lives: All mood check-ins, journals, habits, movement, sleep, and hydration logs are stored locally inside this app's private on-device database (Room SQLite & DataStore). Cloud sync and auto-backup are off by default, and your personal reflections are never sold or transmitted."
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onRequestExportJson,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("export_data_button")
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export JSON")
                            }

                            OutlinedButton(
                                onClick = { showPrivacyDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("privacy_policy_button")
                            ) {
                                Icon(Icons.Outlined.Policy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Privacy Policy")
                            }
                        }

                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("delete_all_data_button")
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete All Stored Data")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showDeleteConfirm) {
        ConfirmActionDialog(
            title = "Delete all local wellness data?",
            body = "This permanently removes all mood check-ins, private journal reflections, habits, movement logs, sleep routines, and preferences stored on this device. This action cannot be undone.",
            confirmLabel = "Delete Everything",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirm = false
                ReminderScheduler.cancelDailyReminder(context)
                onDeleteAllData()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (!uiState.exportedJson.isNullOrBlank()) {
        val json = uiState.exportedJson
        AlertDialog(
            onDismissRequest = onDismissExportJson,
            title = { Text("Export Your Local Data (JSON)", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(
                    modifier = Modifier
                        .height(260.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = json,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_TEXT, json)
                                putExtra(Intent.EXTRA_SUBJECT, "Mind & Body Wellness Export")
                            }
                            runCatching {
                                context.startActivity(Intent.createChooser(sendIntent, "Share Wellness JSON"))
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                    Button(
                        onClick = {
                            saveJsonDocumentLauncher.launch("mind_body_export_${uiState.todayKey}.json")
                        }
                    ) {
                        Text("Save File")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissExportJson) {
                    Text("Close")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Wellness Notice", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PRIVACY POLICY PLACEHOLDER (Complete prior to public release):",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "1. Local-Only Storage: Mind & Body stores your wellness preferences, mood check-ins, journals, movement logs, habits, and sleep/hydration records locally on your device using Android Room and Jetpack DataStore.\n\n" +
                            "2. No Selling or Sharing: Journal entries, mood notes, and personal habits are never sold or shared with advertisers. Before publishing to production app stores, replace this placeholder with your organization's legal controller contact details and formal privacy policy URL.\n\n" +
                            "3. Medical & Crisis Disclaimer: Mind & Body provides general lifestyle and relaxation support. It is not a medical device, clinical assessment, or crisis service. If you experience severe physical symptoms or thoughts of self-harm, contact your local emergency services (e.g., 911, 999, 112, or 988) immediately.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}

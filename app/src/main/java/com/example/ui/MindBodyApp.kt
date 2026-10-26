package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MindScreen
import com.example.ui.screens.MoveScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudyLockOverlay
import com.example.ui.screens.StudyScreen
import com.example.ui.viewmodel.WellnessViewModel

enum class MainDestination(
    val route: String,
    val labelRes: Int,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String,
    val showInBottomBar: Boolean = true
) {
    HOME("home", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    STUDY("study", R.string.nav_study, Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_study"),
    MIND("mind", R.string.nav_mind, Icons.Filled.Spa, Icons.Outlined.Spa, "nav_mind"),
    MOVE("move", R.string.nav_move, Icons.Filled.DirectionsWalk, Icons.Outlined.DirectionsWalk, "nav_move"),
    PROGRESS("progress", R.string.nav_progress, Icons.Filled.Insights, Icons.Outlined.Insights, "nav_progress"),
    SETTINGS("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings", showInBottomBar = false)
}

@Composable
fun MindBodyApp(viewModel: WellnessViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentDestination by rememberSaveable { mutableStateOf(MainDestination.HOME) }
    var studySubTab by rememberSaveable { mutableIntStateOf(0) }
    var mindSubTab by rememberSaveable { mutableIntStateOf(0) }
    var moveSubTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(currentDestination) {
        snackbarHostState.currentSnackbarData?.dismiss()
    }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearStatusMessage()
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (!uiState.preferences.onboardingCompleted) {
        OnboardingScreen(
            onComplete = { name, goals, mins, level, remEnabled, remHr, remMin ->
                viewModel.completeOnboarding(name, goals, mins, level, remEnabled, remHr, remMin)
            },
            onSkip = { viewModel.skipOnboarding() }
        )
        return
    }

    // Full-screen Study Phone Lock Shield takes precedence over navigation when locked study time is active
    val activeStudy = uiState.activeStudySession
    if (activeStudy.isActive && activeStudy.phoneLockEnabled) {
        StudyLockOverlay(
            session = activeStudy,
            onTogglePause = { viewModel.togglePauseStudySession() },
            onUnlockAndFinish = { viewModel.finishOrUnlockStudySession() },
            onDistractionDetected = { viewModel.recordStudyDistractionAttempt() }
        )
        return
    }

    if (currentDestination != MainDestination.HOME) {
        BackHandler {
            currentDestination = MainDestination.HOME
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (!isWideScreen) {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (activeStudy.isActive && !activeStudy.phoneLockEnabled) {
                                    AssistChip(
                                        onClick = {
                                            studySubTab = 0
                                            currentDestination = MainDestination.STUDY
                                        },
                                        label = {
                                            Text(
                                                String.format(
                                                    "%02d:%02d",
                                                    activeStudy.remainingSeconds / 60,
                                                    activeStudy.remainingSeconds % 60
                                                )
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Timer,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                IconButton(
                                    onClick = { currentDestination = MainDestination.SETTINGS },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("nav_settings")
                                ) {
                                    Icon(
                                        imageVector = if (currentDestination == MainDestination.SETTINGS) {
                                            Icons.Filled.Settings
                                        } else {
                                            Icons.Outlined.Settings
                                        },
                                        contentDescription = stringResource(R.string.nav_settings),
                                        tint = if (currentDestination == MainDestination.SETTINGS) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        MainDestination.entries
                            .filter { it.showInBottomBar }
                            .forEach { dest ->
                                val selected = currentDestination == dest
                                val label = stringResource(dest.labelRes)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.filledIcon else dest.outlinedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        MainDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            val label = stringResource(dest.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.filledIcon else dest.outlinedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentDestination) {
                        MainDestination.HOME -> HomeScreen(
                            uiState = uiState,
                            onNavigateToStudyTab = { subTab ->
                                studySubTab = subTab
                                currentDestination = MainDestination.STUDY
                            },
                            onNavigateToMindTab = { subTab ->
                                mindSubTab = subTab
                                currentDestination = MainDestination.MIND
                            },
                            onNavigateToMoveTab = { subTab ->
                                moveSubTab = subTab
                                currentDestination = MainDestination.MOVE
                            },
                            onAddWaterGlass = { viewModel.adjustHydration(1) },
                            onToggleRestDay = { viewModel.toggleRestDay(it) },
                            onMarkPlanStatus = { item, status -> viewModel.markPlanItemStatus(item, status) },
                            onSwapPlanItem = { viewModel.swapPlanItem(it) },
                            onToggleHabit = { id, done -> viewModel.toggleHabitToday(id, done) },
                            onCreateHabit = { title, cat, why -> viewModel.addHabit(title, cat, why) },
                            onDeleteHabit = { viewModel.deleteHabit(it) },
                            onDismissCrisisBanner = { viewModel.dismissCrisisBanner() }
                        )

                        MainDestination.STUDY -> StudyScreen(
                            uiState = uiState,
                            initialTab = studySubTab,
                            onStartStudySession = { taskId, subj, title, focusMin, breakMin, lock, pin, awake ->
                                viewModel.startStudySession(
                                    linkedTaskId = taskId,
                                    subject = subj,
                                    taskTitle = title,
                                    focusMinutes = focusMin,
                                    breakMinutes = breakMin,
                                    lockPhone = lock,
                                    requestOsPinning = pin,
                                    keepScreenAwake = awake
                                )
                            },
                            onTogglePauseActive = { viewModel.togglePauseStudySession() },
                            onFinishActive = { viewModel.finishOrUnlockStudySession() },
                            onAddStudyTask = { subj, title, mins, slot ->
                                viewModel.addStudyTask(subj, title, mins, slot)
                            },
                            onToggleStudyTaskCompleted = { id, done ->
                                viewModel.toggleStudyTaskCompleted(id, done)
                            },
                            onDeleteStudyTask = { viewModel.deleteStudyTask(it) },
                            onDeleteStudySession = { viewModel.deleteStudySession(it) },
                            onUpdateStudyGoalMinutes = { viewModel.updateStudyGoalMinutes(it) }
                        )

                        MainDestination.MIND -> MindScreen(
                            uiState = uiState,
                            initialTab = mindSubTab,
                            onLogMood = { mood, stress, tags, note ->
                                viewModel.logMoodCheckIn(mood, stress, tags, note)
                            },
                            onDeleteMood = { viewModel.deleteMood(it) },
                            onRecordMindfulness = { type, title, sec ->
                                viewModel.recordMindfulnessSession(type, title, sec)
                            },
                            onSaveJournal = { prompt, content ->
                                viewModel.saveJournalEntry(prompt, content)
                            },
                            onDeleteJournal = { viewModel.deleteJournal(it) },
                            onDismissCrisisBanner = { viewModel.dismissCrisisBanner() }
                        )

                        MainDestination.MOVE -> MoveScreen(
                            uiState = uiState,
                            initialTab = moveSubTab,
                            onLogActivity = { type, title, mins, intensity, notes ->
                                viewModel.recordActivity(type, title, mins, intensity, notes)
                            },
                            onDeleteActivity = { viewModel.deleteActivityLog(it) },
                            onAdjustHydration = { viewModel.adjustHydration(it) },
                            onUpdateHydrationGoal = { viewModel.updateHydrationGoal(it) },
                            onLogSleep = { bed, wake, hrs, qual, notes ->
                                viewModel.recordSleep(bed, wake, hrs, qual, notes)
                            },
                            onDeleteSleep = { viewModel.deleteSleepLog(it) }
                        )

                        MainDestination.PROGRESS -> ProgressScreen(
                            uiState = uiState,
                            onSelectRangeDays = { viewModel.setDateRangeDays(it) },
                            onDeleteMood = { viewModel.deleteMood(it) },
                            onDeleteActivity = { viewModel.deleteActivityLog(it) },
                            onDeleteMindfulness = { viewModel.deleteMindfulnessSession(it) },
                            onDeleteSleep = { viewModel.deleteSleepLog(it) },
                            onDeleteJournal = { viewModel.deleteJournal(it) },
                            onDeleteStudySession = { viewModel.deleteStudySession(it) }
                        )

                        MainDestination.SETTINGS -> SettingsScreen(
                            uiState = uiState,
                            onUpdateGoalsAndTime = { goals, mins, level ->
                                viewModel.updateGoalsAndTime(goals, mins, level)
                            },
                            onUpdateReminders = { enabled, hr, min ->
                                viewModel.updateReminderSettings(enabled, hr, min)
                            },
                            onUpdateTheme = { viewModel.updateThemeMode(it) },
                            onUpdateReducedMotion = { viewModel.updateReducedMotion(it) },
                            onLoadDemoData = { viewModel.loadDemoData() },
                            onRemoveDemoData = { viewModel.removeDemoData() },
                            onRequestExportJson = { viewModel.prepareDataExport() },
                            onDismissExportJson = { viewModel.clearExportPreview() },
                            onDeleteAllData = { viewModel.deleteAllData() }
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActivityLogEntity
import com.example.data.model.DateKeyUtils
import com.example.data.model.ExperienceLevel
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HydrationEntryEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.MindfulnessSessionEntity
import com.example.data.model.MoodEntryEntity
import com.example.data.model.SafetySupportHelper
import com.example.data.model.SleepLogEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.StudyTaskEntity
import com.example.data.model.UserPreferences
import com.example.data.model.WellnessGoal
import com.example.data.repository.WellnessRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitWithStatus(
    val habit: HabitEntity,
    val completedToday: Boolean,
    val daysPracticedLast7: Int
)

data class DaySummaryPoint(
    val dateKey: String,
    val label: String,
    val movementMinutes: Int,
    val studyMinutes: Int,
    val avgMoodScore: Float?,
    val habitsDone: Int
)

data class ActiveStudySessionState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val isBreakPhase: Boolean = false,
    val linkedTaskId: Long? = null,
    val subject: String = "Deep Study",
    val taskTitle: String = "Focused Study Session",
    val plannedFocusMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val totalPhaseSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val elapsedFocusSeconds: Int = 0,
    val phoneLockEnabled: Boolean = true,
    val requestOsPinning: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val distractionCount: Int = 0
)

data class WellnessUiState(
    val isLoading: Boolean = true,
    val preferences: UserPreferences = UserPreferences(),
    val todayKey: String = DateKeyUtils.todayKey(),
    val readableDate: String = DateKeyUtils.readableToday(),
    val greeting: String = DateKeyUtils.greetingForHour(),
    val dailyPlan: List<DailyPlanItem> = emptyList(),
    val habitsWithStatus: List<HabitWithStatus> = emptyList(),
    val todayMood: MoodEntryEntity? = null,
    val todayHydrationGlasses: Int = 0,
    val todayMovementMinutes: Int = 0,
    val todayMindfulnessMinutes: Int = 0,
    val todayStudyMinutes: Int = 0,
    val moodEntries: List<MoodEntryEntity> = emptyList(),
    val journalEntries: List<JournalEntryEntity> = emptyList(),
    val mindfulnessSessions: List<MindfulnessSessionEntity> = emptyList(),
    val activityLogs: List<ActivityLogEntity> = emptyList(),
    val sleepLogs: List<SleepLogEntity> = emptyList(),
    val hydrationEntries: List<HydrationEntryEntity> = emptyList(),
    val studyTasks: List<StudyTaskEntity> = emptyList(),
    val studySessions: List<StudySessionEntity> = emptyList(),
    val activeStudySession: ActiveStudySessionState = ActiveStudySessionState(),
    val selectedRangeDays: Int = 7,
    val chartPoints: List<DaySummaryPoint> = emptyList(),
    val rangeMovementMinutes: Int = 0,
    val rangeStudyMinutes: Int = 0,
    val rangeLockedStudySessions: Int = 0,
    val rangeMindfulnessCount: Int = 0,
    val rangeHabitCompletions: Int = 0,
    val rangeMoodCheckIns: Int = 0,
    val rangeAvgSleepHours: Float? = null,
    val statusMessage: String? = null,
    val showCrisisSupportBanner: Boolean = false,
    val exportedJson: String? = null
)

private data class CoreStreamsBundle(
    val prefs: UserPreferences,
    val moods: List<MoodEntryEntity>,
    val journals: List<JournalEntryEntity>,
    val mindfulness: List<MindfulnessSessionEntity>,
    val activities: List<ActivityLogEntity>
)

private data class SecondaryStreamsBundle(
    val habits: List<HabitEntity>,
    val completions: List<HabitCompletionEntity>,
    val sleeps: List<SleepLogEntity>,
    val hydrations: List<HydrationEntryEntity>,
    val planStates: List<com.example.data.model.DailyPlanStateEntity>
)

private data class StudyStreamsBundle(
    val tasks: List<StudyTaskEntity>,
    val sessions: List<StudySessionEntity>,
    val activeSession: ActiveStudySessionState
)

private data class TransientUiOverlay(
    val rangeDays: Int = 7,
    val statusMessage: String? = null,
    val showCrisisBanner: Boolean = false,
    val exportedJson: String? = null
)

class WellnessViewModel(
    private val repository: WellnessRepository
) : ViewModel() {

    private val todayKey = DateKeyUtils.todayKey()
    private val rangeDaysFlow = MutableStateFlow(7)
    private val statusMessageFlow = MutableStateFlow<String?>(null)
    private val crisisBannerFlow = MutableStateFlow(false)
    private val exportedJsonFlow = MutableStateFlow<String?>(null)
    private val activeStudyFlow = MutableStateFlow(ActiveStudySessionState())

    private var studyTimerJob: Job? = null

    private val coreBundleFlow = combine(
        repository.userPreferences,
        repository.moodEntries,
        repository.journalEntries,
        repository.mindfulnessSessions,
        repository.activityLogs
    ) { prefs, moods, journals, mindfulness, activities ->
        CoreStreamsBundle(prefs, moods, journals, mindfulness, activities)
    }

    private val secondaryBundleFlow = combine(
        repository.activeHabits,
        repository.habitCompletions,
        repository.sleepLogs,
        repository.hydrationEntries,
        repository.observePlanStatesForDate(todayKey)
    ) { habits, completions, sleeps, hydrations, planStates ->
        SecondaryStreamsBundle(habits, completions, sleeps, hydrations, planStates)
    }

    private val studyBundleFlow = combine(
        repository.studyTasks,
        repository.studySessions,
        activeStudyFlow
    ) { tasks, sessions, active ->
        StudyStreamsBundle(tasks, sessions, active)
    }

    private val transientOverlayFlow = combine(
        rangeDaysFlow,
        statusMessageFlow,
        crisisBannerFlow,
        exportedJsonFlow
    ) { rangeDays, msg, crisis, exported ->
        TransientUiOverlay(rangeDays, msg, crisis, exported)
    }

    val uiState: StateFlow<WellnessUiState> = combine(
        coreBundleFlow,
        secondaryBundleFlow,
        studyBundleFlow,
        transientOverlayFlow
    ) { core, sec, study, overlay ->
        val currentDay = DateKeyUtils.todayKey()
        val last7Keys = DateKeyUtils.recentDateKeys(7).toSet()
        val rangeKeysList = DateKeyUtils.recentDateKeys(overlay.rangeDays)
        val rangeKeysSet = rangeKeysList.toSet()

        val todayMood = core.moods.firstOrNull { it.dateKey == currentDay }
        val todayHydration = sec.hydrations.firstOrNull { it.dateKey == currentDay }?.glassesCount ?: 0
        val todayMoveMins = core.activities.filter { it.dateKey == currentDay }.sumOf { it.durationMinutes }
        val todayMindSec = core.mindfulness.filter { it.dateKey == currentDay }.sumOf { it.durationSeconds }
        val todayStudyMins = study.sessions.filter { it.dateKey == currentDay }.sumOf { it.durationMinutes }

        val habitsWithStatus = sec.habits.map { habit ->
            val habitComps = sec.completions.filter { it.habitId == habit.id }
            HabitWithStatus(
                habit = habit,
                completedToday = habitComps.any { it.dateKey == currentDay },
                daysPracticedLast7 = habitComps.count { it.dateKey in last7Keys }
            )
        }

        val dailyPlan = DailyPlanGenerator.buildPlan(
            prefs = core.prefs,
            todayKey = currentDay,
            planStates = sec.planStates,
            hasLoggedMoodToday = todayMood != null,
            hasMindfulnessToday = todayMindSec >= 30,
            hasMovementToday = todayMoveMins > 0,
            todayHydrationGlasses = todayHydration
        )

        val chartPoints = rangeKeysList.takeLast(7).map { key ->
            val dayMove = core.activities.filter { it.dateKey == key }.sumOf { it.durationMinutes }
            val dayStudy = study.sessions.filter { it.dateKey == key }.sumOf { it.durationMinutes }
            val dayMoods = core.moods.filter { it.dateKey == key }
            val avgMood = if (dayMoods.isNotEmpty()) {
                dayMoods.map { it.moodScore }.average().toFloat()
            } else null
            val dayHabits = sec.completions.count { it.dateKey == key }
            DaySummaryPoint(
                dateKey = key,
                label = DateKeyUtils.shortDayLabel(key),
                movementMinutes = dayMove,
                studyMinutes = dayStudy,
                avgMoodScore = avgMood,
                habitsDone = dayHabits
            )
        }

        val rangeSleeps = sec.sleeps.filter { it.dateKey in rangeKeysSet }
        val avgSleep = if (rangeSleeps.isNotEmpty()) {
            rangeSleeps.map { it.approxHours }.average().toFloat()
        } else null

        val rangeStudyList = study.sessions.filter { it.dateKey in rangeKeysSet }

        WellnessUiState(
            isLoading = false,
            preferences = core.prefs,
            todayKey = currentDay,
            readableDate = DateKeyUtils.readableToday(),
            greeting = DateKeyUtils.greetingForHour(),
            dailyPlan = dailyPlan,
            habitsWithStatus = habitsWithStatus,
            todayMood = todayMood,
            todayHydrationGlasses = todayHydration,
            todayMovementMinutes = todayMoveMins,
            todayMindfulnessMinutes = (todayMindSec + 30) / 60,
            todayStudyMinutes = todayStudyMins,
            moodEntries = core.moods,
            journalEntries = core.journals,
            mindfulnessSessions = core.mindfulness,
            activityLogs = core.activities,
            sleepLogs = sec.sleeps,
            hydrationEntries = sec.hydrations,
            studyTasks = study.tasks,
            studySessions = study.sessions,
            activeStudySession = study.activeSession,
            selectedRangeDays = overlay.rangeDays,
            chartPoints = chartPoints,
            rangeMovementMinutes = core.activities.filter { it.dateKey in rangeKeysSet }.sumOf { it.durationMinutes },
            rangeStudyMinutes = rangeStudyList.sumOf { it.durationMinutes },
            rangeLockedStudySessions = rangeStudyList.count { it.phoneLocked },
            rangeMindfulnessCount = core.mindfulness.count { it.dateKey in rangeKeysSet },
            rangeHabitCompletions = sec.completions.count { it.dateKey in rangeKeysSet },
            rangeMoodCheckIns = core.moods.count { it.dateKey in rangeKeysSet },
            rangeAvgSleepHours = avgSleep,
            statusMessage = overlay.statusMessage,
            showCrisisSupportBanner = overlay.showCrisisBanner,
            exportedJson = overlay.exportedJson
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WellnessUiState()
    )

    // Study Focus Timer & Phone Lock Controls
    fun startStudySession(
        linkedTaskId: Long?,
        subject: String,
        taskTitle: String,
        focusMinutes: Int,
        breakMinutes: Int,
        lockPhone: Boolean,
        requestOsPinning: Boolean,
        keepScreenAwake: Boolean
    ) {
        val safeFocusMin = focusMinutes.coerceIn(5, 180)
        val safeBreakMin = breakMinutes.coerceIn(0, 30)
        val totalSec = safeFocusMin * 60
        activeStudyFlow.value = ActiveStudySessionState(
            isActive = true,
            isPaused = false,
            isBreakPhase = false,
            linkedTaskId = linkedTaskId,
            subject = subject.ifBlank { "Study" },
            taskTitle = taskTitle.ifBlank { "Focused Study Block" },
            plannedFocusMinutes = safeFocusMin,
            breakMinutes = safeBreakMin,
            totalPhaseSeconds = totalSec,
            remainingSeconds = totalSec,
            elapsedFocusSeconds = 0,
            phoneLockEnabled = lockPhone,
            requestOsPinning = lockPhone && requestOsPinning,
            keepScreenAwake = keepScreenAwake,
            distractionCount = 0
        )
        runStudyTicker()
    }

    private fun runStudyTicker() {
        studyTimerJob?.cancel()
        studyTimerJob = viewModelScope.launch {
            var lastTickMillis = System.currentTimeMillis()
            while (true) {
                delay(1000L)
                val now = System.currentTimeMillis()
                val deltaSec = ((now - lastTickMillis) / 1000L).toInt().coerceIn(1, 60)
                lastTickMillis = now

                val current = activeStudyFlow.value
                if (!current.isActive) break
                if (current.isPaused) continue

                val nextRemaining = (current.remainingSeconds - deltaSec).coerceAtLeast(0)
                val nextElapsedFocus = if (!current.isBreakPhase) {
                    current.elapsedFocusSeconds + deltaSec
                } else {
                    current.elapsedFocusSeconds
                }

                if (nextRemaining == 0) {
                    if (!current.isBreakPhase) {
                        // Completed full study block! Persist immediately.
                        val minsLogged = current.plannedFocusMinutes.coerceAtLeast(1)
                        repository.logStudySession(
                            linkedTaskId = current.linkedTaskId,
                            subject = current.subject,
                            taskTitle = current.taskTitle,
                            durationMinutes = minsLogged,
                            phoneLocked = current.phoneLockEnabled,
                            distractionCount = current.distractionCount,
                            completedFullBlock = true
                        )
                        if (current.breakMinutes > 0) {
                            val breakSec = current.breakMinutes * 60
                            activeStudyFlow.value = current.copy(
                                isBreakPhase = true,
                                totalPhaseSeconds = breakSec,
                                remainingSeconds = breakSec,
                                elapsedFocusSeconds = nextElapsedFocus
                            )
                            statusMessageFlow.value = "Study block complete! Enjoy a ${current.breakMinutes}m mindful break."
                        } else {
                            activeStudyFlow.value = ActiveStudySessionState()
                            statusMessageFlow.value = "Logged ${minsLogged}m focused study session!"
                            break
                        }
                    } else {
                        // Break finished
                        activeStudyFlow.value = ActiveStudySessionState()
                        statusMessageFlow.value = "Break finished! Phone unlocked."
                        break
                    }
                } else {
                    activeStudyFlow.value = current.copy(
                        remainingSeconds = nextRemaining,
                        elapsedFocusSeconds = nextElapsedFocus
                    )
                }
            }
        }
    }

    fun togglePauseStudySession() {
        activeStudyFlow.update { state ->
            if (!state.isActive) state else state.copy(isPaused = !state.isPaused)
        }
    }

    fun recordStudyDistractionAttempt() {
        activeStudyFlow.update { state ->
            if (state.isActive && state.phoneLockEnabled && !state.isBreakPhase) {
                state.copy(distractionCount = state.distractionCount + 1)
            } else {
                state
            }
        }
    }

    fun finishOrUnlockStudySession() {
        val current = activeStudyFlow.value
        studyTimerJob?.cancel()
        activeStudyFlow.value = ActiveStudySessionState()

        if (!current.isActive) return
        if (!current.isBreakPhase) {
            val minsSpent = (current.elapsedFocusSeconds + 30) / 60
            if (minsSpent >= 1) {
                viewModelScope.launch {
                    repository.logStudySession(
                        linkedTaskId = current.linkedTaskId,
                        subject = current.subject,
                        taskTitle = current.taskTitle,
                        durationMinutes = minsSpent,
                        phoneLocked = current.phoneLockEnabled,
                        distractionCount = current.distractionCount,
                        completedFullBlock = false
                    )
                    statusMessageFlow.value = "Phone unlocked. Logged ${minsSpent}m of focused study."
                }
            } else {
                statusMessageFlow.value = "Study lock ended."
            }
        } else {
            statusMessageFlow.value = "Break ended — ready whenever you are."
        }
    }

    fun addStudyTask(subject: String, title: String, plannedMinutes: Int, timeSlot: String) {
        viewModelScope.launch {
            val result = repository.createStudyTask(subject, title, plannedMinutes, timeSlot)
            statusMessageFlow.value = result.fold(
                onSuccess = { "Added study block: $title" },
                onFailure = { it.message ?: "Check study task details." }
            )
        }
    }

    fun toggleStudyTaskCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleStudyTaskCompleted(taskId, completed)
        }
    }

    fun deleteStudyTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteStudyTask(taskId)
            statusMessageFlow.value = "Study task removed."
        }
    }

    fun deleteStudySession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteStudySession(sessionId)
            statusMessageFlow.value = "Study log entry removed."
        }
    }

    fun updateStudyGoalMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.updateStudyGoalMinutes(minutes)
            statusMessageFlow.value = "Daily study goal updated to $minutes min."
        }
    }

    // Existing Wellness Controls
    fun clearStatusMessage() {
        statusMessageFlow.value = null
    }

    fun dismissCrisisBanner() {
        crisisBannerFlow.value = false
    }

    fun setDateRangeDays(days: Int) {
        rangeDaysFlow.value = days.coerceIn(7, 30)
    }

    fun completeOnboarding(
        displayName: String,
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel,
        remindersEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(
                displayName = displayName,
                goals = goals,
                dailyMinutes = dailyMinutes,
                experienceLevel = experienceLevel,
                remindersEnabled = remindersEnabled,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute
            )
            statusMessageFlow.value = "Welcome! Small steps count."
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            repository.skipOnboarding()
        }
    }

    fun updateGoalsAndTime(
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel
    ) {
        viewModelScope.launch {
            repository.updateGoalsAndTime(goals, dailyMinutes, experienceLevel)
            statusMessageFlow.value = "Updated your daily routine preferences."
        }
    }

    fun toggleRestDay(isRestDay: Boolean) {
        viewModelScope.launch {
            repository.setRestDayToday(isRestDay)
            statusMessageFlow.value = if (isRestDay) {
                "Rest day enabled. Taking care of your energy counts."
            } else {
                "Returned to your regular daily plan."
            }
        }
    }

    fun markPlanItemStatus(item: DailyPlanItem, status: String) {
        viewModelScope.launch {
            repository.savePlanItemState(
                dateKey = DateKeyUtils.todayKey(),
                itemKey = item.key,
                status = status,
                swappedIndex = item.swapIndex
            )
            statusMessageFlow.value = when (status) {
                "COMPLETED" -> "Nice work taking a small step today."
                "SKIPPED" -> "Skipped for today — flexibility keeps routines sustainable."
                else -> null
            }
        }
    }

    fun swapPlanItem(item: DailyPlanItem) {
        viewModelScope.launch {
            repository.savePlanItemState(
                dateKey = DateKeyUtils.todayKey(),
                itemKey = item.key,
                status = "PENDING",
                swappedIndex = item.swapIndex + 1
            )
            statusMessageFlow.value = "Swapped for an alternative gentle suggestion."
        }
    }

    fun logMoodCheckIn(moodScore: Int, stressLevel: Int?, tags: List<String>, note: String) {
        if (SafetySupportHelper.containsCrisisSignal(note)) {
            crisisBannerFlow.value = true
        }
        viewModelScope.launch {
            val result = repository.logMood(moodScore, stressLevel, tags, note)
            statusMessageFlow.value = result.fold(
                onSuccess = { "Mood check-in saved privately on this device." },
                onFailure = { it.message ?: "Unable to save mood check-in." }
            )
        }
    }

    fun deleteMood(id: Long) {
        viewModelScope.launch {
            repository.deleteMood(id)
            statusMessageFlow.value = "Mood entry deleted."
        }
    }

    fun saveJournalEntry(prompt: String, content: String) {
        if (SafetySupportHelper.containsCrisisSignal(content)) {
            crisisBannerFlow.value = true
        }
        viewModelScope.launch {
            val result = repository.addJournalEntry(prompt, content)
            statusMessageFlow.value = result.fold(
                onSuccess = { "Reflection saved privately on your device." },
                onFailure = { it.message ?: "Please enter text before saving." }
            )
        }
    }

    fun deleteJournal(id: Long) {
        viewModelScope.launch {
            repository.deleteJournal(id)
            statusMessageFlow.value = "Journal entry deleted."
        }
    }

    fun recordMindfulnessSession(sessionType: String, title: String, durationSeconds: Int) {
        viewModelScope.launch {
            val result = repository.logMindfulnessSession(sessionType, title, durationSeconds)
            statusMessageFlow.value = result.fold(
                onSuccess = { "Logged $title. Gentle pauses add up." },
                onFailure = { it.message }
            )
        }
    }

    fun deleteMindfulnessSession(id: Long) {
        viewModelScope.launch {
            repository.deleteMindfulnessSession(id)
            statusMessageFlow.value = "Mindfulness session removed."
        }
    }

    fun recordActivity(
        activityType: String,
        title: String,
        durationMinutes: Int,
        intensity: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.logActivity(activityType, title, durationMinutes, intensity, notes)
            statusMessageFlow.value = result.fold(
                onSuccess = { "Movement logged! Every minute counts." },
                onFailure = { it.message ?: "Check activity duration." }
            )
        }
    }

    fun deleteActivityLog(id: Long) {
        viewModelScope.launch {
            repository.deleteActivityLog(id)
            statusMessageFlow.value = "Movement log deleted."
        }
    }

    fun addHabit(title: String, category: String, whyNote: String) {
        viewModelScope.launch {
            val result = repository.createHabit(title, category, whyNote)
            statusMessageFlow.value = result.fold(
                onSuccess = { "Added habit: $title" },
                onFailure = { it.message ?: "Please enter a habit name." }
            )
        }
    }

    fun toggleHabitToday(habitId: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, completed)
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            statusMessageFlow.value = "Habit removed."
        }
    }

    fun recordSleep(
        bedTimeText: String,
        wakeTimeText: String,
        approxHours: Float,
        qualityRating: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.logSleep(bedTimeText, wakeTimeText, approxHours, qualityRating, notes)
            statusMessageFlow.value = result.fold(
                onSuccess = { "Sleep routine logged." },
                onFailure = { it.message ?: "Please enter valid approximate hours." }
            )
        }
    }

    fun deleteSleepLog(id: Long) {
        viewModelScope.launch {
            repository.deleteSleepLog(id)
            statusMessageFlow.value = "Sleep log deleted."
        }
    }

    fun adjustHydration(delta: Int) {
        viewModelScope.launch {
            repository.adjustHydrationForDate(delta)
        }
    }

    fun deleteHydrationEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteHydrationEntry(id)
            statusMessageFlow.value = "Hydration entry deleted."
        }
    }

    fun updateHydrationGoal(glasses: Int) {
        viewModelScope.launch {
            repository.updateHydrationGoal(glasses)
            statusMessageFlow.value = "Updated hydration goal to $glasses glasses."
        }
    }

    fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.updateReminderSettings(enabled, hour, minute)
            statusMessageFlow.value = if (enabled) {
                val formatted = String.format("%02d:%02d", hour, minute)
                "Gentle daily reminder set for $formatted."
            } else {
                "Daily reminders turned off."
            }
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun updateReducedMotion(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateReducedMotion(enabled)
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
            statusMessageFlow.value = "Loaded clearly marked [Demo] sample entries."
        }
    }

    fun removeDemoData() {
        viewModelScope.launch {
            repository.removeDemoData()
            statusMessageFlow.value = "Removed all [Demo] sample entries."
        }
    }

    fun prepareDataExport() {
        viewModelScope.launch {
            exportedJsonFlow.value = repository.exportAllDataAsJson()
        }
    }

    fun clearExportPreview() {
        exportedJsonFlow.value = null
    }

    fun deleteAllData() {
        studyTimerJob?.cancel()
        activeStudyFlow.value = ActiveStudySessionState()
        viewModelScope.launch {
            repository.deleteAllUserData()
            exportedJsonFlow.value = null
            statusMessageFlow.value = "All local data has been permanently deleted."
        }
    }
}

class WellnessViewModelFactory(
    private val repository: WellnessRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WellnessViewModel::class.java)) {
            return WellnessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

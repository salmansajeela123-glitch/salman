package com.example.data.repository

import com.example.data.local.PreferencesDataSource
import com.example.data.local.WellnessDao
import com.example.data.model.ActivityLogEntity
import com.example.data.model.DailyPlanStateEntity
import com.example.data.model.DateKeyUtils
import com.example.data.model.ExperienceLevel
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HydrationEntryEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.MindfulnessSessionEntity
import com.example.data.model.MoodEntryEntity
import com.example.data.model.SleepLogEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.StudyTaskEntity
import com.example.data.model.UserPreferences
import com.example.data.model.WellnessGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class WellnessRepository(
    private val dao: WellnessDao,
    private val preferences: PreferencesDataSource
) {
    val userPreferences: Flow<UserPreferences> = preferences.preferencesFlow
    val moodEntries: Flow<List<MoodEntryEntity>> = dao.observeMoodEntries()
    val journalEntries: Flow<List<JournalEntryEntity>> = dao.observeJournalEntries()
    val mindfulnessSessions: Flow<List<MindfulnessSessionEntity>> = dao.observeMindfulnessSessions()
    val activityLogs: Flow<List<ActivityLogEntity>> = dao.observeActivityLogs()
    val activeHabits: Flow<List<HabitEntity>> = dao.observeActiveHabits()
    val habitCompletions: Flow<List<HabitCompletionEntity>> = dao.observeHabitCompletions()
    val sleepLogs: Flow<List<SleepLogEntity>> = dao.observeSleepLogs()
    val hydrationEntries: Flow<List<HydrationEntryEntity>> = dao.observeHydrationEntries()
    val studyTasks: Flow<List<StudyTaskEntity>> = dao.observeStudyTasks()
    val studySessions: Flow<List<StudySessionEntity>> = dao.observeStudySessions()

    fun observePlanStatesForDate(dateKey: String): Flow<List<DailyPlanStateEntity>> =
        dao.observeDailyPlanStates(dateKey)

    suspend fun completeOnboarding(
        displayName: String,
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel,
        remindersEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        preferences.completeOnboarding(
            displayName = displayName,
            goals = goals,
            dailyMinutes = dailyMinutes,
            experienceLevel = experienceLevel,
            remindersEnabled = remindersEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )
        seedStarterHabitsIfEmpty(goals)
    }

    suspend fun skipOnboarding() {
        preferences.skipOnboarding()
        seedStarterHabitsIfEmpty(
            setOf(WellnessGoal.MINDFULNESS, WellnessGoal.DAILY_MOVEMENT, WellnessGoal.CONSISTENCY)
        )
    }

    private suspend fun seedStarterHabitsIfEmpty(goals: Set<WellnessGoal>) {
        val current = dao.observeActiveHabits().first()
        if (current.isNotEmpty()) return

        val starter = mutableListOf<HabitEntity>()
        if (goals.contains(WellnessGoal.MINDFULNESS) || goals.contains(WellnessGoal.STRESS_RELIEF)) {
            starter.add(
                HabitEntity(
                    title = "Take 3 slow breaths before midday",
                    category = "Mind",
                    whyNote = "Creates a calm pause in the middle of the day"
                )
            )
        }
        if (goals.contains(WellnessGoal.DAILY_MOVEMENT) || goals.contains(WellnessGoal.FLEXIBILITY)) {
            starter.add(
                HabitEntity(
                    title = "5-minute stretch or easy walk",
                    category = "Body",
                    whyNote = "Helps ease stiffness with zero pressure"
                )
            )
        }
        starter.add(
            HabitEntity(
                title = "Sip a glass of water mindfully",
                category = "Body",
                whyNote = "Supports steady everyday hydration"
            )
        )
        starter.take(3).forEach { dao.insertHabit(it) }
    }

    suspend fun updateGoalsAndTime(
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel
    ) {
        preferences.updateGoalsAndTime(goals, dailyMinutes, experienceLevel)
    }

    suspend fun updateHydrationGoal(glasses: Int) {
        preferences.updateHydrationGoal(glasses.coerceIn(2, 16))
    }

    suspend fun updateStudyGoalMinutes(minutes: Int) {
        preferences.updateStudyGoalMinutes(minutes.coerceIn(15, 600))
    }

    suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        preferences.updateReminderSettings(enabled, hour, minute)
    }

    suspend fun updateThemeMode(mode: String) {
        preferences.updateThemeMode(mode)
    }

    suspend fun updateReducedMotion(enabled: Boolean) {
        preferences.updateReducedMotion(enabled)
    }

    suspend fun setRestDayToday(isRestDay: Boolean, dateKey: String = DateKeyUtils.todayKey()) {
        preferences.toggleRestDay(dateKey, isRestDay)
    }

    // Validated Mutations
    suspend fun logMood(
        moodScore: Int,
        stressLevel: Int?,
        tags: List<String>,
        note: String,
        dateKey: String = DateKeyUtils.todayKey()
    ): Result<Long> {
        if (moodScore !in 1..5) {
            return Result.failure(IllegalArgumentException("Please select a mood from 1 to 5."))
        }
        val validStress = stressLevel?.coerceIn(1, 5)
        val cleanTags = tags.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",")
        val id = dao.insertMoodEntry(
            MoodEntryEntity(
                moodScore = moodScore,
                stressLevel = validStress,
                tagsCsv = cleanTags,
                note = note.trim().take(600),
                dateKey = dateKey
            )
        )
        return Result.success(id)
    }

    suspend fun deleteMood(id: Long) = dao.deleteMoodEntry(id)

    suspend fun addJournalEntry(
        prompt: String,
        content: String,
        dateKey: String = DateKeyUtils.todayKey()
    ): Result<Long> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Write a few words before saving your reflection."))
        }
        val id = dao.insertJournalEntry(
            JournalEntryEntity(
                prompt = prompt.trim(),
                content = trimmed.take(3000),
                dateKey = dateKey
            )
        )
        return Result.success(id)
    }

    suspend fun deleteJournal(id: Long) = dao.deleteJournalEntry(id)

    suspend fun logMindfulnessSession(
        sessionType: String,
        title: String,
        durationSeconds: Int,
        dateKey: String = DateKeyUtils.todayKey()
    ): Result<Long> {
        if (durationSeconds < 10) {
            return Result.failure(IllegalArgumentException("Session should be at least 10 seconds."))
        }
        val id = dao.insertMindfulnessSession(
            MindfulnessSessionEntity(
                sessionType = sessionType,
                title = title.trim().ifEmpty { "Mindful Pause" },
                durationSeconds = durationSeconds.coerceAtMost(7200),
                dateKey = dateKey
            )
        )
        return Result.success(id)
    }

    suspend fun deleteMindfulnessSession(id: Long) = dao.deleteMindfulnessSession(id)

    suspend fun logActivity(
        activityType: String,
        title: String,
        durationMinutes: Int,
        intensity: String = "Gentle",
        notes: String = "",
        dateKey: String = DateKeyUtils.todayKey()
    ): Result<Long> {
        if (durationMinutes !in 1..360) {
            return Result.failure(IllegalArgumentException("Please enter a duration between 1 and 360 minutes."))
        }
        val cleanTitle = title.trim().ifEmpty { activityType.trim().ifEmpty { "Movement" } }
        val id = dao.insertActivityLog(
            ActivityLogEntity(
                activityType = activityType.trim().ifEmpty { "Movement" },
                title = cleanTitle.take(100),
                durationMinutes = durationMinutes,
                intensity = intensity,
                notes = notes.trim().take(400),
                dateKey = dateKey
            )
        )
        return Result.success(id)
    }

    suspend fun deleteActivityLog(id: Long) = dao.deleteActivityLog(id)

    suspend fun createHabit(title: String, category: String, whyNote: String): Result<Long> {
        val cleanTitle = title.trim()
        if (cleanTitle.length < 2) {
            return Result.failure(IllegalArgumentException("Please enter a short name for your habit."))
        }
        val id = dao.insertHabit(
            HabitEntity(
                title = cleanTitle.take(80),
                category = category.ifBlank { "Mind & Body" },
                whyNote = whyNote.trim().take(160)
            )
        )
        return Result.success(id)
    }

    suspend fun toggleHabitCompletion(
        habitId: Long,
        completed: Boolean,
        dateKey: String = DateKeyUtils.todayKey()
    ) {
        if (completed) {
            dao.insertHabitCompletion(HabitCompletionEntity(habitId = habitId, dateKey = dateKey))
        } else {
            dao.deleteHabitCompletion(habitId = habitId, dateKey = dateKey)
        }
    }

    suspend fun deleteHabit(habitId: Long) {
        dao.deleteCompletionsForHabit(habitId)
        dao.deleteHabit(habitId)
    }

    suspend fun logSleep(
        bedTimeText: String,
        wakeTimeText: String,
        approxHours: Float,
        qualityRating: Int,
        notes: String = "",
        dateKey: String = DateKeyUtils.todayKey()
    ): Result<Long> {
        if (approxHours <= 0f || approxHours > 20f) {
            return Result.failure(IllegalArgumentException("Please enter approximate rest hours between 0.5 and 20."))
        }
        val id = dao.insertSleepLog(
            SleepLogEntity(
                bedTimeText = bedTimeText.trim().ifEmpty { "22:30" },
                wakeTimeText = wakeTimeText.trim().ifEmpty { "06:45" },
                approxHours = approxHours,
                qualityRating = qualityRating.coerceIn(1, 5),
                notes = notes.trim().take(300),
                dateKey = dateKey
            )
        )
        return Result.success(id)
    }

    suspend fun deleteSleepLog(id: Long) = dao.deleteSleepLog(id)

    suspend fun adjustHydrationForDate(delta: Int, dateKey: String = DateKeyUtils.todayKey()): Int {
        val existing = dao.getHydrationForDate(dateKey)
        val nextCount = ((existing?.glassesCount ?: 0) + delta).coerceIn(0, 30)
        if (existing != null) {
            dao.insertHydrationEntry(existing.copy(glassesCount = nextCount, timestamp = System.currentTimeMillis()))
        } else if (nextCount > 0) {
            dao.insertHydrationEntry(HydrationEntryEntity(glassesCount = nextCount, dateKey = dateKey))
        }
        return nextCount
    }

    suspend fun deleteHydrationEntry(id: Long) = dao.deleteHydrationEntry(id)

    suspend fun savePlanItemState(
        dateKey: String,
        itemKey: String,
        status: String,
        swappedIndex: Int = 0
    ) {
        dao.upsertDailyPlanState(
            DailyPlanStateEntity(
                dateKey = dateKey,
                itemKey = itemKey,
                status = status,
                swappedIndex = swappedIndex
            )
        )
    }

    // Study Time Management & Focus Sessions
    suspend fun createStudyTask(
        subject: String,
        title: String,
        plannedMinutes: Int,
        timeSlot: String,
        dateKey: String = DateKeyUtils.todayKey()
    ): Result<Long> {
        val cleanTitle = title.trim()
        if (cleanTitle.length < 2) {
            return Result.failure(IllegalArgumentException("Please enter a study task title."))
        }
        if (plannedMinutes !in 5..300) {
            return Result.failure(IllegalArgumentException("Planned study block must be between 5 and 300 minutes."))
        }
        val id = dao.insertStudyTask(
            StudyTaskEntity(
                subject = subject.trim().ifBlank { "General Study" },
                title = cleanTitle.take(100),
                plannedMinutes = plannedMinutes,
                timeSlot = timeSlot.ifBlank { "Afternoon" },
                dateKey = dateKey
            )
        )
        return Result.success(id)
    }

    suspend fun toggleStudyTaskCompleted(taskId: Long, completed: Boolean) {
        dao.setStudyTaskCompleted(taskId, completed)
    }

    suspend fun deleteStudyTask(taskId: Long) {
        dao.deleteStudyTask(taskId)
    }

    suspend fun logStudySession(
        linkedTaskId: Long?,
        subject: String,
        taskTitle: String,
        durationMinutes: Int,
        phoneLocked: Boolean,
        distractionCount: Int = 0,
        completedFullBlock: Boolean = true,
        dateKey: String = DateKeyUtils.todayKey()
    ): Result<Long> {
        if (durationMinutes < 1) {
            return Result.failure(IllegalArgumentException("Study session must be at least 1 minute to log."))
        }
        if (linkedTaskId != null && linkedTaskId > 0L) {
            dao.addMinutesToStudyTask(linkedTaskId, durationMinutes)
        }
        val id = dao.insertStudySession(
            StudySessionEntity(
                subject = subject.trim().ifBlank { "Study" },
                taskTitle = taskTitle.trim().ifBlank { "Focused Study Block" }.take(100),
                durationMinutes = durationMinutes.coerceAtMost(480),
                phoneLocked = phoneLocked,
                distractionCount = distractionCount.coerceAtLeast(0),
                completedFullBlock = completedFullBlock,
                dateKey = dateKey
            )
        )
        return Result.success(id)
    }

    suspend fun deleteStudySession(id: Long) {
        dao.deleteStudySession(id)
    }

    // Clearly Marked Sample/Demo Data (easy to load & remove in 1 tap)
    suspend fun loadDemoData() {
        dao.removeAllDemoEntries()
        val recentDays = DateKeyUtils.recentDateKeys(6)
        val demoHabitId = dao.insertHabit(
            HabitEntity(
                title = "[Demo] Step outside for morning light",
                category = "Body",
                whyNote = "Sample habit showing how gentle consistency looks",
                isDemo = true
            )
        )
        dao.insertStudyTask(
            StudyTaskEntity(
                subject = "Exam Prep",
                title = "[Demo] Review Chapter 3 flashcards",
                plannedMinutes = 25,
                timeSlot = "Morning",
                isCompleted = false,
                completedMinutes = 0,
                dateKey = DateKeyUtils.todayKey(),
                isDemo = true
            )
        )
        recentDays.forEachIndexed { idx, dayKey ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -(recentDays.size - 1 - idx))
            }
            val ts = cal.timeInMillis
            dao.insertMoodEntry(
                MoodEntryEntity(
                    moodScore = listOf(3, 4, 3, 4, 5, 4)[idx % 6],
                    stressLevel = listOf(3, 2, 3, 2, 2, 2)[idx % 6],
                    tagsCsv = if (idx % 2 == 0) "Calm,Rested" else "Steady,Focused",
                    note = "[Demo] Took a short breather between study blocks.",
                    dateKey = dayKey,
                    timestamp = ts,
                    isDemo = true
                )
            )
            dao.insertStudySession(
                StudySessionEntity(
                    subject = if (idx % 2 == 0) "Reading" else "Problem Solving",
                    taskTitle = if (idx % 2 == 0) "[Demo] Deep Reading Pomodoro" else "[Demo] Practice Set Focus",
                    durationMinutes = listOf(25, 45, 25, 50, 30, 25)[idx % 6],
                    phoneLocked = true,
                    distractionCount = 0,
                    completedFullBlock = true,
                    dateKey = dayKey,
                    timestamp = ts,
                    isDemo = true
                )
            )
            if (idx % 2 == 0) {
                dao.insertHabitCompletion(
                    HabitCompletionEntity(
                        habitId = demoHabitId,
                        dateKey = dayKey,
                        timestamp = ts,
                        isDemo = true
                    )
                )
                dao.insertMindfulnessSession(
                    MindfulnessSessionEntity(
                        sessionType = "Breathing",
                        title = "[Demo] Longer Exhale (4-2-6)",
                        durationSeconds = 120,
                        dateKey = dayKey,
                        timestamp = ts,
                        isDemo = true
                    )
                )
            }
            dao.insertActivityLog(
                ActivityLogEntity(
                    activityType = if (idx % 2 == 0) "Walking" else "Stretching",
                    title = if (idx % 2 == 0) "[Demo] Easy Neighborhood Walk" else "[Demo] Desk Unwind Stretch",
                    durationMinutes = listOf(10, 6, 15, 8, 12, 10)[idx % 6],
                    intensity = "Gentle",
                    notes = "[Demo] Sample movement entry",
                    dateKey = dayKey,
                    timestamp = ts,
                    isDemo = true
                )
            )
            dao.insertSleepLog(
                SleepLogEntity(
                    bedTimeText = "22:45",
                    wakeTimeText = "06:45",
                    approxHours = listOf(7.2f, 7.5f, 6.8f, 7.8f, 8.0f, 7.4f)[idx % 6],
                    qualityRating = 4,
                    notes = "[Demo] Sample sleep routine log",
                    dateKey = dayKey,
                    timestamp = ts,
                    isDemo = true
                )
            )
        }
        dao.insertJournalEntry(
            JournalEntryEntity(
                prompt = "What went well today, even if it was small?",
                content = "[Demo] Paused for five minutes to stretch my shoulders and drink water.",
                dateKey = DateKeyUtils.todayKey(),
                isDemo = true
            )
        )
        preferences.setHasDemoData(true)
    }

    suspend fun removeDemoData() {
        dao.removeAllDemoEntries()
        preferences.setHasDemoData(false)
    }

    suspend fun deleteAllUserData() {
        dao.wipeAllTables()
        preferences.clearAllPreferences()
    }

    suspend fun exportAllDataAsJson(): String {
        val prefs = preferences.preferencesFlow.first()
        val moods = dao.observeMoodEntries().first()
        val journals = dao.observeJournalEntries().first()
        val mindfulness = dao.observeMindfulnessSessions().first()
        val activities = dao.observeActivityLogs().first()
        val habits = dao.observeActiveHabits().first()
        val completions = dao.observeHabitCompletions().first()
        val sleeps = dao.observeSleepLogs().first()
        val hydrations = dao.observeHydrationEntries().first()
        val sTasks = dao.observeStudyTasks().first()
        val sSessions = dao.observeStudySessions().first()

        val root = JSONObject().apply {
            put("app", "Mind & Body")
            put("exportedAtMillis", System.currentTimeMillis())
            put("storageNotice", "Exported from local on-device Room & DataStore storage.")
            put("preferences", JSONObject().apply {
                put("displayName", prefs.displayName)
                put("goals", JSONArray(prefs.selectedGoals.map { it.name }))
                put("dailyMinutesAvailable", prefs.dailyMinutesAvailable)
                put("experienceLevel", prefs.experienceLevel.name)
                put("hydrationGoalGlasses", prefs.hydrationGoalGlasses)
                put("dailyStudyGoalMinutes", prefs.dailyStudyGoalMinutes)
            })
            put("studyTasks", JSONArray().apply {
                sTasks.forEach { t ->
                    put(JSONObject().apply {
                        put("date", t.dateKey)
                        put("subject", t.subject)
                        put("title", t.title)
                        put("plannedMinutes", t.plannedMinutes)
                        put("completedMinutes", t.completedMinutes)
                        put("timeSlot", t.timeSlot)
                        put("isCompleted", t.isCompleted)
                    })
                }
            })
            put("studySessions", JSONArray().apply {
                sSessions.forEach { s ->
                    put(JSONObject().apply {
                        put("date", s.dateKey)
                        put("subject", s.subject)
                        put("taskTitle", s.taskTitle)
                        put("durationMinutes", s.durationMinutes)
                        put("phoneLocked", s.phoneLocked)
                        put("distractionCount", s.distractionCount)
                    })
                }
            })
            put("moodCheckIns", JSONArray().apply {
                moods.forEach { m ->
                    put(JSONObject().apply {
                        put("date", m.dateKey)
                        put("moodScore", m.moodScore)
                        put("stressLevel", m.stressLevel ?: JSONObject.NULL)
                        put("tags", m.tagsCsv)
                        put("note", m.note)
                        put("isDemo", m.isDemo)
                    })
                }
            })
            put("journalEntries", JSONArray().apply {
                journals.forEach { j ->
                    put(JSONObject().apply {
                        put("date", j.dateKey)
                        put("prompt", j.prompt)
                        put("content", j.content)
                        put("isDemo", j.isDemo)
                    })
                }
            })
            put("mindfulnessSessions", JSONArray().apply {
                mindfulness.forEach { s ->
                    put(JSONObject().apply {
                        put("date", s.dateKey)
                        put("type", s.sessionType)
                        put("title", s.title)
                        put("durationSeconds", s.durationSeconds)
                    })
                }
            })
            put("activityLogs", JSONArray().apply {
                activities.forEach { a ->
                    put(JSONObject().apply {
                        put("date", a.dateKey)
                        put("type", a.activityType)
                        put("title", a.title)
                        put("durationMinutes", a.durationMinutes)
                        put("intensity", a.intensity)
                        put("notes", a.notes)
                    })
                }
            })
            put("habits", JSONArray().apply {
                habits.forEach { h ->
                    put(JSONObject().apply {
                        put("id", h.id)
                        put("title", h.title)
                        put("category", h.category)
                    })
                }
            })
            put("habitCompletionsCount", completions.size)
            put("sleepLogs", JSONArray().apply {
                sleeps.forEach { sl ->
                    put(JSONObject().apply {
                        put("date", sl.dateKey)
                        put("bedTime", sl.bedTimeText)
                        put("wakeTime", sl.wakeTimeText)
                        put("approxHours", sl.approxHours.toDouble())
                        put("qualityRating", sl.qualityRating)
                    })
                }
            })
            put("hydrationLogs", JSONArray().apply {
                hydrations.forEach { hy ->
                    put(JSONObject().apply {
                        put("date", hy.dateKey)
                        put("glasses", hy.glassesCount)
                    })
                }
            })
        }
        return root.toString(2)
    }
}

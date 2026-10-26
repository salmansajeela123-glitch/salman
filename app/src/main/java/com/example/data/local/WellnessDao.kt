package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.ActivityLogEntity
import com.example.data.model.DailyPlanStateEntity
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HydrationEntryEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.MindfulnessSessionEntity
import com.example.data.model.MoodEntryEntity
import com.example.data.model.SleepLogEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.StudyTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDao {

    // Mood Check-ins
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun observeMoodEntries(): Flow<List<MoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(entry: MoodEntryEntity): Long

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteMoodEntry(id: Long)

    // Journal Entries
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun observeJournalEntries(): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalEntry(id: Long)

    // Mindfulness & Breathing Sessions
    @Query("SELECT * FROM mindfulness_sessions ORDER BY timestamp DESC")
    fun observeMindfulnessSessions(): Flow<List<MindfulnessSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMindfulnessSession(session: MindfulnessSessionEntity): Long

    @Query("DELETE FROM mindfulness_sessions WHERE id = :id")
    suspend fun deleteMindfulnessSession(id: Long)

    // Activity Logs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun observeActivityLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity): Long

    @Query("DELETE FROM activity_logs WHERE id = :id")
    suspend fun deleteActivityLog(id: Long)

    // Habits & Completions
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun observeActiveHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: Long)

    @Query("SELECT * FROM habit_completions ORDER BY timestamp DESC")
    fun observeHabitCompletions(): Flow<List<HabitCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabitCompletion(completion: HabitCompletionEntity): Long

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun deleteHabitCompletion(habitId: Long, dateKey: String)

    // Sleep Logs
    @Query("SELECT * FROM sleep_logs ORDER BY timestamp DESC")
    fun observeSleepLogs(): Flow<List<SleepLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLogEntity): Long

    @Query("DELETE FROM sleep_logs WHERE id = :id")
    suspend fun deleteSleepLog(id: Long)

    // Hydration Entries
    @Query("SELECT * FROM hydration_entries ORDER BY timestamp DESC")
    fun observeHydrationEntries(): Flow<List<HydrationEntryEntity>>

    @Query("SELECT * FROM hydration_entries WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getHydrationForDate(dateKey: String): HydrationEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHydrationEntry(entry: HydrationEntryEntity): Long

    @Query("DELETE FROM hydration_entries WHERE id = :id")
    suspend fun deleteHydrationEntry(id: Long)

    // Daily Plan Overrides
    @Query("SELECT * FROM daily_plan_states WHERE dateKey = :dateKey")
    fun observeDailyPlanStates(dateKey: String): Flow<List<DailyPlanStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyPlanState(state: DailyPlanStateEntity): Long

    // Study Tasks (Time Management Planner)
    @Query("SELECT * FROM study_tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun observeStudyTasks(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE id = :id LIMIT 1")
    suspend fun getStudyTaskById(id: Long): StudyTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyTask(task: StudyTaskEntity): Long

    @Query("UPDATE study_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setStudyTaskCompleted(id: Long, completed: Boolean)

    @Query("UPDATE study_tasks SET completedMinutes = completedMinutes + :addedMinutes, isCompleted = CASE WHEN (completedMinutes + :addedMinutes) >= plannedMinutes THEN 1 ELSE isCompleted END WHERE id = :id")
    suspend fun addMinutesToStudyTask(id: Long, addedMinutes: Int)

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteStudyTask(id: Long)

    // Study Focus Sessions
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun observeStudySessions(): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity): Long

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteStudySession(id: Long)

    // Demo Data Cleanup
    @Query("DELETE FROM mood_entries WHERE isDemo = 1")
    suspend fun deleteDemoMoods()

    @Query("DELETE FROM journal_entries WHERE isDemo = 1")
    suspend fun deleteDemoJournals()

    @Query("DELETE FROM mindfulness_sessions WHERE isDemo = 1")
    suspend fun deleteDemoMindfulness()

    @Query("DELETE FROM activity_logs WHERE isDemo = 1")
    suspend fun deleteDemoActivities()

    @Query("DELETE FROM habit_completions WHERE isDemo = 1")
    suspend fun deleteDemoHabitCompletions()

    @Query("DELETE FROM habits WHERE isDemo = 1")
    suspend fun deleteDemoHabits()

    @Query("DELETE FROM sleep_logs WHERE isDemo = 1")
    suspend fun deleteDemoSleep()

    @Query("DELETE FROM hydration_entries WHERE isDemo = 1")
    suspend fun deleteDemoHydration()

    @Query("DELETE FROM study_tasks WHERE isDemo = 1")
    suspend fun deleteDemoStudyTasks()

    @Query("DELETE FROM study_sessions WHERE isDemo = 1")
    suspend fun deleteDemoStudySessions()

    @Transaction
    suspend fun removeAllDemoEntries() {
        deleteDemoMoods()
        deleteDemoJournals()
        deleteDemoMindfulness()
        deleteDemoActivities()
        deleteDemoHabitCompletions()
        deleteDemoHabits()
        deleteDemoSleep()
        deleteDemoHydration()
        deleteDemoStudyTasks()
        deleteDemoStudySessions()
    }

    // Complete Data Wipe
    @Query("DELETE FROM mood_entries")
    suspend fun clearMoods()

    @Query("DELETE FROM journal_entries")
    suspend fun clearJournals()

    @Query("DELETE FROM mindfulness_sessions")
    suspend fun clearMindfulness()

    @Query("DELETE FROM activity_logs")
    suspend fun clearActivities()

    @Query("DELETE FROM habit_completions")
    suspend fun clearHabitCompletions()

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    @Query("DELETE FROM sleep_logs")
    suspend fun clearSleep()

    @Query("DELETE FROM hydration_entries")
    suspend fun clearHydration()

    @Query("DELETE FROM daily_plan_states")
    suspend fun clearDailyPlanStates()

    @Query("DELETE FROM study_tasks")
    suspend fun clearStudyTasks()

    @Query("DELETE FROM study_sessions")
    suspend fun clearStudySessions()

    @Transaction
    suspend fun wipeAllTables() {
        clearMoods()
        clearJournals()
        clearMindfulness()
        clearActivities()
        clearHabitCompletions()
        clearHabits()
        clearSleep()
        clearHydration()
        clearDailyPlanStates()
        clearStudyTasks()
        clearStudySessions()
    }
}

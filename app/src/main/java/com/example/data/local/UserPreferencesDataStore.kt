package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ExperienceLevel
import com.example.data.model.UserPreferences
import com.example.data.model.WellnessGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wellnessDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mind_body_preferences"
)

interface PreferencesDataSource {
    val preferencesFlow: Flow<UserPreferences>
    suspend fun completeOnboarding(
        displayName: String,
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel,
        remindersEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    )
    suspend fun skipOnboarding()
    suspend fun updateGoalsAndTime(
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel
    )
    suspend fun updateHydrationGoal(glasses: Int)
    suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int)
    suspend fun updateThemeMode(mode: String)
    suspend fun updateReducedMotion(enabled: Boolean)
    suspend fun toggleRestDay(dateKey: String, isRestDay: Boolean)
    suspend fun setHasDemoData(hasDemo: Boolean)
    suspend fun updateStudyGoalMinutes(minutes: Int)
    suspend fun clearAllPreferences()
}

class UserPreferencesDataStore(private val context: Context) : PreferencesDataSource {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val SELECTED_GOALS = stringSetPreferencesKey("selected_goals")
        val DAILY_MINUTES = intPreferencesKey("daily_minutes")
        val EXPERIENCE_LEVEL = stringPreferencesKey("experience_level")
        val HYDRATION_GOAL = intPreferencesKey("hydration_goal_glasses")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val REST_DAY_DATE = stringPreferencesKey("rest_day_date")
        val HAS_DEMO_DATA = booleanPreferencesKey("has_demo_data")
        val STUDY_GOAL_MINUTES = intPreferencesKey("study_goal_minutes")
    }

    override val preferencesFlow: Flow<UserPreferences> = context.wellnessDataStore.data.map { prefs ->
        val storedGoals = prefs[Keys.SELECTED_GOALS]
            ?.mapNotNull { raw -> WellnessGoal.entries.find { it.name == raw } }
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }
            ?: setOf(WellnessGoal.MINDFULNESS, WellnessGoal.DAILY_MOVEMENT, WellnessGoal.CONSISTENCY)

        val level = prefs[Keys.EXPERIENCE_LEVEL]
            ?.let { raw -> ExperienceLevel.entries.find { it.name == raw } }
            ?: ExperienceLevel.GENTLE_RETURN

        UserPreferences(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            displayName = prefs[Keys.DISPLAY_NAME] ?: "",
            selectedGoals = storedGoals,
            dailyMinutesAvailable = (prefs[Keys.DAILY_MINUTES] ?: 10).coerceIn(5, 60),
            experienceLevel = level,
            hydrationGoalGlasses = (prefs[Keys.HYDRATION_GOAL] ?: 6).coerceIn(2, 16),
            remindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: false,
            reminderHour = (prefs[Keys.REMINDER_HOUR] ?: 9).coerceIn(0, 23),
            reminderMinute = (prefs[Keys.REMINDER_MINUTE] ?: 0).coerceIn(0, 59),
            themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            isRestDayToday = prefs[Keys.REST_DAY_DATE] ?: "",
            hasDemoData = prefs[Keys.HAS_DEMO_DATA] ?: false,
            dailyStudyGoalMinutes = (prefs[Keys.STUDY_GOAL_MINUTES] ?: 90).coerceIn(15, 600)
        )
    }

    override suspend fun completeOnboarding(
        displayName: String,
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel,
        remindersEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        val safeGoals = goals.ifEmpty { setOf(WellnessGoal.CONSISTENCY) }
        context.wellnessDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
            prefs[Keys.DISPLAY_NAME] = displayName.trim().take(40)
            prefs[Keys.SELECTED_GOALS] = safeGoals.map { it.name }.toSet()
            prefs[Keys.DAILY_MINUTES] = dailyMinutes.coerceIn(5, 60)
            prefs[Keys.EXPERIENCE_LEVEL] = experienceLevel.name
            prefs[Keys.REMINDERS_ENABLED] = remindersEnabled
            prefs[Keys.REMINDER_HOUR] = reminderHour.coerceIn(0, 23)
            prefs[Keys.REMINDER_MINUTE] = reminderMinute.coerceIn(0, 59)
        }
    }

    override suspend fun skipOnboarding() {
        context.wellnessDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    override suspend fun updateGoalsAndTime(
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel
    ) {
        val safeGoals = goals.ifEmpty { setOf(WellnessGoal.CONSISTENCY) }
        context.wellnessDataStore.edit { prefs ->
            prefs[Keys.SELECTED_GOALS] = safeGoals.map { it.name }.toSet()
            prefs[Keys.DAILY_MINUTES] = dailyMinutes.coerceIn(5, 60)
            prefs[Keys.EXPERIENCE_LEVEL] = experienceLevel.name
        }
    }

    override suspend fun updateHydrationGoal(glasses: Int) {
        context.wellnessDataStore.edit { prefs ->
            prefs[Keys.HYDRATION_GOAL] = glasses.coerceIn(2, 16)
        }
    }

    override suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        context.wellnessDataStore.edit { prefs ->
            prefs[Keys.REMINDERS_ENABLED] = enabled
            prefs[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
            prefs[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    override suspend fun updateThemeMode(mode: String) {
        context.wellnessDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    override suspend fun updateReducedMotion(enabled: Boolean) {
        context.wellnessDataStore.edit { prefs ->
            prefs[Keys.REDUCED_MOTION] = enabled
        }
    }

    override suspend fun toggleRestDay(dateKey: String, isRestDay: Boolean) {
        context.wellnessDataStore.edit { prefs ->
            prefs[Keys.REST_DAY_DATE] = if (isRestDay) dateKey else ""
        }
    }

    override suspend fun setHasDemoData(hasDemo: Boolean) {
        context.wellnessDataStore.edit { prefs ->
            prefs[Keys.HAS_DEMO_DATA] = hasDemo
        }
    }

    override suspend fun updateStudyGoalMinutes(minutes: Int) {
        context.wellnessDataStore.edit { prefs ->
            prefs[Keys.STUDY_GOAL_MINUTES] = minutes.coerceIn(15, 600)
        }
    }

    override suspend fun clearAllPreferences() {
        context.wellnessDataStore.edit { it.clear() }
    }
}

package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PreferencesDataSource
import com.example.data.local.WellnessDatabase
import com.example.data.model.ExperienceLevel
import com.example.data.model.UserPreferences
import com.example.data.model.WellnessGoal
import com.example.data.repository.WellnessRepository
import com.example.ui.MindBodyApp
import com.example.ui.theme.MindBodyTheme
import com.example.ui.viewmodel.WellnessViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class FakePreferencesDataSource : PreferencesDataSource {
    private val state = MutableStateFlow(UserPreferences())
    override val preferencesFlow: Flow<UserPreferences> = state

    override suspend fun completeOnboarding(
        displayName: String,
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel,
        remindersEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        state.value = state.value.copy(
            onboardingCompleted = true,
            displayName = displayName,
            selectedGoals = goals,
            dailyMinutesAvailable = dailyMinutes,
            experienceLevel = experienceLevel,
            remindersEnabled = remindersEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )
    }

    override suspend fun skipOnboarding() {
        state.value = state.value.copy(onboardingCompleted = true)
    }

    override suspend fun updateGoalsAndTime(
        goals: Set<WellnessGoal>,
        dailyMinutes: Int,
        experienceLevel: ExperienceLevel
    ) {
        state.value = state.value.copy(
            selectedGoals = goals,
            dailyMinutesAvailable = dailyMinutes,
            experienceLevel = experienceLevel
        )
    }

    override suspend fun updateHydrationGoal(glasses: Int) {
        state.value = state.value.copy(hydrationGoalGlasses = glasses)
    }

    override suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        state.value = state.value.copy(
            remindersEnabled = enabled,
            reminderHour = hour,
            reminderMinute = minute
        )
    }

    override suspend fun updateThemeMode(mode: String) {
        state.value = state.value.copy(themeMode = mode)
    }

    override suspend fun updateReducedMotion(enabled: Boolean) {
        state.value = state.value.copy(reducedMotion = enabled)
    }

    override suspend fun toggleRestDay(dateKey: String, isRestDay: Boolean) {
        state.value = state.value.copy(isRestDayToday = if (isRestDay) dateKey else "")
    }

    override suspend fun setHasDemoData(hasDemo: Boolean) {
        state.value = state.value.copy(hasDemoData = hasDemo)
    }

    override suspend fun updateStudyGoalMinutes(minutes: Int) {
        state.value = state.value.copy(dailyStudyGoalMinutes = minutes)
    }

    override suspend fun clearAllPreferences() {
        state.value = UserPreferences()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: WellnessDatabase
    private lateinit var repository: WellnessRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WellnessDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WellnessRepository(database.wellnessDao(), FakePreferencesDataSource())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app_name from context matches Mind and Body`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mind & Body", appName)
    }

    @Test
    fun `repository validates inputs, persists logs, exports json, and deletes all data`() = runBlocking {
        // Validation checks
        assertTrue(repository.logMood(0, null, emptyList(), "").isFailure)
        assertTrue(repository.addJournalEntry("Prompt", "   ").isFailure)
        assertTrue(repository.logActivity("Walking", "Walk", 0).isFailure)

        // Valid persistence checks
        assertTrue(repository.logMood(4, 2, listOf("Calm"), "Quiet morning").isSuccess)
        assertTrue(repository.addJournalEntry("What went well?", "Took a mindful walk").isSuccess)
        assertTrue(repository.logActivity("Walking", "Morning Walk", 15).isSuccess)
        val habitId = repository.createHabit("Evening stretch", "Body", "Ease tension").getOrThrow()
        repository.toggleHabitCompletion(habitId, true, "2026-10-26")

        assertEquals(1, repository.moodEntries.first().size)
        assertEquals(1, repository.journalEntries.first().size)
        assertEquals(1, repository.activityLogs.first().size)
        assertEquals(1, repository.habitCompletions.first().size)

        // Study time management & phone-locked session logging
        assertTrue(repository.createStudyTask("Math", "", 25, "Morning").isFailure)
        val studyTaskId = repository.createStudyTask("Coding & CS", "Algorithms Practice", 25, "Afternoon").getOrThrow()
        assertTrue(
            repository.logStudySession(
                linkedTaskId = studyTaskId,
                subject = "Coding & CS",
                taskTitle = "Algorithms Practice",
                durationMinutes = 25,
                phoneLocked = true,
                distractionCount = 0
            ).isSuccess
        )
        val savedTask = repository.studyTasks.first().first { it.id == studyTaskId }
        assertTrue(savedTask.isCompleted)
        assertEquals(25, savedTask.completedMinutes)

        // Demo data can be loaded and cleanly removed without losing user data
        repository.loadDemoData()
        assertTrue(repository.moodEntries.first().size > 1)
        repository.removeDemoData()
        assertEquals(1, repository.moodEntries.first().size)

        // Export JSON contains user records and study sessions
        val exported = repository.exportAllDataAsJson()
        assertTrue(exported.contains("Morning Walk"))
        assertTrue(exported.contains("Quiet morning"))
        assertTrue(exported.contains("Algorithms Practice"))

        // Complete wipe removes all stored records
        repository.deleteAllUserData()
        assertTrue(repository.moodEntries.first().isEmpty())
        assertTrue(repository.journalEntries.first().isEmpty())
        assertTrue(repository.activityLogs.first().isEmpty())
        assertTrue(repository.studyTasks.first().isEmpty())
        assertTrue(repository.studySessions.first().isEmpty())
        assertFalse(repository.userPreferences.first().onboardingCompleted)
    }

    @Test
    fun `onboarding skip launches usable home dashboard and navigates core tabs`() {
        val viewModel = WellnessViewModel(repository)
        composeTestRule.setContent {
            MindBodyTheme {
                MindBodyApp(viewModel = viewModel)
            }
        }

        // Wait for initial Room Flow emissions to exit loading state
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasTestTag("onboarding_skip_button"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Skip onboarding
        composeTestRule.onNodeWithTag("onboarding_skip_button").assertIsDisplayed().performClick()

        // Wait for Home dashboard after starter habit seeding
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(androidx.compose.ui.test.hasTestTag("home_screen_list"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag("home_screen_list").assertIsDisplayed()

        // Navigate to Mind tab and save a mood check-in
        composeTestRule.onNodeWithTag("nav_mind").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("save_mood_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Navigate to Study tab, engage Phone Lock, and intentionally unlock
        composeTestRule.onNodeWithTag("nav_study").performClick()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("study_focus_list")
            .performScrollToNode(hasTestTag("start_study_lock_button"))
        composeTestRule.onNodeWithTag("start_study_lock_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("study_lock_overlay"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag("study_lock_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("study_lock_unlock_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("study_lock_confirm_unlock_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag("nav_progress"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Navigate to Progress tab
        composeTestRule.onNodeWithTag("nav_progress").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("progress_screen_list").assertIsDisplayed()
    }
}

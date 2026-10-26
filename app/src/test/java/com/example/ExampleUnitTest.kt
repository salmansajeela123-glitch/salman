package com.example

import com.example.data.model.DailyPlanStateEntity
import com.example.data.model.DateKeyUtils
import com.example.data.model.ExperienceLevel
import com.example.data.model.MoodLevel
import com.example.data.model.PlanItemCategory
import com.example.data.model.SafetySupportHelper
import com.example.data.model.UserPreferences
import com.example.data.model.WellnessGoal
import com.example.ui.viewmodel.DailyPlanGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `daily plan adapts to selected goals and rest day`() {
        val prefs = UserPreferences(
            onboardingCompleted = true,
            selectedGoals = setOf(WellnessGoal.STRESS_RELIEF, WellnessGoal.STRENGTH),
            dailyMinutesAvailable = 15,
            experienceLevel = ExperienceLevel.BEGINNER
        )
        val today = "2026-10-26"

        val plan = DailyPlanGenerator.buildPlan(
            prefs = prefs,
            todayKey = today,
            planStates = emptyList(),
            hasLoggedMoodToday = false,
            hasMindfulnessToday = false,
            hasMovementToday = false,
            todayHydrationGlasses = 0
        )

        assertEquals(3, plan.size)
        assertEquals(PlanItemCategory.MIND, plan[0].category)
        assertEquals(PlanItemCategory.MOVE, plan[1].category)
        assertTrue(plan[1].durationMinutes <= 15)

        // Swapping an item rotates the suggested template
        val swappedPlan = DailyPlanGenerator.buildPlan(
            prefs = prefs,
            todayKey = today,
            planStates = listOf(
                DailyPlanStateEntity(
                    dateKey = today,
                    itemKey = "slot_mind",
                    status = "PENDING",
                    swappedIndex = 1
                )
            ),
            hasLoggedMoodToday = false,
            hasMindfulnessToday = false,
            hasMovementToday = false,
            todayHydrationGlasses = 0
        )
        assertNotEquals(plan[0].title, swappedPlan[0].title)

        // Rest day produces a single low-pressure rest suggestion
        val restPlan = DailyPlanGenerator.buildPlan(
            prefs = prefs.copy(isRestDayToday = today),
            todayKey = today,
            planStates = emptyList(),
            hasLoggedMoodToday = false,
            hasMindfulnessToday = false,
            hasMovementToday = false,
            todayHydrationGlasses = 0
        )
        assertEquals(1, restPlan.size)
        assertEquals(PlanItemCategory.REST, restPlan.first().category)
    }

    @Test
    fun `safety helper detects crisis signals without flagging normal notes`() {
        assertTrue(SafetySupportHelper.containsCrisisSignal("I feel like I want to end my life"))
        assertTrue(SafetySupportHelper.containsCrisisSignal("Thoughts of self-harm tonight"))
        assertFalse(SafetySupportHelper.containsCrisisSignal("Feeling tired after a busy workday, going to rest early."))
        assertFalse(SafetySupportHelper.containsCrisisSignal(""))
    }

    @Test
    fun `mood level and date helpers return expected values`() {
        assertEquals(MoodLevel.BRIGHT, MoodLevel.fromScore(5))
        assertEquals(MoodLevel.STEADY, MoodLevel.fromScore(99))
        val keys = DateKeyUtils.recentDateKeys(7)
        assertEquals(7, keys.size)
        assertEquals(7, keys.distinct().size)
    }
}

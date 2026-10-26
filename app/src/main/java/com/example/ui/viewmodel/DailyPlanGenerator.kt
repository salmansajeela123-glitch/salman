package com.example.ui.viewmodel

import com.example.data.model.DailyPlanStateEntity
import com.example.data.model.ExperienceLevel
import com.example.data.model.PlanItemCategory
import com.example.data.model.UserPreferences
import com.example.data.model.WellnessGoal

data class DailyPlanItem(
    val key: String,
    val title: String,
    val durationMinutes: Int,
    val category: PlanItemCategory,
    val whySuggested: String,
    val status: String = "PENDING", // PENDING, COMPLETED, SKIPPED
    val swapIndex: Int = 0,
    val actionTarget: String = "" // "MOOD", "BREATHE", "MOVE", "HYDRATE", "JOURNAL"
)

object DailyPlanGenerator {

    private data class CandidateTemplate(
        val title: String,
        val minutes: Int,
        val why: String,
        val target: String
    )

    private val mindTemplates = listOf(
        CandidateTemplate(
            title = "2-minute Longer Exhale breathing pause",
            minutes = 2,
            why = "Suggested for Stress Management & Mindfulness — a slower exhale helps you pause comfortably.",
            target = "BREATHE"
        ),
        CandidateTemplate(
            title = "Quick mood & energy check-in",
            minutes = 1,
            why = "Naming how you feel builds gentle self-awareness without judgment.",
            target = "MOOD"
        ),
        CandidateTemplate(
            title = "Write 2 sentences on a gentle prompt",
            minutes = 3,
            why = "Jotting down one thought clears mental clutter privately on your device.",
            target = "JOURNAL"
        ),
        CandidateTemplate(
            title = "5-4-3-2-1 Senses grounding moment",
            minutes = 3,
            why = "Suggested to anchor your focus in your immediate surroundings.",
            target = "BREATHE"
        )
    )

    private val moveTemplates = listOf(
        CandidateTemplate(
            title = "5-minute Desk & Shoulder Unwind stretch",
            minutes = 5,
            why = "Suggested for Mobility & Comfort — gentle movement eases everyday stiffness.",
            target = "MOVE"
        ),
        CandidateTemplate(
            title = "Easy 10-minute conversational walk",
            minutes = 10,
            why = "Suggested for Daily Movement — walking at an unhurried pace supports steady energy.",
            target = "MOVE"
        ),
        CandidateTemplate(
            title = "6-minute Morning Joint Mobility",
            minutes = 6,
            why = "Suggested for starting gently with low-impact joint movement.",
            target = "MOVE"
        ),
        CandidateTemplate(
            title = "Foundational Chair Squats & Wall Push-Ups",
            minutes = 8,
            why = "Suggested for Gentle Strength — equipment-free movement with low-impact options.",
            target = "MOVE"
        )
    )

    private val rhythmTemplates = listOf(
        CandidateTemplate(
            title = "Pause for a glass of water",
            minutes = 1,
            why = "Small steady sips support comfortable energy throughout the day.",
            target = "HYDRATE"
        ),
        CandidateTemplate(
            title = "Log last night's approximate rest routine",
            minutes = 1,
            why = "Suggested for Sleep Habits — noticing wind-down times supports evening consistency.",
            target = "MOVE"
        ),
        CandidateTemplate(
            title = "Unclench jaw & drop shoulders for 3 breaths",
            minutes = 1,
            why = "Fits into even the busiest day to release physical tension.",
            target = "BREATHE"
        )
    )

    fun buildPlan(
        prefs: UserPreferences,
        todayKey: String,
        planStates: List<DailyPlanStateEntity>,
        hasLoggedMoodToday: Boolean,
        hasMindfulnessToday: Boolean,
        hasMovementToday: Boolean,
        todayHydrationGlasses: Int
    ): List<DailyPlanItem> {
        val stateMap = planStates.associateBy { it.itemKey }

        if (prefs.isRestDayToday == todayKey) {
            val restState = stateMap["rest_day_breath"]
            return listOf(
                DailyPlanItem(
                    key = "rest_day_breath",
                    title = "Rest Day: Optional 1-minute quiet breath or sip of water",
                    durationMinutes = 1,
                    category = PlanItemCategory.REST,
                    whySuggested = "You marked today as a Rest Day. Rest is an active part of sustainable health.",
                    status = restState?.status ?: if (hasMindfulnessToday) "COMPLETED" else "PENDING",
                    actionTarget = "BREATHE"
                )
            )
        }

        // 1. Mind slot
        val mindStartIdx = when {
            prefs.selectedGoals.contains(WellnessGoal.STRESS_RELIEF) -> 0
            prefs.selectedGoals.contains(WellnessGoal.MINDFULNESS) -> 3
            else -> 1
        }
        val mindState = stateMap["slot_mind"]
        val mindSwap = mindState?.swappedIndex ?: 0
        val chosenMind = mindTemplates[(mindStartIdx + mindSwap) % mindTemplates.size]
        val mindAutoDone = (chosenMind.target == "MOOD" && hasLoggedMoodToday) ||
            (chosenMind.target == "BREATHE" && hasMindfulnessToday)

        // 2. Move slot (respecting available time and experience level)
        val moveStartIdx = when {
            prefs.dailyMinutesAvailable <= 7 || prefs.experienceLevel == ExperienceLevel.GENTLE_RETURN -> 0
            prefs.selectedGoals.contains(WellnessGoal.STRENGTH) -> 3
            prefs.selectedGoals.contains(WellnessGoal.FLEXIBILITY) -> 2
            else -> 1
        }
        val moveState = stateMap["slot_move"]
        val moveSwap = moveState?.swappedIndex ?: 0
        val chosenMove = moveTemplates[(moveStartIdx + moveSwap) % moveTemplates.size]
        val adjustedMoveMinutes = chosenMove.minutes.coerceAtMost(prefs.dailyMinutesAvailable.coerceAtLeast(3))

        // 3. Rhythm/Care slot
        val rhythmStartIdx = if (prefs.selectedGoals.contains(WellnessGoal.SLEEP_HABITS)) 1 else 0
        val rhythmState = stateMap["slot_rhythm"]
        val rhythmSwap = rhythmState?.swappedIndex ?: 0
        val chosenRhythm = rhythmTemplates[(rhythmStartIdx + rhythmSwap) % rhythmTemplates.size]
        val rhythmAutoDone = chosenRhythm.target == "HYDRATE" && todayHydrationGlasses >= 1

        return listOf(
            DailyPlanItem(
                key = "slot_mind",
                title = chosenMind.title,
                durationMinutes = chosenMind.minutes,
                category = PlanItemCategory.MIND,
                whySuggested = chosenMind.why,
                status = resolveStatus(mindState?.status, mindAutoDone),
                swapIndex = mindSwap,
                actionTarget = chosenMind.target
            ),
            DailyPlanItem(
                key = "slot_move",
                title = chosenMove.title,
                durationMinutes = adjustedMoveMinutes,
                category = PlanItemCategory.MOVE,
                whySuggested = chosenMove.why,
                status = resolveStatus(moveState?.status, hasMovementToday),
                swapIndex = moveSwap,
                actionTarget = chosenMove.target
            ),
            DailyPlanItem(
                key = "slot_rhythm",
                title = chosenRhythm.title,
                durationMinutes = chosenRhythm.minutes,
                category = PlanItemCategory.HYDRATION,
                whySuggested = chosenRhythm.why,
                status = resolveStatus(rhythmState?.status, rhythmAutoDone),
                swapIndex = rhythmSwap,
                actionTarget = chosenRhythm.target
            )
        )
    }

    private fun resolveStatus(explicitStatus: String?, autoDone: Boolean): String {
        return when (explicitStatus) {
            "COMPLETED" -> "COMPLETED"
            "SKIPPED" -> "SKIPPED"
            else -> if (autoDone) "COMPLETED" else "PENDING"
        }
    }
}

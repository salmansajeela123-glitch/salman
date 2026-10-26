package com.example.data.model

data class BreathingPattern(
    val id: String,
    val title: String,
    val subtitle: String,
    val inhaleSec: Int,
    val holdInSec: Int,
    val exhaleSec: Int,
    val holdOutSec: Int
) {
    val cycleSeconds: Int get() = inhaleSec + holdInSec + exhaleSec + holdOutSec
}

data class MindfulnessPractice(
    val id: String,
    val title: String,
    val durationMinutes: Int,
    val summary: String,
    val steps: List<String>
)

data class MovementRoutine(
    val id: String,
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val levelLabel: String,
    val warmUp: String,
    val steps: List<RoutineStep>,
    val coolDown: String
)

data class RoutineStep(
    val name: String,
    val durationSeconds: Int,
    val instruction: String,
    val easierAlternative: String
)

object WellnessCatalog {

    val moodContextTags = listOf(
        "Rested", "Tired", "Calm", "Busy", "Focused",
        "Stiff", "Grateful", "Social", "Quiet", "Stretched"
    )

    val journalPrompts = listOf(
        "What is on my mind right now?",
        "What went well today, even if it was small?",
        "What is one gentle thing my body or mind needs today?",
        "Where did I notice a moment of calm or relief recently?",
        "What can I set down or simplify for the rest of today?"
    )

    val breathingPatterns = listOf(
        BreathingPattern(
            id = "calm_exhale",
            title = "Longer Exhale (4-2-6)",
            subtitle = "Gentle, unhurried exhale to help settle everyday tension",
            inhaleSec = 4,
            holdInSec = 2,
            exhaleSec = 6,
            holdOutSec = 0
        ),
        BreathingPattern(
            id = "balanced_breath",
            title = "Balanced Rhythm (5-5)",
            subtitle = "Smooth, steady breathing with no breath holds",
            inhaleSec = 5,
            holdInSec = 0,
            exhaleSec = 5,
            holdOutSec = 0
        ),
        BreathingPattern(
            id = "box_pause",
            title = "Four-Part Pause (4-4-4-4)",
            subtitle = "Even, structured cadence for grounding your attention",
            inhaleSec = 4,
            holdInSec = 4,
            exhaleSec = 4,
            holdOutSec = 4
        )
    )

    val mindfulnessPractices = listOf(
        MindfulnessPractice(
            id = "senses_54321",
            title = "5-4-3-2-1 Senses Grounding",
            durationMinutes = 3,
            summary = "Anchor your attention in the room around you using everyday sensory details.",
            steps = listOf(
                "Pause comfortably — sitting, standing, or lying down — and let your shoulders drop.",
                "Notice 5 things you can see right now (light on a wall, a texture, a color).",
                "Notice 4 things you can physically feel (feet on the floor, fabric, cool air).",
                "Notice 3 sounds near or far without needing to judge them.",
                "Notice 2 scents or simply the feeling of air moving through your nose, and 1 kind appreciation for pausing."
            )
        ),
        MindfulnessPractice(
            id = "unclench_reset",
            title = "Two-Minute Unclench & Reset",
            durationMinutes = 2,
            summary = "Release unnoticed tension in your jaw, shoulders, and hands.",
            steps = listOf(
                "Gently unstick your tongue from the roof of your mouth and soften your jaw.",
                "Inhale lightly as you lift your shoulders slightly, then let them melt down on the exhale.",
                "Uncurl your fingers and rest your hands openly on your lap or desk.",
                "Take three slow, natural breaths at whatever pace feels comfortable."
            )
        ),
        MindfulnessPractice(
            id = "gentle_body_scan",
            title = "Gentle Head-to-Toe Check-In",
            durationMinutes = 4,
            summary = "Notice how your body feels today with curiosity rather than criticism.",
            steps = listOf(
                "Rest your gaze softly or close your eyes if comfortable.",
                "Bring kind awareness to your forehead, eyes, and neck — no need to force anything to change.",
                "Notice the rise and fall of your chest and upper back as you breathe.",
                "Bring attention down through your hips, knees, ankles, and feet resting against the ground.",
                "Thank yourself for taking four quiet minutes to check in."
            )
        )
    )

    val movementRoutines = listOf(
        MovementRoutine(
            id = "morning_mobility",
            title = "Gentle Morning Joint Mobility",
            category = "Mobility",
            durationMinutes = 6,
            levelLabel = "Beginner • Low Impact",
            warmUp = "Stand or sit tall and take 3 slow breaths while rolling your shoulders gently backward.",
            steps = listOf(
                RoutineStep(
                    name = "Neck & Shoulder Rolls",
                    durationSeconds = 60,
                    instruction = "Slowly tilt ear toward shoulder on each side, then roll shoulders smoothly.",
                    easierAlternative = "Keep movements very small while seated with back supported."
                ),
                RoutineStep(
                    name = "Cat-Cow Spinal Wave",
                    durationSeconds = 90,
                    instruction = "Hands on knees (standing or seated), gently arch and round your upper back with your breath.",
                    easierAlternative = "Perform seated upright in a sturdy chair with hands on thighs."
                ),
                RoutineStep(
                    name = "Hip Circles & Ankle Rolls",
                    durationSeconds = 90,
                    instruction = "Make easy circles with your hips, then rotate each ankle 5 times per direction.",
                    easierAlternative = "Hold onto a wall or chair back for steady balance."
                ),
                RoutineStep(
                    name = "Overhead Reach & Side Bend",
                    durationSeconds = 60,
                    instruction = "Reach arms comfortably upward and lean gently left and right.",
                    easierAlternative = "Keep hands on your ribs or shoulders instead of overhead."
                )
            ),
            coolDown = "Shake out your arms lightly, sip water if thirsty, and pause for 2 calm breaths."
        ),
        MovementRoutine(
            id = "desk_stretch_unwind",
            title = "Desk & Upper-Back Unwind",
            category = "Stretching",
            durationMinutes = 5,
            levelLabel = "All Levels • Seated or Standing",
            warmUp = "Step back from screens or rest your hands in your lap; roll wrists gently for 20 seconds.",
            steps = listOf(
                RoutineStep(
                    name = "Chest Opener Clasp",
                    durationSeconds = 60,
                    instruction = "Draw elbows gently back to open the front of your shoulders while breathing evenly.",
                    easierAlternative = "Rest hands on your lower hips and gently lift your breastbone."
                ),
                RoutineStep(
                    name = "Seated Torso Twist",
                    durationSeconds = 75,
                    instruction = "Sit tall and rotate gently to the right for 3 breaths, then to the left.",
                    easierAlternative = "Turn only your shoulders slightly within a completely comfortable range."
                ),
                RoutineStep(
                    name = "Hamstring & Calf Lengthener",
                    durationSeconds = 75,
                    instruction = "Extend one heel forward on the floor and hinge slightly from the hips.",
                    easierAlternative = "Keep a generous bend in your knee so the stretch stays mild."
                )
            ),
            coolDown = "Relax your arms by your sides and notice the warmth in your shoulders."
        ),
        MovementRoutine(
            id = "mindful_walk",
            title = "Easy Mindful Walk",
            category = "Walking",
            durationMinutes = 10,
            levelLabel = "Beginner • Indoor or Outdoor",
            warmUp = "First 2 minutes: stroll at an unhurried, conversational warm-up pace.",
            steps = listOf(
                RoutineStep(
                    name = "Comfortable Steady Pace",
                    durationSeconds = 240,
                    instruction = "Walk at a natural pace where you can still speak in full sentences comfortably.",
                    easierAlternative = "Walk indoors down a hallway or march gently in place near support."
                ),
                RoutineStep(
                    name = "Posture & Breath Check",
                    durationSeconds = 180,
                    instruction = "Let your arms swing naturally and notice the rhythm of your footsteps.",
                    easierAlternative = "Pause on a bench anytime you want a breather."
                )
            ),
            coolDown = "Final 2 minutes: slow your steps gradually and stretch your calves lightly."
        ),
        MovementRoutine(
            id = "gentle_strength",
            title = "Foundational Bodyweight Strength",
            category = "Bodyweight",
            durationMinutes = 10,
            levelLabel = "Beginner • No Equipment",
            warmUp = "1 minute of easy marching in place and arm circles to warm your joints.",
            steps = listOf(
                RoutineStep(
                    name = "Sit-to-Stand Squats",
                    durationSeconds = 75,
                    instruction = "Lower hips controlled toward a chair and stand tall, pressing evenly through your feet.",
                    easierAlternative = "Sit all the way onto a sturdy chair between each repetition and use armrests if helpful."
                ),
                RoutineStep(
                    name = "Wall or Counter Push-Ups",
                    durationSeconds = 75,
                    instruction = "Place hands shoulder-width on a wall, bend elbows smoothly, and press back.",
                    easierAlternative = "Stand closer to the wall for lighter resistance."
                ),
                RoutineStep(
                    name = "Rest & Breath Reset",
                    durationSeconds = 45,
                    instruction = "Rest completely, shake out your arms, and take slow breaths.",
                    easierAlternative = "Extend rest as long as you need before continuing."
                ),
                RoutineStep(
                    name = "Standing Bird-Dog Balance",
                    durationSeconds = 75,
                    instruction = "Holding a wall lightly, extend one leg slightly behind you while keeping your torso steady.",
                    easierAlternative = "Keep toes lightly touching the floor behind you for stability."
                )
            ),
            coolDown = "Finish with 1 minute of slow quad and shoulder stretches and gentle breathing."
        )
    )
}

object SafetySupportHelper {
    private val crisisPhrases = listOf(
        "suicide",
        "suicidal",
        "kill myself",
        "end my life",
        "hurt myself",
        "self harm",
        "self-harm",
        "want to die",
        "better off dead",
        "harm someone",
        "kill someone",
        "imminent danger"
    )

    fun containsCrisisSignal(text: String): Boolean {
        if (text.isBlank()) return false
        val normalized = text.lowercase()
        return crisisPhrases.any { phrase -> normalized.contains(phrase) }
    }
}

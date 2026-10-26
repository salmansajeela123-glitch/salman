package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class WellnessGoal(val title: String, val subtitle: String) {
    STRESS_RELIEF("Stress Management", "Short pauses to unclench and reset"),
    MINDFULNESS("Mindfulness", "Present-moment grounding and reflection"),
    SLEEP_HABITS("Better Sleep Habits", "Steady wind-down and wake routines"),
    DAILY_MOVEMENT("Daily Movement", "Gentle walks and everyday activity"),
    STRENGTH("Gentle Strength", "Accessible bodyweight foundations"),
    FLEXIBILITY("Mobility & Stretch", "Ease stiffness in joints and shoulders"),
    ENERGY("Steady Energy", "Hydration, daylight, and posture breaks"),
    CONSISTENCY("General Consistency", "Small achievable habits without pressure")
}

enum class ExperienceLevel(val label: String, val description: String) {
    GENTLE_RETURN("Starting Gently", "Short, low-impact steps at an easy pace"),
    BEGINNER("Beginner", "Approachable everyday movement & mindfulness"),
    COMFORTABLE("Comfortable", "Steady routines with moderate variety")
}

enum class MoodLevel(val score: Int, val label: String, val supportiveHint: String) {
    HEAVY(1, "Heavy", "Take things extra gently today."),
    LOW(2, "Low", "A quiet pause or glass of water is plenty."),
    STEADY(3, "Steady", "Grounded and taking the day as it comes."),
    GOOD(4, "Good", "Feeling balanced and supported."),
    BRIGHT(5, "Bright", "Feeling energized and clear.");

    companion object {
        fun fromScore(score: Int): MoodLevel =
            entries.find { it.score == score } ?: STEADY
    }
}

enum class PlanItemCategory(val label: String) {
    MIND("Mind"),
    MOVE("Move"),
    HABIT("Habit"),
    HYDRATION("Hydration"),
    REST("Rest")
}

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val displayName: String = "",
    val selectedGoals: Set<WellnessGoal> = setOf(
        WellnessGoal.MINDFULNESS,
        WellnessGoal.DAILY_MOVEMENT,
        WellnessGoal.CONSISTENCY
    ),
    val dailyMinutesAvailable: Int = 10,
    val experienceLevel: ExperienceLevel = ExperienceLevel.GENTLE_RETURN,
    val hydrationGoalGlasses: Int = 6,
    val remindersEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val themeMode: String = "SYSTEM",
    val reducedMotion: Boolean = false,
    val isRestDayToday: String = "",
    val hasDemoData: Boolean = false,
    val dailyStudyGoalMinutes: Int = 90
)

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moodScore: Int, // 1..5
    val stressLevel: Int?, // Optional 1..5 (1 = Calm, 5 = High tension)
    val tagsCsv: String = "",
    val note: String = "",
    val dateKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
) {
    val tags: List<String>
        get() = tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String = "",
    val content: String,
    val dateKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(tableName = "mindfulness_sessions")
data class MindfulnessSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionType: String, // "Breathing" | "Grounding" | "Mindfulness"
    val title: String,
    val durationSeconds: Int,
    val dateKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityType: String, // Walking, Stretching, Mobility, Bodyweight, Yoga, Cycling, Other
    val title: String,
    val durationMinutes: Int,
    val intensity: String = "Gentle",
    val notes: String = "",
    val dateKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Mind & Body",
    val whyNote: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(
    tableName = "habit_completions",
    indices = [Index(value = ["habitId", "dateKey"], unique = true)]
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(tableName = "sleep_logs")
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bedTimeText: String,
    val wakeTimeText: String,
    val approxHours: Float,
    val qualityRating: Int, // 1..5 self-rating
    val notes: String = "",
    val dateKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(tableName = "hydration_entries")
data class HydrationEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val glassesCount: Int,
    val dateKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(
    tableName = "daily_plan_states",
    indices = [Index(value = ["dateKey", "itemKey"], unique = true)]
)
data class DailyPlanStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val itemKey: String,
    val status: String, // "COMPLETED", "SKIPPED", "SWAPPED"
    val swappedIndex: Int = 0
)

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val title: String,
    val plannedMinutes: Int,
    val timeSlot: String = "Morning", // Morning, Afternoon, Evening
    val isCompleted: Boolean = false,
    val completedMinutes: Int = 0,
    val dateKey: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val taskTitle: String,
    val durationMinutes: Int,
    val phoneLocked: Boolean,
    val distractionCount: Int = 0,
    val completedFullBlock: Boolean = true,
    val dateKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

object DateKeyUtils {
    fun todayKey(nowMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(nowMillis))
    }

    fun readableToday(nowMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.US)
        return sdf.format(Date(nowMillis))
    }

    fun shortDayLabel(dateKey: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsed = parser.parse(dateKey) ?: return dateKey.takeLast(5)
            SimpleDateFormat("EEE", Locale.US).format(parsed)
        } catch (_: Exception) {
            dateKey.takeLast(5)
        }
    }

    fun readableDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d • h:mm a", Locale.US)
        return sdf.format(Date(millis))
    }

    fun recentDateKeys(daysCount: Int, endMillis: Long = System.currentTimeMillis()): List<String> {
        val cal = Calendar.getInstance().apply { timeInMillis = endMillis }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val keys = mutableListOf<String>()
        cal.add(Calendar.DAY_OF_YEAR, -(daysCount - 1))
        repeat(daysCount) {
            keys.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return keys
    }

    fun greetingForHour(hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): String {
        return when (hourOfDay) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Quiet night"
        }
    }
}

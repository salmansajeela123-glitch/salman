package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Database(
    entities = [
        MoodEntryEntity::class,
        JournalEntryEntity::class,
        MindfulnessSessionEntity::class,
        ActivityLogEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        SleepLogEntity::class,
        HydrationEntryEntity::class,
        DailyPlanStateEntity::class,
        StudyTaskEntity::class,
        StudySessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WellnessDatabase : RoomDatabase() {
    abstract fun wellnessDao(): WellnessDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `study_tasks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `plannedMinutes` INTEGER NOT NULL,
                        `timeSlot` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `completedMinutes` INTEGER NOT NULL,
                        `dateKey` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `isDemo` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `study_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `taskTitle` TEXT NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `phoneLocked` INTEGER NOT NULL,
                        `distractionCount` INTEGER NOT NULL,
                        `completedFullBlock` INTEGER NOT NULL,
                        `dateKey` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `isDemo` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: WellnessDatabase? = null

        fun getInstance(context: Context): WellnessDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WellnessDatabase::class.java,
                    "mind_body_wellness.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

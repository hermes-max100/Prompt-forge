package com.aistudio.promptforge.abcd.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedPrompt::class, PlaygroundRun::class, EvalCase::class], version = 1, exportSchema = false)
abstract class PromptDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var Instance: PromptDatabase? = null

        fun getDatabase(context: Context): PromptDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PromptDatabase::class.java, "prompt_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

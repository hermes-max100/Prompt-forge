package com.aistudio.promptforge.abcd.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "saved_prompts")
data class SavedPrompt(
    @PrimaryKey val id: String,
    val title: String,
    val frameworkId: String,
    val fieldsJson: String,
    val assembled: String,
    val system: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "playground_runs")
data class PlaygroundRun(
    @PrimaryKey val id: String,
    val input: String,
    val output: String,
    val latencyMs: Long = 0,
    val promptTokens: Int = 0,
    val outputTokens: Int = 0,
    val totalTokens: Int = 0,
    val at: Long
)

@Entity(tableName = "eval_cases")
data class EvalCase(
    @PrimaryKey val id: String,
    val input: String,
    val expected: String
)

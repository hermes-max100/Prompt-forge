package com.aistudio.promptforge.abcd.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "autoforge_packs")
@Serializable
data class AutoForgePack(
    @PrimaryKey val id: String,
    val goalTitle: String,
    val goalInput: String,
    val taskType: String,
    val promptText: String,
    val skillsJson: String,
    val mcpConfigJson: String,
    val fullSpecMarkdown: String,
    val executionLatencyMs: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_skills")
@Serializable
data class SavedSkill(
    @PrimaryKey val id: String,
    val title: String,
    val slug: String,
    val category: String,
    val description: String,
    val trigger: String,
    val source: String,
    val implementationCode: String,
    val skillMarkdown: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_mcps")
@Serializable
data class SavedMcp(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val description: String,
    val toolsCount: Int,
    val mcpJsonConfig: String,
    val serverCode: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_prompts")
@Serializable
data class SavedPrompt(
    @PrimaryKey val id: String,
    val title: String,
    val frameworkId: String,
    val fieldsJson: String,
    val assembled: String,
    val system: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playground_runs")
@Serializable
data class PlaygroundRun(
    @PrimaryKey val id: String,
    val forgeType: String = "AutoForge",
    val input: String,
    val output: String,
    val latencyMs: Long = 0,
    val promptTokens: Int = 0,
    val outputTokens: Int = 0,
    val totalTokens: Int = 0,
    val at: Long = System.currentTimeMillis()
)

@Entity(tableName = "eval_cases")
@Serializable
data class EvalCase(
    @PrimaryKey val id: String,
    val input: String,
    val expected: String
)

@Entity(tableName = "favorite_prompts")
@Serializable
data class FavoritePrompt(
    @PrimaryKey val promptId: String,
    val favoritedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prompt_stats")
@Serializable
data class PromptStat(
    @PrimaryKey val promptId: String,
    val executionCount: Int = 0,
    val copyCount: Int = 0,
    val shareCount: Int = 0,
    val lastLatencyMs: Long = 0,
    val lastUsedAt: Long = System.currentTimeMillis()
)


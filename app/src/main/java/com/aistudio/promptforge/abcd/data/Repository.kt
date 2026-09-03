package com.aistudio.promptforge.abcd.data

import com.aistudio.promptforge.abcd.BuildConfig
import com.aistudio.promptforge.abcd.api.Content
import com.aistudio.promptforge.abcd.api.GeminiErrorResponse
import com.aistudio.promptforge.abcd.api.GenerateContentRequest
import com.aistudio.promptforge.abcd.api.GenerationConfig
import com.aistudio.promptforge.abcd.api.Part
import com.aistudio.promptforge.abcd.api.RetrofitClient
import com.aistudio.promptforge.abcd.model.AutoForgePackData
import com.aistudio.promptforge.abcd.model.GeneratedMcp
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.util.UUID
import kotlin.math.ceil

@kotlinx.serialization.Serializable
data class GenerationMetrics(
    val latencyMs: Long = 0,
    val promptTokens: Int = 0,
    val outputTokens: Int = 0,
    val totalTokens: Int = 0,
    val isEstimated: Boolean = false,
    val promptChars: Int = 0,
    val promptWords: Int = 0,
    val outputChars: Int = 0,
    val outputWords: Int = 0
) {
    val tokensPerSecond: Float
        get() = if (latencyMs > 0 && outputTokens > 0) {
            (outputTokens.toFloat() / (latencyMs.toFloat() / 1000f))
        } else 0f
}

sealed class AiResult<out T> {
    data class Success<out T>(val data: T, val metrics: GenerationMetrics) : AiResult<T>()
    data class Error(val message: String, val code: Int? = null) : AiResult<Nothing>()
}

class PromptRepository(private val dao: PromptDao) {

    // AutoForge Packs
    fun getAutoForgePacks(): Flow<List<AutoForgePack>> = dao.getAllAutoForgePacks()
    suspend fun insertAutoForgePack(pack: AutoForgePack) = dao.insertAutoForgePack(pack)
    suspend fun deleteAutoForgePack(id: String) = dao.deleteAutoForgePack(id)

    // Saved Skills
    fun getSavedSkills(): Flow<List<SavedSkill>> = dao.getAllSavedSkills()
    suspend fun insertSavedSkill(skill: SavedSkill) = dao.insertSavedSkill(skill)
    suspend fun deleteSavedSkill(id: String) = dao.deleteSavedSkill(id)

    // Saved MCPs
    fun getSavedMcps(): Flow<List<SavedMcp>> = dao.getAllSavedMcps()
    suspend fun insertSavedMcp(mcp: SavedMcp) = dao.insertSavedMcp(mcp)
    suspend fun deleteSavedMcp(id: String) = dao.deleteSavedMcp(id)

    // Saved Prompts
    fun getSavedPrompts(): Flow<List<SavedPrompt>> = dao.getAllSavedPrompts()
    suspend fun insertSavedPrompt(prompt: SavedPrompt) = dao.insertSavedPrompt(prompt)
    suspend fun deleteSavedPrompt(id: String) = dao.deleteSavedPrompt(id)

    // Playground Runs
    fun getPlaygroundRuns(): Flow<List<PlaygroundRun>> = dao.getAllPlaygroundRuns()
    suspend fun insertPlaygroundRun(run: PlaygroundRun) = dao.insertPlaygroundRun(run)
    suspend fun clearPlaygroundRuns() = dao.clearPlaygroundRuns()

    companion object {
        fun estimateTokenCount(text: String): Int {
            if (text.isBlank()) return 0
            val byChars = ceil(text.length / 4.0).toInt()
            val words = text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            val byWords = ceil(words / 0.75).toInt()
            return maxOf(byChars, byWords, 1)
        }
    }

    suspend fun generateComplete(
        system: String?,
        user: String,
        temperature: Float = 0.4f,
        maxTokens: Int = 1500
    ): AiResult<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val startTime = System.currentTimeMillis()

        val promptCombined = if (!system.isNullOrBlank()) "$system\n$user" else user
        val promptWords = promptCombined.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val promptChars = promptCombined.length

        if (apiKey.isEmpty()) {
            // Local high-speed synthesis fallback
            val fallbackText = AutoForgeEngine.generateLocalPrompt10OutOf10(user)
            val latency = System.currentTimeMillis() - startTime
            val outTokens = estimateTokenCount(fallbackText)
            val inTokens = estimateTokenCount(promptCombined)
            return@withContext AiResult.Success(
                data = fallbackText,
                metrics = GenerationMetrics(
                    latencyMs = latency,
                    promptTokens = inTokens,
                    outputTokens = outTokens,
                    totalTokens = inTokens + outTokens,
                    isEstimated = true,
                    promptChars = promptChars,
                    promptWords = promptWords,
                    outputChars = fallbackText.length,
                    outputWords = fallbackText.trim().split("\\s+".toRegex()).size
                )
            )
        }

        val sysContent = if (!system.isNullOrBlank()) {
            Content(parts = listOf(Part(text = system)))
        } else null

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = user)))),
            generationConfig = GenerationConfig(temperature = temperature, maxOutputTokens = maxTokens),
            systemInstruction = sysContent
        )

        try {
            val response = try {
                RetrofitClient.service.generateContent(apiKey, request)
            } catch (_: Exception) {
                RetrofitClient.service.generateContentFallback(apiKey, request)
            }
            val latencyMs = System.currentTimeMillis() - startTime

            val candidate = response.candidates?.firstOrNull()
            if (candidate?.finishReason == "SAFETY" || response.promptFeedback?.blockReason != null) {
                val reason = response.promptFeedback?.blockReason ?: candidate?.finishReason ?: "SAFETY"
                return@withContext AiResult.Error("Generation blocked by safety filters ($reason).")
            }

            val text = candidate?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                val outputWords = text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                val outputChars = text.length

                val promptTokens = response.usageMetadata?.promptTokenCount ?: estimateTokenCount(promptCombined)
                val outputTokens = response.usageMetadata?.candidatesTokenCount ?: estimateTokenCount(text)
                val totalTokens = response.usageMetadata?.totalTokenCount ?: (promptTokens + outputTokens)
                val isEstimated = response.usageMetadata == null

                val metrics = GenerationMetrics(
                    latencyMs = latencyMs,
                    promptTokens = promptTokens,
                    outputTokens = outputTokens,
                    totalTokens = totalTokens,
                    isEstimated = isEstimated,
                    promptChars = promptChars,
                    promptWords = promptWords,
                    outputChars = outputChars,
                    outputWords = outputWords
                )
                AiResult.Success(text, metrics)
            } else {
                AiResult.Error("Empty response received from Gemini.")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val parsedMessage = try {
                if (!errorBody.isNullOrBlank()) {
                    val parsed = RetrofitClient.json.decodeFromString<GeminiErrorResponse>(errorBody)
                    parsed.error?.message
                } else null
            } catch (_: Exception) {
                null
            }
            val message = parsedMessage ?: "HTTP ${e.code()}: ${e.message()}"
            AiResult.Error(message, e.code())
        } catch (e: Exception) {
            // Local fallback on network error
            val fallbackText = AutoForgeEngine.generateLocalPrompt10OutOf10(user)
            val latency = System.currentTimeMillis() - startTime
            val outTokens = estimateTokenCount(fallbackText)
            val inTokens = estimateTokenCount(promptCombined)
            AiResult.Success(
                data = fallbackText,
                metrics = GenerationMetrics(
                    latencyMs = latency,
                    promptTokens = inTokens,
                    outputTokens = outTokens,
                    totalTokens = inTokens + outTokens,
                    isEstimated = true,
                    promptChars = promptChars,
                    promptWords = promptWords,
                    outputChars = fallbackText.length,
                    outputWords = fallbackText.trim().split("\\s+".toRegex()).size
                )
            )
        }
    }

    suspend fun synthesize10OutOf10Prompt(goal: String): AiResult<String> {
        val systemPrompt = """
You are AutoForge's Master Prompt Synthesizer.
Your mission is to take ANY broad, generic, or brief goal and engineer a flawless, comprehensive "10 out of 10" production prompt.
The generated prompt MUST include:
1. # SYSTEM PERSONA & EXPERTISE (Elite domain authority & operational standards)
2. # MISSION OBJECTIVE & SCOPE (Unambiguous boundary conditions)
3. # MULTI-STEP CHAIN-OF-THOUGHT PROTOCOL (Step 1 to Step 5 logical execution order)
4. # GUARDRAILS & ANTI-HALLUCINATION RULES (Zero unverified assumptions, rate limit handling)
5. # STRICT OUTPUT CONTRACT (Typed JSON schemas or structured Markdown sections)

Return ONLY the assembled 10/10 Prompt without conversational preamble.
""".trimIndent()

        return generateComplete(
            system = systemPrompt,
            user = "Goal/Task Intent: $goal",
            temperature = 0.3f,
            maxTokens = 1400
        )
    }

    suspend fun synthesizeSkillsForGoal(goal: String, prompt: String): List<GeneratedSkill> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return AutoForgeEngine.generateLocalSkills(goal)
        }

        // Try AI generation for custom skills, fallback to local engine if needed
        val system = """
You are AutoForge's Skill Engineer.
Analyze the user's goal and generate the Python code and SKILL.md for a custom autonomous skill.
Return format:
SKILL_NAME: <name>
CATEGORY: <category>
TRIGGER: <trigger_keyword>
DESCRIPTION: <brief description>
---CODE---
<executable python class code>
---MARKDOWN---
<SKILL.md documentation>
""".trimIndent()

        val aiResult = generateComplete(
            system = system,
            user = "Goal: $goal\nPrompt Context: $prompt",
            temperature = 0.3f,
            maxTokens = 1200
        )

        return if (aiResult is AiResult.Success && aiResult.data.contains("---CODE---")) {
            val text = aiResult.data
            try {
                val name = text.substringAfter("SKILL_NAME:").substringBefore("\n").trim().ifBlank { "Custom Agent Skill" }
                val category = text.substringAfter("CATEGORY:").substringBefore("\n").trim().ifBlank { "Custom Skill" }
                val trigger = text.substringAfter("TRIGGER:").substringBefore("\n").trim().ifBlank { "custom_skill" }
                val desc = text.substringAfter("DESCRIPTION:").substringBefore("---CODE---").trim()
                val code = text.substringAfter("---CODE---").substringBefore("---MARKDOWN---").trim()
                val md = text.substringAfter("---MARKDOWN---").trim()

                val generated = GeneratedSkill(
                    name = name,
                    slug = name.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-'),
                    category = category,
                    description = desc.ifBlank { "Auto-coded skill for $goal" },
                    source = "AutoForge Autonomous Code Synthesis",
                    triggers = listOf(trigger, "execute_$trigger"),
                    language = "python",
                    code = code,
                    skillMarkdown = md
                )
                listOf(generated) + AutoForgeEngine.generateLocalSkills(goal).take(2)
            } catch (_: Exception) {
                AutoForgeEngine.generateLocalSkills(goal)
            }
        } else {
            AutoForgeEngine.generateLocalSkills(goal)
        }
    }

    suspend fun synthesizeMcpsForGoal(goal: String, skills: List<GeneratedSkill>): List<GeneratedMcp> {
        return AutoForgeEngine.generateLocalMcps(goal, skills)
    }
}

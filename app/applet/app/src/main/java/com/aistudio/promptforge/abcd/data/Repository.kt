package com.aistudio.promptforge.abcd.data

import com.aistudio.promptforge.abcd.BuildConfig
import com.aistudio.promptforge.abcd.api.Content
import com.aistudio.promptforge.abcd.api.GeminiErrorResponse
import com.aistudio.promptforge.abcd.api.GenerateContentRequest
import com.aistudio.promptforge.abcd.api.GenerationConfig
import com.aistudio.promptforge.abcd.api.Part
import com.aistudio.promptforge.abcd.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
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
    fun getSavedPrompts(): Flow<List<SavedPrompt>> = dao.getAllSavedPrompts()
    suspend fun insertSavedPrompt(prompt: SavedPrompt) = dao.insertSavedPrompt(prompt)
    suspend fun deleteSavedPrompt(id: String) = dao.deleteSavedPrompt(id)

    fun getPlaygroundRuns(): Flow<List<PlaygroundRun>> = dao.getAllPlaygroundRuns()
    suspend fun insertPlaygroundRun(run: PlaygroundRun) = dao.insertPlaygroundRun(run)
    suspend fun clearPlaygroundRuns() = dao.clearPlaygroundRuns()

    fun getEvalCases(): Flow<List<EvalCase>> = dao.getAllEvalCases()
    suspend fun insertEvalCase(case: EvalCase) = dao.insertEvalCase(case)
    suspend fun deleteEvalCase(id: String) = dao.deleteEvalCase(id)

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
        maxTokens: Int = 1000
    ): AiResult<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) {
            return@withContext AiResult.Error("API Key is missing. Please configure GEMINI_API_KEY in the environment or secrets panel.")
        }

        val promptCombined = if (!system.isNullOrBlank()) "$system\n$user" else user
        val promptWords = promptCombined.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val promptChars = promptCombined.length

        val sysContent = if (!system.isNullOrBlank()) {
            Content(parts = listOf(Part(text = system)))
        } else null

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = user)))),
            generationConfig = GenerationConfig(temperature = temperature, maxOutputTokens = maxTokens),
            systemInstruction = sysContent
        )

        val startTime = System.currentTimeMillis()
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
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
            AiResult.Error(e.message ?: "Network error occurred")
        }
    }
}

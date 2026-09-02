package com.aistudio.promptforge.abcd.data

import com.aistudio.promptforge.abcd.BuildConfig
import com.aistudio.promptforge.abcd.api.Content
import com.aistudio.promptforge.abcd.api.GenerateContentRequest
import com.aistudio.promptforge.abcd.api.GenerationConfig
import com.aistudio.promptforge.abcd.api.Part
import com.aistudio.promptforge.abcd.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

sealed class AiResult<out T> {
    data class Success<out T>(val data: T) : AiResult<T>()
    data class Error(val message: String) : AiResult<Nothing>()
}

class PromptRepository(private val dao: PromptDao) {

    fun getSavedPrompts(): Flow<List<SavedPrompt>> = dao.getAllSavedPrompts()

    suspend fun insertSavedPrompt(prompt: SavedPrompt) {
        dao.insertSavedPrompt(prompt)
    }

    suspend fun deleteSavedPrompt(id: String) {
        dao.deleteSavedPrompt(id)
    }

    fun getPlaygroundRuns(): Flow<List<PlaygroundRun>> = dao.getAllPlaygroundRuns()

    suspend fun insertPlaygroundRun(run: PlaygroundRun) {
        dao.insertPlaygroundRun(run)
    }

    suspend fun clearPlaygroundRuns() {
        dao.clearPlaygroundRuns()
    }

    fun getEvalCases(): Flow<List<EvalCase>> = dao.getAllEvalCases()

    suspend fun insertEvalCase(case: EvalCase) {
        dao.insertEvalCase(case)
    }

    suspend fun deleteEvalCase(id: String) {
        dao.deleteEvalCase(id)
    }

    suspend fun generateComplete(
        system: String?,
        user: String,
        temperature: Float = 0.4f,
        maxTokens: Int = 700
    ): AiResult<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) return@withContext AiResult.Error("API Key is missing.")
        
        val sysContent = if (!system.isNullOrBlank()) {
            Content(parts = listOf(Part(text = system)))
        } else null
        
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = user)))),
            generationConfig = GenerationConfig(temperature = temperature, maxOutputTokens = maxTokens),
            systemInstruction = sysContent
        )
        
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                AiResult.Success(text)
            } else {
                AiResult.Error("Empty response from AI")
            }
        } catch (e: Exception) {
            AiResult.Error(e.message ?: "Unknown error")
        }
    }
}

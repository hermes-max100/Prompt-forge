package com.aistudio.promptforge.abcd.api

import com.aistudio.promptforge.abcd.BuildConfig
import com.aistudio.promptforge.abcd.model.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class ApiCallResult<out T> {
    data class Success<out T>(val data: T, val latencyMs: Long) : ApiCallResult<T>()
    data class Failure(val error: AppError) : ApiCallResult<Nothing>()
}

data class ApiHealthStatus(
    val isHealthy: Boolean,
    val model: String,
    val latencyMs: Long = 0,
    val message: String,
    val error: AppError? = null,
    val testedAt: Long = System.currentTimeMillis()
)

class PromptForgeApiService(
    private val service: GeminiApiService = RetrofitClient.service
) {
    @Volatile
    var customApiKey: String = ""

    fun isApiKeyConfigured(): Boolean {
        return BuildConfig.GEMINI_API_KEY.isNotBlank() || customApiKey.isNotBlank()
    }

    fun getActiveApiKey(): String {
        return customApiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
    }

    fun setCustomKey(key: String) {
        customApiKey = key.trim()
    }

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        model: String = SupportedModels.FLASH_LATEST,
        temperature: Float = 0.4f,
        maxTokens: Int = 1500
    ): ApiCallResult<GenerateContentResponse> = withContext(Dispatchers.IO) {
        val apiKey = getActiveApiKey()
        if (apiKey.isBlank()) {
            return@withContext ApiCallResult.Failure(AppError.apiKeyMissing())
        }

        val startTime = System.currentTimeMillis()
        val sysContent = if (!systemInstruction.isNullOrBlank()) {
            Content(parts = listOf(Part(text = systemInstruction)))
        } else null

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = temperature,
                maxOutputTokens = maxTokens
            ),
            systemInstruction = sysContent
        )

        try {
            val response = try {
                service.generateContentWithModel(model, apiKey, request)
            } catch (_: Exception) {
                // Fallback to flash-latest direct route
                service.generateContent(apiKey, request)
            }
            val latency = System.currentTimeMillis() - startTime

            // Verify content safety or empty response
            val candidate = response.candidates?.firstOrNull()
            if (candidate?.finishReason == "SAFETY" || response.promptFeedback?.blockReason != null) {
                val reason = response.promptFeedback?.blockReason ?: candidate?.finishReason ?: "SAFETY"
                return@withContext ApiCallResult.Failure(AppError.safetyBlocked(reason))
            }

            if (candidate?.content?.parts?.firstOrNull()?.text == null) {
                return@withContext ApiCallResult.Failure(
                    AppError.generic("Empty content returned by Gemini model $model")
                )
            }

            ApiCallResult.Success(response, latency)
        } catch (e: Throwable) {
            val appError = classifyError(e)
            ApiCallResult.Failure(appError)
        }
    }

    suspend fun testConnection(model: String = SupportedModels.FLASH_LATEST): ApiHealthStatus = withContext(Dispatchers.IO) {
        val apiKey = getActiveApiKey()
        if (apiKey.isBlank()) {
            return@withContext ApiHealthStatus(
                isHealthy = false,
                model = model,
                latencyMs = 0,
                message = "API Key not configured. Running in Local Offline Mode.",
                error = AppError.apiKeyMissing()
            )
        }

        val startTime = System.currentTimeMillis()
        val pingRequest = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "ping")))),
            generationConfig = GenerationConfig(maxOutputTokens = 10, temperature = 0.0f)
        )

        try {
            val response = service.generateContentWithModel(model, apiKey, pingRequest)
            val latency = System.currentTimeMillis() - startTime
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                ApiHealthStatus(
                    isHealthy = true,
                    model = model,
                    latencyMs = latency,
                    message = "Connected to $model (${latency}ms latency)"
                )
            } else {
                ApiHealthStatus(
                    isHealthy = false,
                    model = model,
                    latencyMs = latency,
                    message = "API reachable but received empty candidate",
                    error = AppError.generic("Empty ping response")
                )
            }
        } catch (e: Throwable) {
            val latency = System.currentTimeMillis() - startTime
            val err = classifyError(e)
            ApiHealthStatus(
                isHealthy = false,
                model = model,
                latencyMs = latency,
                message = err.message,
                error = err
            )
        }
    }

    fun classifyError(throwable: Throwable): AppError {
        return when (throwable) {
            is HttpException -> parseHttpError(throwable)
            is SocketTimeoutException -> AppError.timeout(throwable.localizedMessage)
            is UnknownHostException -> AppError.networkError("Unknown host: ${throwable.message}")
            is ConnectException -> AppError.networkError("Connection failed: ${throwable.message}")
            is IOException -> AppError.networkError("I/O network exception: ${throwable.message}")
            else -> AppError.generic(
                message = throwable.localizedMessage ?: "Unexpected error occurred",
                details = throwable.stackTraceToString().take(300)
            )
        }
    }

    private fun parseHttpError(e: HttpException): AppError {
        val code = e.code()
        val rawBody = e.response()?.errorBody()?.string()

        val parsedErrorMessage = try {
            if (!rawBody.isNullOrBlank()) {
                val parsed = RetrofitClient.json.decodeFromString<GeminiErrorResponse>(rawBody)
                parsed.error?.message
            } else null
        } catch (_: Exception) {
            null
        }

        val details = parsedErrorMessage ?: rawBody ?: e.message()

        return when (code) {
            400 -> AppError(
                type = com.aistudio.promptforge.abcd.model.AiErrorType.UNKNOWN,
                title = "Bad Request (HTTP 400)",
                message = parsedErrorMessage ?: "Gemini API rejected the request format or parameters.",
                technicalDetails = details,
                httpCode = 400,
                isRetryable = false,
                suggestedAction = "Check model parameters or reduce input length."
            )
            401, 403 -> AppError.apiKeyInvalid(code, details)
            404 -> AppError(
                type = com.aistudio.promptforge.abcd.model.AiErrorType.UNKNOWN,
                title = "Model Not Found (HTTP 404)",
                message = "The requested Gemini model was not found.",
                technicalDetails = details,
                httpCode = 404,
                isRetryable = false,
                suggestedAction = "Switch to gemini-flash-latest or gemini-3.5-flash."
            )
            429 -> AppError.rateLimited(details)
            500, 502, 503, 504 -> AppError.serverError(code, details)
            else -> AppError(
                type = com.aistudio.promptforge.abcd.model.AiErrorType.UNKNOWN,
                title = "API Error (HTTP $code)",
                message = parsedErrorMessage ?: "Gemini service returned HTTP $code.",
                technicalDetails = details,
                httpCode = code,
                isRetryable = true,
                suggestedAction = "Try again in a few moments."
            )
        }
    }
}

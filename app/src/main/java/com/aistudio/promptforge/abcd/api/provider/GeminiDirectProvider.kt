package com.aistudio.promptforge.abcd.api.provider

import com.aistudio.promptforge.abcd.api.ApiCallResult
import com.aistudio.promptforge.abcd.api.PromptForgeApiService
import com.aistudio.promptforge.abcd.data.GenerationMetrics
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.model.AiErrorType
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.util.AiOutputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiDirectProvider(
    private val apiService: PromptForgeApiService = PromptForgeApiService()
) : AiProvider {

    override val providerId: String = "gemini_direct"
    override val displayName: String = "Google Gemini (Direct API)"

    override fun isConfigured(): Boolean = apiService.isApiKeyConfigured()

    fun setCustomApiKey(key: String) {
        apiService.setCustomKey(key)
    }

    override suspend fun generate(request: ProviderGenerationRequest): AiExecutionResult<String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val combinedPrompt = if (!request.systemInstruction.isNullOrBlank()) {
            "${request.systemInstruction}\n${request.prompt}"
        } else {
            request.prompt
        }

        if (!isConfigured()) {
            return@withContext AiExecutionResult.UserActionRequired(
                reason = "Gemini API key is not configured.",
                appError = AppError.apiKeyMissing(),
                actionType = ActionRequiredType.CONFIGURE_KEY
            )
        }

        val apiResult = apiService.generateContent(
            prompt = request.prompt,
            systemInstruction = request.systemInstruction,
            model = request.model,
            temperature = request.temperature,
            maxTokens = request.maxTokens
        )

        when (apiResult) {
            is ApiCallResult.Success -> {
                val candidate = apiResult.data.candidates?.firstOrNull()
                val rawText = candidate?.content?.parts?.firstOrNull()?.text ?: ""
                val latency = apiResult.latencyMs

                // Treat AI output as untrusted: sanitize and bound
                val validation = AiOutputValidator.sanitizeAndValidate(rawText)

                val promptTokens = apiResult.data.usageMetadata?.promptTokenCount
                    ?: PromptRepository.estimateTokenCount(combinedPrompt)
                val outputTokens = apiResult.data.usageMetadata?.candidatesTokenCount
                    ?: PromptRepository.estimateTokenCount(validation.sanitizedText)
                val totalTokens = apiResult.data.usageMetadata?.totalTokenCount ?: (promptTokens + outputTokens)

                val metrics = GenerationMetrics(
                    latencyMs = latency,
                    promptTokens = promptTokens,
                    outputTokens = outputTokens,
                    totalTokens = totalTokens,
                    isEstimated = apiResult.data.usageMetadata == null,
                    promptChars = combinedPrompt.length,
                    promptWords = combinedPrompt.split("\\s+".toRegex()).count { it.isNotBlank() },
                    outputChars = validation.sanitizedText.length,
                    outputWords = validation.sanitizedText.split("\\s+".toRegex()).count { it.isNotBlank() }
                )

                AiExecutionResult.Success(
                    data = validation.sanitizedText,
                    metrics = metrics,
                    isFallback = false,
                    rawOutput = rawText
                )
            }
            is ApiCallResult.Failure -> {
                val err = apiResult.error
                when (err.type) {
                    AiErrorType.API_KEY_MISSING, AiErrorType.API_KEY_INVALID -> {
                        AiExecutionResult.UserActionRequired(
                            reason = err.message,
                            appError = err,
                            actionType = ActionRequiredType.CONFIGURE_KEY
                        )
                    }
                    AiErrorType.SAFETY_BLOCKED -> {
                        AiExecutionResult.UserActionRequired(
                            reason = err.message,
                            appError = err,
                            actionType = ActionRequiredType.MODIFY_PROMPT_SAFETY
                        )
                    }
                    AiErrorType.RATE_LIMIT_EXCEEDED -> {
                        AiExecutionResult.RetryableFailure(
                            reason = err.message,
                            appError = err,
                            retryAfterMs = 30_000L
                        )
                    }
                    AiErrorType.NETWORK_UNAVAILABLE, AiErrorType.TIMEOUT, AiErrorType.SERVER_ERROR -> {
                        AiExecutionResult.RetryableFailure(
                            reason = err.message,
                            appError = err,
                            retryAfterMs = 3_000L
                        )
                    }
                    else -> {
                        AiExecutionResult.PermanentFailure(
                            reason = err.message,
                            appError = err
                        )
                    }
                }
            }
        }
    }

    override suspend fun testHealth(model: String): ProviderHealth {
        val health = apiService.testConnection(model)
        return ProviderHealth(
            isHealthy = health.isHealthy,
            providerId = providerId,
            displayName = displayName,
            latencyMs = health.latencyMs,
            message = health.message,
            error = health.error
        )
    }
}

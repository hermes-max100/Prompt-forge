package com.aistudio.promptforge.abcd.api.provider

import com.aistudio.promptforge.abcd.data.GenerationMetrics
import com.aistudio.promptforge.abcd.model.AppError
import kotlinx.serialization.Serializable

@Serializable
enum class ActionRequiredType {
    CONFIGURE_KEY,
    UPGRADE_QUOTA,
    MODIFY_PROMPT_SAFETY,
    CHECK_NETWORK,
    CONFIGURE_PROXY
}

sealed class AiExecutionResult<out T> {
    data class Success<out T>(
        val data: T,
        val metrics: GenerationMetrics,
        val isFallback: Boolean = false,
        val rawOutput: String = "",
        val notice: AppError? = null
    ) : AiExecutionResult<T>()

    data class RetryableFailure(
        val reason: String,
        val appError: AppError,
        val retryAfterMs: Long = 2000L
    ) : AiExecutionResult<Nothing>()

    data class UserActionRequired(
        val reason: String,
        val appError: AppError,
        val actionType: ActionRequiredType = ActionRequiredType.CONFIGURE_KEY
    ) : AiExecutionResult<Nothing>()

    data class PermanentFailure(
        val reason: String,
        val appError: AppError
    ) : AiExecutionResult<Nothing>()
}

data class ProviderGenerationRequest(
    val prompt: String,
    val systemInstruction: String? = null,
    val model: String = "models/gemini-flash-latest",
    val temperature: Float = 0.4f,
    val maxTokens: Int = 1500,
    val jsonSchemaConstraint: String? = null
)

data class ProviderHealth(
    val isHealthy: Boolean,
    val providerId: String,
    val displayName: String,
    val latencyMs: Long = 0,
    val message: String,
    val error: AppError? = null
)

interface AiProvider {
    val providerId: String
    val displayName: String
    suspend fun generate(request: ProviderGenerationRequest): AiExecutionResult<String>
    suspend fun testHealth(model: String): ProviderHealth
    fun isConfigured(): Boolean
}

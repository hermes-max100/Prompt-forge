package com.aistudio.promptforge.abcd.api.provider

import com.aistudio.promptforge.abcd.data.AutoForgeEngine
import com.aistudio.promptforge.abcd.data.GenerationMetrics
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.util.AiOutputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAutonomousProvider : AiProvider {

    override val providerId: String = "local_autonomous"
    override val displayName: String = "Local Autonomous Engine (Offline 10/10)"

    override fun isConfigured(): Boolean = true

    override suspend fun generate(request: ProviderGenerationRequest): AiExecutionResult<String> = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        val generatedPrompt = AutoForgeEngine.generateLocalPrompt10OutOf10(
            goal = request.prompt,
            framework = "Autonomous Production"
        )
        val latency = System.currentTimeMillis() - startTime
        val validation = AiOutputValidator.sanitizeAndValidate(generatedPrompt)

        val promptTokens = PromptRepository.estimateTokenCount(request.prompt)
        val outputTokens = PromptRepository.estimateTokenCount(validation.sanitizedText)

        val metrics = GenerationMetrics(
            latencyMs = latency,
            promptTokens = promptTokens,
            outputTokens = outputTokens,
            totalTokens = promptTokens + outputTokens,
            isEstimated = true,
            promptChars = request.prompt.length,
            promptWords = request.prompt.split("\\s+".toRegex()).count { it.isNotBlank() },
            outputChars = validation.sanitizedText.length,
            outputWords = validation.sanitizedText.split("\\s+".toRegex()).count { it.isNotBlank() }
        )

        AiExecutionResult.Success(
            data = validation.sanitizedText,
            metrics = metrics,
            isFallback = true,
            rawOutput = generatedPrompt
        )
    }

    override suspend fun testHealth(model: String): ProviderHealth {
        return ProviderHealth(
            isHealthy = true,
            providerId = providerId,
            displayName = displayName,
            latencyMs = 5,
            message = "Local Autonomous Engine is 100% operational offline."
        )
    }
}

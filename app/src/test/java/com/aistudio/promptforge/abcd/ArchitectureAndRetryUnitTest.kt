package com.aistudio.promptforge.abcd

import com.aistudio.promptforge.abcd.data.AiResult
import com.aistudio.promptforge.abcd.data.GenerationMetrics
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.model.AiErrorType
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.model.ErrorSeverity
import com.aistudio.promptforge.abcd.model.GoalPreset
import com.aistudio.promptforge.abcd.ui.EngineStage
import com.aistudio.promptforge.abcd.util.RetryConfig
import com.aistudio.promptforge.abcd.util.RetryPolicy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ArchitectureAndRetryUnitTest {

    @Test
    fun testRetryPolicyErrorClassification() {
        // Network, timeout, rate limit and server errors must be retryable
        val networkErr = AppError.networkError("Failed to connect")
        assertTrue(RetryPolicy.isRetryable(networkErr))
        assertEquals(AiErrorType.NETWORK_UNAVAILABLE, networkErr.type)

        val timeoutErr = AppError.timeout("Read timed out")
        assertTrue(RetryPolicy.isRetryable(timeoutErr))
        assertEquals(AiErrorType.TIMEOUT, timeoutErr.type)

        val rateLimitErr = AppError.rateLimited("Quota exceeded 429")
        assertTrue(RetryPolicy.isRetryable(rateLimitErr))
        assertEquals(AiErrorType.RATE_LIMIT_EXCEEDED, rateLimitErr.type)

        val serverErr = AppError.serverError(503, "Service unavailable")
        assertTrue(RetryPolicy.isRetryable(serverErr))

        // Missing API key or invalid API key or safety block should NOT be automatically retried
        val apiKeyMissing = AppError.apiKeyMissing()
        assertFalse(RetryPolicy.isRetryable(apiKeyMissing))

        val apiKeyInvalid = AppError.apiKeyInvalid(403)
        assertFalse(RetryPolicy.isRetryable(apiKeyInvalid))

        val safetyBlocked = AppError.safetyBlocked("HATE_SPEECH")
        assertFalse(RetryPolicy.isRetryable(safetyBlocked))
    }

    @Test
    fun testRetryPolicyExceptionClassification() {
        assertTrue(RetryPolicy.isRetryableException(SocketTimeoutException("Read timed out")))
        assertTrue(RetryPolicy.isRetryableException(UnknownHostException("generativelanguage.googleapis.com")))
        assertTrue(RetryPolicy.isRetryableException(IOException("Connection reset by peer")))
        assertTrue(RetryPolicy.isRetryableException(RuntimeException("HTTP 429 Too Many Requests")))
        assertTrue(RetryPolicy.isRetryableException(RuntimeException("HTTP 503 Service Unavailable")))

        assertFalse(RetryPolicy.isRetryableException(IllegalArgumentException("Invalid prompt syntax")))
        assertFalse(RetryPolicy.isRetryableException(NullPointerException("Missing field")))
    }

    @Test
    fun testRetryConfigBackoffCalculations() {
        val config = RetryConfig(
            maxRetries = 3,
            initialDelayMs = 500L,
            maxDelayMs = 4000L,
            backoffFactor = 2.0,
            jitterRatio = 0.0 // Zero jitter for predictable assertions
        )

        // Attempt 0: 500 * 2^0 = 500
        assertEquals(500L, config.calculateDelayMs(0))
        // Attempt 1: 500 * 2^1 = 1000
        assertEquals(1000L, config.calculateDelayMs(1))
        // Attempt 2: 500 * 2^2 = 2000
        assertEquals(2000L, config.calculateDelayMs(2))
        // Attempt 3: 500 * 2^3 = 4000 (capped at maxDelayMs 4000)
        assertEquals(4000L, config.calculateDelayMs(3))
        // Attempt 4: 500 * 2^4 = 8000 -> clamped to 4000
        assertEquals(4000L, config.calculateDelayMs(4))
    }

    @Test
    fun testExecuteWithRetrySuccessAfterFailure() = runBlocking {
        var attempts = 0
        val result = RetryPolicy.executeWithRetry(
            config = RetryConfig(maxRetries = 3, initialDelayMs = 10L)
        ) { attempt ->
            attempts++
            if (attempt < 2) {
                throw SocketTimeoutException("Simulated timeout on attempt $attempt")
            }
            "Success on attempt $attempt"
        }

        assertEquals("Success on attempt 2", result)
        assertEquals(3, attempts) // attempts: 0, 1, 2
    }

    @Test
    fun testExecuteAiResultWithRetry() = runBlocking {
        var callCount = 0
        val retryResult = RetryPolicy.executeAiResultWithRetry<String>(
            config = RetryConfig(maxRetries = 2, initialDelayMs = 10L)
        ) { attempt ->
            callCount++
            if (attempt == 0) {
                AiResult.Error("Temporary rate limit", 429, AppError.rateLimited())
            } else {
                AiResult.Success("Generated content successfully", GenerationMetrics(latencyMs = 120))
            }
        }

        assertTrue(retryResult is AiResult.Success)
        assertEquals("Generated content successfully", (retryResult as AiResult.Success).data)
        assertEquals(2, callCount)
    }

    @Test
    fun testGoalPresetApplication() {
        val preset = GoalPreset(
            title = "Autonomous Research Agent",
            category = "Research",
            summary = "Scours papers and summarizes",
            genericGoal = "Research trending LLM papers daily, compute synthesis, and output markdown report.",
            iconEmoji = "🔬"
        )
        assertEquals("Autonomous Research Agent", preset.title)
        assertTrue(preset.genericGoal.contains("LLM papers"))
    }

    @Test
    fun testEngineStageMetadata() {
        assertEquals(0, EngineStage.IDLE.stepIndex)
        assertEquals(1, EngineStage.PROMPT_FORGING.stepIndex)
        assertEquals(2, EngineStage.SKILL_FORGING.stepIndex)
        assertEquals(3, EngineStage.PLUGIN_FORGING.stepIndex)
        assertEquals(4, EngineStage.ASSEMBLY.stepIndex)
        assertEquals(5, EngineStage.READY.stepIndex)
        assertEquals("Prompt Forge", EngineStage.PROMPT_FORGING.title)
    }

    @Test
    fun testAppErrorSeverities() {
        val infoError = AppError.apiKeyMissing()
        assertEquals(ErrorSeverity.INFO, infoError.severity)

        val warningError = AppError.networkError()
        assertEquals(ErrorSeverity.WARNING, warningError.severity)

        val criticalError = AppError.serverError(500)
        assertEquals(ErrorSeverity.ERROR, criticalError.severity)
    }
}

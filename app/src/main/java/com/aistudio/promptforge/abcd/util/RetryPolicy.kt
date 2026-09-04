package com.aistudio.promptforge.abcd.util

import com.aistudio.promptforge.abcd.data.AiResult
import com.aistudio.promptforge.abcd.model.AiErrorType
import com.aistudio.promptforge.abcd.model.AppError
import kotlinx.coroutines.delay
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Robust retry policy supporting exponential backoff, jitter, and selective error classification.
 */
data class RetryConfig(
    val maxRetries: Int = 3,
    val initialDelayMs: Long = 600L,
    val maxDelayMs: Long = 8000L,
    val backoffFactor: Double = 2.0,
    val jitterRatio: Double = 0.25
) {
    fun calculateDelayMs(attempt: Int): Long {
        val baseDelay = (initialDelayMs * backoffFactor.pow(attempt.toDouble())).toLong()
        val clampedDelay = min(baseDelay, maxDelayMs)
        val jitter = (clampedDelay * jitterRatio * (Random.nextDouble() * 2 - 1)).toLong()
        return (clampedDelay + jitter).coerceAtLeast(100L)
    }
}

object RetryPolicy {

    /**
     * Determines whether an AppError can be retried.
     */
    fun isRetryable(error: AppError): Boolean {
        return when (error.type) {
            AiErrorType.NETWORK_UNAVAILABLE,
            AiErrorType.RATE_LIMIT_EXCEEDED,
            AiErrorType.SERVER_ERROR,
            AiErrorType.TIMEOUT -> true
            AiErrorType.API_KEY_MISSING,
            AiErrorType.API_KEY_INVALID,
            AiErrorType.SAFETY_BLOCKED,
            AiErrorType.PARSING_ERROR,
            AiErrorType.DATABASE_ERROR,
            AiErrorType.UNKNOWN -> error.isRetryable
        }
    }

    /**
     * Determines whether a Throwable is an inherently transient, retryable failure.
     */
    fun isRetryableException(throwable: Throwable): Boolean {
        return when (throwable) {
            is SocketTimeoutException,
            is UnknownHostException,
            is IOException -> true
            else -> {
                val message = throwable.message?.lowercase() ?: ""
                message.contains("timeout") ||
                    message.contains("connection refused") ||
                    message.contains("reset by peer") ||
                    message.contains("429") ||
                    message.contains("503") ||
                    message.contains("500") ||
                    message.contains("temporary failure")
            }
        }
    }

    /**
     * Executes a suspending operation with automatic retries according to [config].
     * @param onRetry Callback invoked before each retry attempt with attempt number, delay, and error.
     */
    suspend fun <T> executeWithRetry(
        config: RetryConfig = RetryConfig(),
        onRetry: (suspend (attempt: Int, delayMs: Long, error: Throwable) -> Unit)? = null,
        block: suspend (attempt: Int) -> T
    ): T {
        var lastException: Throwable? = null
        for (attempt in 0..config.maxRetries) {
            try {
                return block(attempt)
            } catch (t: Throwable) {
                lastException = t
                if (attempt >= config.maxRetries || !isRetryableException(t)) {
                    throw t
                }
                val delayMs = config.calculateDelayMs(attempt)
                onRetry?.invoke(attempt + 1, delayMs, t)
                delay(delayMs)
            }
        }
        throw lastException ?: IllegalStateException("Retry exhausted without exception")
    }

    /**
     * Executes an AiResult returning block with retries when AiResult.Error is retryable.
     */
    suspend fun <T> executeAiResultWithRetry(
        config: RetryConfig = RetryConfig(),
        onRetry: (suspend (attempt: Int, delayMs: Long, error: AppError) -> Unit)? = null,
        block: suspend (attempt: Int) -> AiResult<T>
    ): AiResult<T> {
        var lastResult: AiResult.Error? = null
        for (attempt in 0..config.maxRetries) {
            val result = block(attempt)
            when (result) {
                is AiResult.Success -> return result
                is AiResult.Error -> {
                    lastResult = result
                    val appError = result.appError ?: AppError.generic(result.message)
                    if (attempt >= config.maxRetries || !isRetryable(appError)) {
                        return result
                    }
                    val delayMs = config.calculateDelayMs(attempt)
                    onRetry?.invoke(attempt + 1, delayMs, appError)
                    delay(delayMs)
                }
            }
        }
        return lastResult ?: AiResult.Error(message = "Retries exhausted", appError = AppError.generic("Retries exhausted"))
    }
}

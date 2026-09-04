package com.aistudio.promptforge.abcd.model

import kotlinx.serialization.Serializable

enum class ErrorSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

enum class AiErrorType {
    NETWORK_UNAVAILABLE,
    API_KEY_MISSING,
    API_KEY_INVALID,
    RATE_LIMIT_EXCEEDED,
    SAFETY_BLOCKED,
    SERVER_ERROR,
    TIMEOUT,
    PARSING_ERROR,
    DATABASE_ERROR,
    UNKNOWN
}

@Serializable
data class AppError(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: AiErrorType,
    val title: String,
    val message: String,
    val technicalDetails: String? = null,
    val httpCode: Int? = null,
    val isRetryable: Boolean = true,
    val suggestedAction: String? = null,
    val severity: ErrorSeverity = ErrorSeverity.ERROR,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun networkError(details: String? = null): AppError {
            return AppError(
                type = AiErrorType.NETWORK_UNAVAILABLE,
                title = "Network Connection Unavailable",
                message = "Unable to reach the Gemini API service. Please verify your internet connection.",
                technicalDetails = details,
                isRetryable = true,
                suggestedAction = "Check your internet connection and tap Retry. Local offline AutoForge engine remains active.",
                severity = ErrorSeverity.WARNING
            )
        }

        fun apiKeyMissing(): AppError {
            return AppError(
                type = AiErrorType.API_KEY_MISSING,
                title = "Gemini API Key Not Configured",
                message = "No GEMINI_API_KEY environment variable was detected.",
                technicalDetails = "System.getenv(\"GEMINI_API_KEY\") is empty or unset",
                isRetryable = false,
                suggestedAction = "Configure your GEMINI_API_KEY in the AI Studio Secrets panel. The app is currently running in local offline synthesis mode.",
                severity = ErrorSeverity.INFO
            )
        }

        fun apiKeyInvalid(httpCode: Int = 403, details: String? = null): AppError {
            return AppError(
                type = AiErrorType.API_KEY_INVALID,
                title = "Invalid or Unauthorized API Key",
                message = "The Gemini API rejected the request due to invalid credentials (HTTP $httpCode).",
                technicalDetails = details ?: "HTTP $httpCode: PermissionDenied / API key not valid",
                httpCode = httpCode,
                isRetryable = false,
                suggestedAction = "Verify that your API key is correct and has the Generative Language API enabled in Google Cloud / AI Studio.",
                severity = ErrorSeverity.ERROR
            )
        }

        fun rateLimited(details: String? = null): AppError {
            return AppError(
                type = AiErrorType.RATE_LIMIT_EXCEEDED,
                title = "API Rate Limit Exceeded",
                message = "Too many requests sent to the Gemini API (HTTP 429 Resource Exhausted).",
                technicalDetails = details ?: "HTTP 429: Quota exceeded or rate limit reached",
                httpCode = 429,
                isRetryable = true,
                suggestedAction = "Please wait 30–60 seconds before retrying, or rely on the local synthesis engine.",
                severity = ErrorSeverity.WARNING
            )
        }

        fun safetyBlocked(reason: String): AppError {
            return AppError(
                type = AiErrorType.SAFETY_BLOCKED,
                title = "Content Blocked by Safety Filters",
                message = "The prompt or response triggered Gemini safety filters ($reason).",
                technicalDetails = "Safety block reason: $reason",
                isRetryable = false,
                suggestedAction = "Refine the prompt wording to adhere to safety guidelines and retry.",
                severity = ErrorSeverity.WARNING
            )
        }

        fun serverError(code: Int, details: String? = null): AppError {
            return AppError(
                type = AiErrorType.SERVER_ERROR,
                title = "Gemini Service Unavailable",
                message = "Upstream Google Generative AI server error (HTTP $code).",
                technicalDetails = details ?: "HTTP $code: Internal server error",
                httpCode = code,
                isRetryable = true,
                suggestedAction = "The service may be temporarily degraded. Tap Retry in a few seconds.",
                severity = ErrorSeverity.ERROR
            )
        }

        fun timeout(details: String? = null): AppError {
            return AppError(
                type = AiErrorType.TIMEOUT,
                title = "Request Timed Out",
                message = "The API call exceeded the 60-second response timeout window.",
                technicalDetails = details ?: "SocketTimeoutException: Read/Write timed out",
                isRetryable = true,
                suggestedAction = "The network connection might be slow. Tap Retry or use smaller token limits.",
                severity = ErrorSeverity.WARNING
            )
        }

        fun generic(message: String, details: String? = null): AppError {
            return AppError(
                type = AiErrorType.UNKNOWN,
                title = "Operation Failed",
                message = message,
                technicalDetails = details,
                isRetryable = true,
                suggestedAction = "Please try again or check the application logs.",
                severity = ErrorSeverity.ERROR
            )
        }
    }
}

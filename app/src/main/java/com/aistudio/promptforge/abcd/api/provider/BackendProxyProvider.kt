package com.aistudio.promptforge.abcd.api.provider

import com.aistudio.promptforge.abcd.data.GenerationMetrics
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.util.AiOutputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BackendProxyProvider(
    private var proxyUrl: String = "",
    private var proxyAuthToken: String = ""
) : AiProvider {

    override val providerId: String = "backend_proxy"
    override val displayName: String = "Server-Side Proxy / Gateway"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun updateProxyConfig(url: String, authToken: String) {
        this.proxyUrl = url.trim()
        this.proxyAuthToken = authToken.trim()
    }

    fun getProxyUrl(): String = proxyUrl
    fun getProxyAuthToken(): String = proxyAuthToken

    override fun isConfigured(): Boolean = proxyUrl.isNotBlank()

    override suspend fun generate(request: ProviderGenerationRequest): AiExecutionResult<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext AiExecutionResult.UserActionRequired(
                reason = "Backend Proxy URL is not configured.",
                appError = AppError.generic(
                    message = "Server-side proxy gateway URL is empty.",
                    details = "Configure your backend proxy endpoint in Settings (e.g. https://your-server.com/api/generate)"
                ),
                actionType = ActionRequiredType.CONFIGURE_PROXY
            )
        }

        val startTime = System.currentTimeMillis()
        val combinedPrompt = if (!request.systemInstruction.isNullOrBlank()) {
            "${request.systemInstruction}\n${request.prompt}"
        } else {
            request.prompt
        }

        try {
            val jsonPayload = JSONObject().apply {
                put("prompt", request.prompt)
                put("systemInstruction", request.systemInstruction ?: "")
                put("model", request.model)
                put("temperature", request.temperature.toDouble())
                put("maxTokens", request.maxTokens)
            }

            val requestBuilder = Request.Builder()
                .url(proxyUrl)
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))

            if (proxyAuthToken.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $proxyAuthToken")
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val latency = System.currentTimeMillis() - startTime
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = "Proxy returned HTTP ${response.code}: $responseBody"
                return@withContext if (response.code == 401 || response.code == 403) {
                    AiExecutionResult.UserActionRequired(
                        reason = "Proxy authorization failed (HTTP ${response.code})",
                        appError = AppError.generic(errorMsg),
                        actionType = ActionRequiredType.CONFIGURE_PROXY
                    )
                } else if (response.code == 429 || response.code >= 500) {
                    AiExecutionResult.RetryableFailure(
                        reason = "Proxy temporarily unavailable (HTTP ${response.code})",
                        appError = AppError.generic(errorMsg),
                        retryAfterMs = 5000L
                    )
                } else {
                    AiExecutionResult.PermanentFailure(
                        reason = errorMsg,
                        appError = AppError.generic(errorMsg)
                    )
                }
            }

            val rawText = try {
                val json = JSONObject(responseBody)
                json.optString("text", json.optString("output", responseBody))
            } catch (_: Exception) {
                responseBody
            }

            val validation = AiOutputValidator.sanitizeAndValidate(rawText)
            val promptTokens = PromptRepository.estimateTokenCount(combinedPrompt)
            val outputTokens = PromptRepository.estimateTokenCount(validation.sanitizedText)

            val metrics = GenerationMetrics(
                latencyMs = latency,
                promptTokens = promptTokens,
                outputTokens = outputTokens,
                totalTokens = promptTokens + outputTokens,
                isEstimated = true,
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
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            AiExecutionResult.RetryableFailure(
                reason = "Network error connecting to proxy: ${e.localizedMessage}",
                appError = AppError.networkError(e.message),
                retryAfterMs = 4000L
            )
        }
    }

    override suspend fun testHealth(model: String): ProviderHealth = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext ProviderHealth(
                isHealthy = false,
                providerId = providerId,
                displayName = displayName,
                message = "Proxy endpoint not configured."
            )
        }
        val startTime = System.currentTimeMillis()
        try {
            val req = Request.Builder()
                .url(proxyUrl)
                .head()
                .build()
            val resp = httpClient.newCall(req).execute()
            val latency = System.currentTimeMillis() - startTime
            ProviderHealth(
                isHealthy = resp.isSuccessful || resp.code == 405, // 405 Method Not Allowed is common for HEAD
                providerId = providerId,
                displayName = displayName,
                latencyMs = latency,
                message = "Connected to Proxy (${latency}ms)"
            )
        } catch (e: Exception) {
            ProviderHealth(
                isHealthy = false,
                providerId = providerId,
                displayName = displayName,
                message = "Proxy connection failed: ${e.localizedMessage}"
            )
        }
    }
}

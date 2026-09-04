package com.aistudio.promptforge.abcd.model

import kotlinx.serialization.Serializable

@Serializable
enum class VariableType {
    STRING,
    NUMBER,
    BOOLEAN,
    CHOICE,
    CODE
}

@Serializable
data class PromptVariable(
    val name: String,
    val type: VariableType = VariableType.STRING,
    val defaultValue: String = "",
    val isRequired: Boolean = true,
    val description: String = "",
    val example: String = "",
    val validationRegex: String? = null,
    val options: List<String> = emptyList()
)

@Serializable
data class PromptModelConfig(
    val model: String = "models/gemini-flash-latest",
    val temperature: Float = 0.4f,
    val maxTokens: Int = 1500,
    val systemInstruction: String? = null,
    val topP: Float = 0.95f
)

@Serializable
enum class DiffType {
    ADDED,
    REMOVED,
    UNCHANGED
}

@Serializable
data class DiffLine(
    val originalLineNumber: Int?,
    val revisedLineNumber: Int?,
    val text: String,
    val type: DiffType
)

@Serializable
data class PromptDiffResult(
    val lines: List<DiffLine>,
    val additionsCount: Int,
    val deletionsCount: Int,
    val unchangedCount: Int
) {
    val isIdentical: Boolean get() = additionsCount == 0 && deletionsCount == 0
}

@Serializable
enum class ProvenanceStatus {
    SUCCESS,
    RETRYABLE_ERROR,
    USER_ACTION_REQUIRED,
    FAILED,
    FALLBACK_LOCAL
}

@Serializable
data class ExecutionProvenanceRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val promptId: String,
    val promptTitle: String,
    val revisionId: String? = null,
    val revisionNumber: Int? = null,
    val resolvedVariables: Map<String, String> = emptyMap(),
    val selectedModel: String,
    val temperature: Float = 0.4f,
    val maxTokens: Int = 1500,
    val timestamp: Long = System.currentTimeMillis(),
    val latencyMs: Long = 0,
    val tokensPrompt: Int = 0,
    val tokensOutput: Int = 0,
    val totalTokens: Int = 0,
    val tokenCostEstimateUsd: Double = 0.0,
    val sanitizedOutput: String,
    val rawOutput: String = "",
    val status: ProvenanceStatus = ProvenanceStatus.SUCCESS,
    val errorReason: String? = null
)

@Serializable
data class VariableValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap(),
    val resolvedMap: Map<String, String> = emptyMap()
)

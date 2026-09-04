package com.aistudio.promptforge.abcd.data.repository

import com.aistudio.promptforge.abcd.data.ExecutionProvenanceEntity
import com.aistudio.promptforge.abcd.data.PromptDao
import com.aistudio.promptforge.abcd.model.ExecutionProvenanceRecord
import com.aistudio.promptforge.abcd.model.ProvenanceStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class ProvenanceRepository(
    private val dao: PromptDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllProvenance(): Flow<List<ExecutionProvenanceRecord>> {
        return dao.getAllExecutionProvenance().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getProvenanceForPrompt(promptId: String): Flow<List<ExecutionProvenanceRecord>> {
        return dao.getProvenanceForPrompt(promptId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun recordRun(
        promptId: String,
        promptTitle: String,
        revisionId: String? = null,
        revisionNumber: Int? = null,
        resolvedVariables: Map<String, String> = emptyMap(),
        selectedModel: String,
        temperature: Float = 0.4f,
        maxTokens: Int = 1500,
        latencyMs: Long,
        tokensPrompt: Int,
        tokensOutput: Int,
        sanitizedOutput: String,
        rawOutput: String = "",
        status: ProvenanceStatus = ProvenanceStatus.SUCCESS,
        errorReason: String? = null
    ): ExecutionProvenanceRecord {
        val totalTokens = tokensPrompt + tokensOutput
        // Gemini Flash is approximately $0.0001 per 1K input tokens, $0.0004 per 1K output tokens
        val estimatedCost = (tokensPrompt * 0.0000001) + (tokensOutput * 0.0000004)

        val record = ExecutionProvenanceRecord(
            id = UUID.randomUUID().toString(),
            promptId = promptId,
            promptTitle = promptTitle,
            revisionId = revisionId,
            revisionNumber = revisionNumber,
            resolvedVariables = resolvedVariables,
            selectedModel = selectedModel,
            temperature = temperature,
            maxTokens = maxTokens,
            timestamp = System.currentTimeMillis(),
            latencyMs = latencyMs,
            tokensPrompt = tokensPrompt,
            tokensOutput = tokensOutput,
            totalTokens = totalTokens,
            tokenCostEstimateUsd = estimatedCost,
            sanitizedOutput = sanitizedOutput,
            rawOutput = rawOutput,
            status = status,
            errorReason = errorReason
        )

        val entity = ExecutionProvenanceEntity(
            id = record.id,
            promptId = record.promptId,
            promptTitle = record.promptTitle,
            revisionId = record.revisionId,
            revisionNumber = record.revisionNumber,
            resolvedVariablesJson = json.encodeToString(record.resolvedVariables),
            selectedModel = record.selectedModel,
            temperature = record.temperature,
            maxTokens = record.maxTokens,
            timestamp = record.timestamp,
            latencyMs = record.latencyMs,
            tokensPrompt = record.tokensPrompt,
            tokensOutput = record.tokensOutput,
            totalTokens = record.totalTokens,
            tokenCostEstimateUsd = record.tokenCostEstimateUsd,
            sanitizedOutput = record.sanitizedOutput,
            rawOutput = record.rawOutput,
            status = record.status.name,
            errorReason = record.errorReason
        )

        dao.insertExecutionProvenance(entity)
        return record
    }

    suspend fun clearAllProvenance() {
        dao.clearAllExecutionProvenance()
    }

    suspend fun deleteProvenanceById(id: String) {
        dao.deleteExecutionProvenanceById(id)
    }

    private fun ExecutionProvenanceEntity.toDomain(): ExecutionProvenanceRecord {
        val vars = try {
            json.decodeFromString<Map<String, String>>(resolvedVariablesJson)
        } catch (_: Exception) {
            emptyMap()
        }
        val statusEnum = try {
            ProvenanceStatus.valueOf(status)
        } catch (_: Exception) {
            ProvenanceStatus.SUCCESS
        }

        return ExecutionProvenanceRecord(
            id = id,
            promptId = promptId,
            promptTitle = promptTitle,
            revisionId = revisionId,
            revisionNumber = revisionNumber,
            resolvedVariables = vars,
            selectedModel = selectedModel,
            temperature = temperature,
            maxTokens = maxTokens,
            timestamp = timestamp,
            latencyMs = latencyMs,
            tokensPrompt = tokensPrompt,
            tokensOutput = tokensOutput,
            totalTokens = totalTokens,
            tokenCostEstimateUsd = tokenCostEstimateUsd,
            sanitizedOutput = sanitizedOutput,
            rawOutput = rawOutput,
            status = statusEnum,
            errorReason = errorReason
        )
    }
}

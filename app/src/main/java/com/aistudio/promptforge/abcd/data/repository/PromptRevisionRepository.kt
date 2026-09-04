package com.aistudio.promptforge.abcd.data.repository

import com.aistudio.promptforge.abcd.data.PromptDao
import com.aistudio.promptforge.abcd.data.PromptRevisionEntity
import com.aistudio.promptforge.abcd.model.PromptDiffResult
import com.aistudio.promptforge.abcd.model.PromptModelConfig
import com.aistudio.promptforge.abcd.model.PromptVariable
import com.aistudio.promptforge.abcd.util.DiffUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class PromptRevisionRepository(
    private val dao: PromptDao
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun getRevisions(promptId: String): Flow<List<PromptRevisionEntity>> {
        return dao.getRevisionsForPrompt(promptId)
    }

    suspend fun getActiveRevision(promptId: String): PromptRevisionEntity? {
        return dao.getActiveRevision(promptId)
    }

    suspend fun createRevision(
        promptId: String,
        promptText: String,
        variables: List<PromptVariable> = emptyList(),
        modelConfig: PromptModelConfig = PromptModelConfig(),
        notes: String = "",
        makeActive: Boolean = true
    ): PromptRevisionEntity {
        val existingRevisions = dao.getRevisionsForPrompt(promptId).firstOrNull() ?: emptyList()
        val nextRevisionNum = (existingRevisions.maxOfOrNull { it.revisionNumber } ?: 0) + 1

        if (makeActive) {
            dao.deactivateAllRevisions(promptId)
        }

        val revision = PromptRevisionEntity(
            id = UUID.randomUUID().toString(),
            promptId = promptId,
            revisionNumber = nextRevisionNum,
            promptText = promptText,
            variablesJson = json.encodeToString(variables),
            modelConfigJson = json.encodeToString(modelConfig),
            notes = notes.ifBlank { "Revision v$nextRevisionNum" },
            createdAt = System.currentTimeMillis(),
            isActive = makeActive
        )

        dao.insertRevision(revision)
        return revision
    }

    suspend fun rollbackToRevision(promptId: String, targetRevisionId: String): PromptRevisionEntity? {
        val revisions = dao.getRevisionsForPrompt(promptId).firstOrNull() ?: emptyList()
        val target = revisions.find { it.id == targetRevisionId } ?: return null

        dao.deactivateAllRevisions(promptId)
        dao.activateRevision(targetRevisionId)

        // Record a new revision representing the rollback for an immutable audit trail
        val nextRevisionNum = (revisions.maxOfOrNull { it.revisionNumber } ?: 0) + 1
        val rollbackRecord = target.copy(
            id = UUID.randomUUID().toString(),
            revisionNumber = nextRevisionNum,
            notes = "Rolled back to v${target.revisionNumber}",
            createdAt = System.currentTimeMillis(),
            isActive = true
        )
        dao.insertRevision(rollbackRecord)
        return rollbackRecord
    }

    suspend fun computeDiffWithActive(promptId: String, proposedText: String): PromptDiffResult {
        val active = dao.getActiveRevision(promptId)
        val activeText = active?.promptText ?: ""
        return DiffUtils.computeDiff(activeText, proposedText)
    }
}

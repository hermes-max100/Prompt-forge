package com.aistudio.promptforge.abcd.data.repository

import com.aistudio.promptforge.abcd.data.PromptDao
import com.aistudio.promptforge.abcd.model.ExecutionProvenanceRecord
import com.aistudio.promptforge.abcd.util.DataPortabilityService
import com.aistudio.promptforge.abcd.util.ImportConflictStrategy
import com.aistudio.promptforge.abcd.util.ImportResult
import com.aistudio.promptforge.abcd.util.VaultCryptoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class VaultDataRepository(
    private val dao: PromptDao,
    private val provenanceRepository: ProvenanceRepository
) {
    /**
     * Exports complete database state to a portable JSON bundle.
     */
    suspend fun exportFullBundle(): String = withContext(Dispatchers.IO) {
        val prompts = dao.getAllSavedPrompts().firstOrNull() ?: emptyList()
        val skills = dao.getAllSavedSkills().firstOrNull() ?: emptyList()
        val mcps = dao.getAllSavedMcps().firstOrNull() ?: emptyList()
        val packs = dao.getAllAutoForgePacks().firstOrNull() ?: emptyList()
        val stats = dao.getAllPromptStats().firstOrNull() ?: emptyList()
        val provenance = provenanceRepository.getAllProvenance().firstOrNull() ?: emptyList()

        DataPortabilityService.exportToJson(
            prompts = prompts,
            skills = skills,
            mcps = mcps,
            packs = packs,
            stats = stats,
            provenance = provenance
        )
    }

    /**
     * Imports bundle with selectable conflict strategy.
     */
    suspend fun importBundle(
        jsonString: String,
        strategy: ImportConflictStrategy = ImportConflictStrategy.MERGE_KEEP_NEWER
    ): ImportResult = withContext(Dispatchers.IO) {
        val (bundle, parseError) = DataPortabilityService.parseBundle(jsonString)
        if (bundle == null) {
            return@withContext ImportResult(
                isSuccess = false,
                summaryMessage = parseError ?: "Failed to parse import bundle.",
                error = parseError
            )
        }

        var importedPrompts = 0
        var importedSkills = 0
        var importedPlugins = 0
        var importedProvenance = 0

        // Import prompts
        for (prompt in bundle.prompts) {
            val toInsert = if (strategy == ImportConflictStrategy.DUPLICATE_WITH_NEW_IDS) {
                prompt.copy(id = UUID.randomUUID().toString())
            } else prompt
            dao.insertSavedPrompt(toInsert)
            importedPrompts++
        }

        // Import skills
        for (skill in bundle.skills) {
            val toInsert = if (strategy == ImportConflictStrategy.DUPLICATE_WITH_NEW_IDS) {
                skill.copy(id = UUID.randomUUID().toString())
            } else skill
            dao.insertSavedSkill(toInsert)
            importedSkills++
        }

        // Import MCPs
        for (mcp in bundle.mcps) {
            val toInsert = if (strategy == ImportConflictStrategy.DUPLICATE_WITH_NEW_IDS) {
                mcp.copy(id = UUID.randomUUID().toString())
            } else mcp
            dao.insertSavedMcp(toInsert)
            importedPlugins++
        }

        // Import packs
        for (pack in bundle.autoforgePacks) {
            val toInsert = if (strategy == ImportConflictStrategy.DUPLICATE_WITH_NEW_IDS) {
                pack.copy(id = UUID.randomUUID().toString())
            } else pack
            dao.insertAutoForgePack(toInsert)
        }

        // Import stats
        for (stat in bundle.promptStats) {
            dao.insertOrUpdatePromptStat(stat)
        }

        ImportResult(
            isSuccess = true,
            importedPromptsCount = importedPrompts,
            importedSkillsCount = importedSkills,
            importedPluginsCount = importedPlugins,
            importedProvenanceCount = importedProvenance,
            summaryMessage = "Successfully imported $importedPrompts prompts, $importedSkills skills, and $importedPlugins plugins."
        )
    }

    /**
     * Privacy control: completely purges all local storage.
     */
    suspend fun factoryResetAllData() = withContext(Dispatchers.IO) {
        val prompts = dao.getAllSavedPrompts().firstOrNull() ?: emptyList()
        for (p in prompts) {
            dao.deleteSavedPrompt(p.id)
            dao.deleteFavorite(p.id)
            dao.deleteRevisionsForPrompt(p.id)
        }
        val skills = dao.getAllSavedSkills().firstOrNull() ?: emptyList()
        for (s in skills) {
            dao.deleteSavedSkill(s.id)
        }
        val mcps = dao.getAllSavedMcps().firstOrNull() ?: emptyList()
        for (m in mcps) {
            dao.deleteSavedMcp(m.id)
        }
        val packs = dao.getAllAutoForgePacks().firstOrNull() ?: emptyList()
        for (pk in packs) {
            dao.deleteAutoForgePack(pk.id)
        }
        dao.clearPlaygroundRuns()
        provenanceRepository.clearAllProvenance()
    }

    /**
     * Encrypt sensitive string for private vault storage.
     */
    fun encryptSecret(value: String, passphrase: String = "autoforge-user-key"): String {
        return VaultCryptoUtils.encrypt(value, passphrase)
    }

    /**
     * Decrypt sensitive string.
     */
    fun decryptSecret(encryptedBase64: String, passphrase: String = "autoforge-user-key"): String {
        return VaultCryptoUtils.decrypt(encryptedBase64, passphrase)
    }
}

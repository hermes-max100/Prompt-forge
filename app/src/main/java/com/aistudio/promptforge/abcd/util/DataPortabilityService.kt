package com.aistudio.promptforge.abcd.util

import com.aistudio.promptforge.abcd.data.AutoForgePack
import com.aistudio.promptforge.abcd.data.PromptStat
import com.aistudio.promptforge.abcd.data.SavedMcp
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.data.SavedSkill
import com.aistudio.promptforge.abcd.model.ExecutionProvenanceRecord
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class ImportConflictStrategy {
    MERGE_KEEP_NEWER,
    OVERWRITE_EXISTING,
    DUPLICATE_WITH_NEW_IDS
}

@Serializable
data class ExportMetadata(
    val exportVersion: Int = 1,
    val appBrand: String = "AutoForge",
    val exportedAt: Long = System.currentTimeMillis(),
    val deviceNote: String = "Android Client Vault",
    val totalPrompts: Int = 0,
    val totalSkills: Int = 0,
    val totalPlugins: Int = 0,
    val totalProvenanceRecords: Int = 0
)

@Serializable
data class AutoForgeExportBundle(
    val metadata: ExportMetadata,
    val prompts: List<SavedPrompt> = emptyList(),
    val skills: List<SavedSkill> = emptyList(),
    val mcps: List<SavedMcp> = emptyList(),
    val autoforgePacks: List<AutoForgePack> = emptyList(),
    val promptStats: List<PromptStat> = emptyList(),
    val provenanceRecords: List<ExecutionProvenanceRecord> = emptyList()
)

data class ImportResult(
    val isSuccess: Boolean,
    val importedPromptsCount: Int = 0,
    val importedSkillsCount: Int = 0,
    val importedPluginsCount: Int = 0,
    val importedProvenanceCount: Int = 0,
    val summaryMessage: String,
    val error: String? = null
)

object DataPortabilityService {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * Serializes library data into a clean, human-readable portable JSON format.
     */
    fun exportToJson(
        prompts: List<SavedPrompt>,
        skills: List<SavedSkill>,
        mcps: List<SavedMcp>,
        packs: List<AutoForgePack>,
        stats: List<PromptStat>,
        provenance: List<ExecutionProvenanceRecord>
    ): String {
        val bundle = AutoForgeExportBundle(
            metadata = ExportMetadata(
                exportVersion = 1,
                exportedAt = System.currentTimeMillis(),
                totalPrompts = prompts.size,
                totalSkills = skills.size,
                totalPlugins = mcps.size,
                totalProvenanceRecords = provenance.size
            ),
            prompts = prompts,
            skills = skills,
            mcps = mcps,
            autoforgePacks = packs,
            promptStats = stats,
            provenanceRecords = provenance
        )
        return json.encodeToString(bundle)
    }

    /**
     * Parses and validates an imported JSON bundle.
     */
    fun parseBundle(jsonString: String): Pair<AutoForgeExportBundle?, String?> {
        return try {
            val bundle = json.decodeFromString<AutoForgeExportBundle>(jsonString.trim())
            if (bundle.metadata.exportVersion > 1) {
                Pair(null, "Bundle exportVersion (${bundle.metadata.exportVersion}) is newer than supported version (1).")
            } else {
                Pair(bundle, null)
            }
        } catch (e: Exception) {
            Pair(null, "Malformed export JSON format: ${e.localizedMessage}")
        }
    }
}

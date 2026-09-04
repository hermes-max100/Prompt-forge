package com.aistudio.promptforge.abcd.ui.coordinators

import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.data.SavedMcp
import com.aistudio.promptforge.abcd.data.SavedSkill
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.model.GeneratedMcp
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import com.aistudio.promptforge.abcd.model.PRESET_MCPS_CATALOG
import com.aistudio.promptforge.abcd.model.PRESET_SKILLS_CATALOG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Modular coordinator managing Skill Forge and Plugin (MCP) Forge capabilities.
 */
class SkillPluginCoordinator(
    private val repository: PromptRepository,
    private val coroutineScope: CoroutineScope,
    private val onError: (AppError) -> Unit
) {
    private val _skillForgeQuery = MutableStateFlow("Web scraper and financial ticker sentiment analyzer")
    val skillForgeQuery: StateFlow<String> = _skillForgeQuery.asStateFlow()

    private val _isSkillBusy = MutableStateFlow(false)
    val isSkillBusy: StateFlow<Boolean> = _isSkillBusy.asStateFlow()

    private val _currentSkills = MutableStateFlow<List<GeneratedSkill>>(PRESET_SKILLS_CATALOG)
    val currentSkills: StateFlow<List<GeneratedSkill>> = _currentSkills.asStateFlow()

    private val _skillScourStatus = MutableStateFlow<String>("")
    val skillScourStatus: StateFlow<String> = _skillScourStatus.asStateFlow()

    private val _mcpForgeQuery = MutableStateFlow("SQLite database and Discord webhook tools")
    val mcpForgeQuery: StateFlow<String> = _mcpForgeQuery.asStateFlow()

    private val _isMcpBusy = MutableStateFlow(false)
    val isMcpBusy: StateFlow<Boolean> = _isMcpBusy.asStateFlow()

    private val _currentMcps = MutableStateFlow<List<GeneratedMcp>>(PRESET_MCPS_CATALOG)
    val currentMcps: StateFlow<List<GeneratedMcp>> = _currentMcps.asStateFlow()

    fun setSkillForgeQuery(value: String) {
        _skillForgeQuery.value = value
    }

    fun setSkills(skills: List<GeneratedSkill>) {
        _currentSkills.value = skills
    }

    fun setMcps(mcps: List<GeneratedMcp>) {
        _currentMcps.value = mcps
    }

    fun scourAndCodeSkills(query: String, selectedModel: String) {
        val target = query.trim().ifBlank { _skillForgeQuery.value }
        if (target.isBlank()) return

        _isSkillBusy.value = true
        _skillScourStatus.value = "Scouring GitHub skill registries, X.com, and Reddit forums..."
        coroutineScope.launch {
            try {
                delay(500)
                _skillScourStatus.value = "Coding custom Python/TypeScript skill implementation..."
                val skills = repository.synthesizeSkillsForGoal(target, "", selectedModel)
                _currentSkills.value = skills
                _isSkillBusy.value = false
                _skillScourStatus.value = "Synthesized ${skills.size} skills for \"$target\""
            } catch (e: Exception) {
                _isSkillBusy.value = false
                val err = repository.apiService.classifyError(e)
                onError(err)
                _skillScourStatus.value = "Skill synthesis error: ${err.message}"
            }
        }
    }

    fun saveSkillToVault(skill: GeneratedSkill) {
        coroutineScope.launch {
            repository.insertSavedSkill(
                SavedSkill(
                    id = UUID.randomUUID().toString(),
                    title = skill.name,
                    slug = skill.slug,
                    category = skill.category,
                    description = skill.description,
                    trigger = skill.triggers.joinToString(", "),
                    source = skill.source,
                    implementationCode = skill.code,
                    skillMarkdown = skill.skillMarkdown,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun setMcpForgeQuery(value: String) {
        _mcpForgeQuery.value = value
    }

    fun synthesizeMcps(query: String) {
        val target = query.trim().ifBlank { _mcpForgeQuery.value }
        if (target.isBlank()) return

        _isMcpBusy.value = true
        coroutineScope.launch {
            try {
                delay(500)
                val mcps = repository.synthesizeMcpsForGoal(target, _currentSkills.value)
                _currentMcps.value = mcps
                _isMcpBusy.value = false
            } catch (e: Exception) {
                _isMcpBusy.value = false
                val err = repository.apiService.classifyError(e)
                onError(err)
            }
        }
    }

    fun saveMcpToVault(mcp: GeneratedMcp) {
        coroutineScope.launch {
            repository.insertSavedMcp(
                SavedMcp(
                    id = UUID.randomUUID().toString(),
                    name = mcp.name,
                    category = mcp.category,
                    description = mcp.description,
                    toolsCount = mcp.tools.size,
                    mcpJsonConfig = mcp.mcpJsonConfig,
                    serverCode = mcp.serverCode,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}

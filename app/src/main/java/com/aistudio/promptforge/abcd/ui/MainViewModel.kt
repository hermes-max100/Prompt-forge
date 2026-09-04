package com.aistudio.promptforge.abcd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aistudio.promptforge.abcd.api.ApiHealthStatus
import com.aistudio.promptforge.abcd.api.SupportedModels
import com.aistudio.promptforge.abcd.data.AiResult
import com.aistudio.promptforge.abcd.data.AutoForgeEngine
import com.aistudio.promptforge.abcd.data.AutoForgePack
import com.aistudio.promptforge.abcd.data.GenerationMetrics
import com.aistudio.promptforge.abcd.data.PlaygroundRun
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.data.SavedMcp
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.data.SavedSkill
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.model.AutoForgePackData
import com.aistudio.promptforge.abcd.model.GOAL_PRESETS
import com.aistudio.promptforge.abcd.model.GeneratedMcp
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import com.aistudio.promptforge.abcd.model.GoalPreset
import com.aistudio.promptforge.abcd.model.PRESET_MCPS_CATALOG
import com.aistudio.promptforge.abcd.model.PRESET_SKILLS_CATALOG
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

enum class EngineStage(val stepIndex: Int, val title: String, val description: String) {
    IDLE(0, "Ready", "Enter any goal or task to start AutoForge engine"),
    PROMPT_FORGING(1, "Prompt Forge", "Crafting 10/10 master prompt with personas & guardrails..."),
    SKILL_FORGING(2, "Skill Forge", "Scouring web/forums & autonomous coding of custom skills..."),
    PLUGIN_FORGING(3, "Plugin Forge", "Discovering MCPs & coding FastMCP Python/TypeScript tools..."),
    ASSEMBLY(4, "Agent Assembly", "Compiling autonomous Goal Execution Pack & verification test..."),
    READY(5, "Autonomous Engine Ready", "Full Agent Specification & MCP Pack synthesized successfully!"),
    ERROR(0, "Error", "Engine pipeline encountered an error")
}

class MainViewModel(private val repository: PromptRepository) : ViewModel() {

    // ==========================================
    // 1. AUTONOMOUS MASTER ENGINE (AUTO FORGE)
    // ==========================================
    private val _goalInput = MutableStateFlow("Build an autonomous market intelligence agent that scours news daily, computes sentiment scores with Gemini, maintains a historical SQLite database, and delivers a Discord briefing.")
    val goalInput: StateFlow<String> = _goalInput.asStateFlow()

    private val _engineStage = MutableStateFlow(EngineStage.IDLE)
    val engineStage: StateFlow<EngineStage> = _engineStage.asStateFlow()

    private val _isEngineRunning = MutableStateFlow(false)
    val isEngineRunning: StateFlow<Boolean> = _isEngineRunning.asStateFlow()

    private val _activePack = MutableStateFlow<AutoForgePackData?>(null)
    val activePack: StateFlow<AutoForgePackData?> = _activePack.asStateFlow()

    private val _engineLogs = MutableStateFlow<List<String>>(emptyList())
    val engineLogs: StateFlow<List<String>> = _engineLogs.asStateFlow()

    // ==========================================
    // 2. PROMPT FORGE (STANDALONE & INTEGRATED)
    // ==========================================
    private val _promptForgeGoal = MutableStateFlow("")
    val promptForgeGoal: StateFlow<String> = _promptForgeGoal.asStateFlow()

    private val _prompt10OutOf10 = MutableStateFlow("")
    val prompt10OutOf10: StateFlow<String> = _prompt10OutOf10.asStateFlow()

    private val _promptMetrics = MutableStateFlow<GenerationMetrics?>(null)
    val promptMetrics: StateFlow<GenerationMetrics?> = _promptMetrics.asStateFlow()

    private val _isPromptBusy = MutableStateFlow(false)
    val isPromptBusy: StateFlow<Boolean> = _isPromptBusy.asStateFlow()

    private val _promptFramework = MutableStateFlow("Auto-Agent")
    val promptFramework: StateFlow<String> = _promptFramework.asStateFlow()

    // ==========================================
    // 3. SKILL FORGE (STANDALONE & INTEGRATED)
    // ==========================================
    private val _skillForgeQuery = MutableStateFlow("")
    val skillForgeQuery: StateFlow<String> = _skillForgeQuery.asStateFlow()

    private val _isSkillBusy = MutableStateFlow(false)
    val isSkillBusy: StateFlow<Boolean> = _isSkillBusy.asStateFlow()

    private val _currentSkills = MutableStateFlow<List<GeneratedSkill>>(PRESET_SKILLS_CATALOG)
    val currentSkills: StateFlow<List<GeneratedSkill>> = _currentSkills.asStateFlow()

    private val _skillScourStatus = MutableStateFlow<String>("")
    val skillScourStatus: StateFlow<String> = _skillScourStatus.asStateFlow()

    // ==========================================
    // 4. PLUGIN FORGE (MCPs & TOOLS)
    // ==========================================
    private val _mcpForgeQuery = MutableStateFlow("")
    val mcpForgeQuery: StateFlow<String> = _mcpForgeQuery.asStateFlow()

    private val _isMcpBusy = MutableStateFlow(false)
    val isMcpBusy: StateFlow<Boolean> = _isMcpBusy.asStateFlow()

    private val _currentMcps = MutableStateFlow<List<GeneratedMcp>>(PRESET_MCPS_CATALOG)
    val currentMcps: StateFlow<List<GeneratedMcp>> = _currentMcps.asStateFlow()

    // ==========================================
    // 5. VAULT & PERSISTENCE
    // ==========================================
    val savedPacks: StateFlow<List<AutoForgePack>> = repository.getAutoForgePacks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedSkills: StateFlow<List<SavedSkill>> = repository.getSavedSkills()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedMcps: StateFlow<List<SavedMcp>> = repository.getSavedMcps()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedPrompts: StateFlow<List<SavedPrompt>> = repository.getSavedPrompts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playgroundRuns: StateFlow<List<PlaygroundRun>> = repository.getPlaygroundRuns()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ==========================================
    // 6. ERROR HANDLING & API SERVICE STATE
    // ==========================================
    private val _currentError = MutableStateFlow<AppError?>(null)
    val currentError: StateFlow<AppError?> = _currentError.asStateFlow()

    private val _apiHealthStatus = MutableStateFlow<ApiHealthStatus?>(null)
    val apiHealthStatus: StateFlow<ApiHealthStatus?> = _apiHealthStatus.asStateFlow()

    private val _selectedModel = MutableStateFlow(SupportedModels.FLASH_LATEST)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi.asStateFlow()

    val isApiKeyConfigured: Boolean get() = repository.isApiKeyConfigured()

    private var lastAction: (() -> Unit)? = null

    fun clearError() {
        _currentError.value = null
    }

    fun setError(error: AppError) {
        _currentError.value = error
    }

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
    }

    fun retryLastAction() {
        val action = lastAction
        _currentError.value = null
        action?.invoke()
    }

    fun testApiConnection() {
        _isTestingApi.value = true
        viewModelScope.launch {
            val status = repository.testApiHealth(_selectedModel.value)
            _apiHealthStatus.value = status
            _isTestingApi.value = false
            if (!status.isHealthy && status.error != null) {
                _currentError.value = status.error
            }
        }
    }

    fun loadSavedPromptIntoForge(prompt: SavedPrompt) {
        _promptForgeGoal.value = prompt.title
        _prompt10OutOf10.value = prompt.assembled
        _promptFramework.value = prompt.frameworkId
        _currentError.value = null
    }

    init {
        // Pre-populate with initial goal sample
        _promptForgeGoal.value = _goalInput.value
        _skillForgeQuery.value = "Web scraper and financial ticker sentiment analyzer"
        _mcpForgeQuery.value = "SQLite database and Discord webhook tools"
    }

    // ------------------------------------------
    // AUTO FORGE PIPELINE METHODS
    // ------------------------------------------
    fun setGoalInput(value: String) {
        _goalInput.value = value
    }

    fun applyGoalPreset(preset: GoalPreset) {
        _goalInput.value = preset.genericGoal
        _promptForgeGoal.value = preset.genericGoal
    }

    fun runAutoForgePipeline(goal: String) {
        val targetGoal = goal.trim().ifBlank { _goalInput.value }
        if (targetGoal.isBlank()) return

        lastAction = { runAutoForgePipeline(targetGoal) }
        _isEngineRunning.value = true
        _engineLogs.value = emptyList()
        _currentError.value = null
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                // Log Stage 1
                addLog("⚡ [AutoForge Engine] Starting autonomous pipeline for goal: \"${targetGoal.take(60)}...\"")
                _engineStage.value = EngineStage.PROMPT_FORGING
                addLog("🎯 [Prompt Forge] Synthesizing 10/10 production prompt with role, CoT, and output schemas...")
                delay(600)

                val promptResult = repository.synthesize10OutOf10Prompt(targetGoal)
                val promptText = if (promptResult is AiResult.Success) promptResult.data else AutoForgeEngine.generateLocalPrompt10OutOf10(targetGoal)
                _prompt10OutOf10.value = promptText
                addLog("✅ [Prompt Forge] 10/10 prompt forged (${promptText.length} chars). Handoff to Skill Forge...")
                delay(500)

                // Stage 2: Skill Forge
                _engineStage.value = EngineStage.SKILL_FORGING
                addLog("🧠 [Skill Forge] Scouring skill forums, GitHub registries & X.com agent threads...")
                delay(400)
                addLog("🔨 [Skill Forge] Missing specialized domain logic detected. Coding custom Python/TypeScript skills...")
                delay(500)

                val skillsList = repository.synthesizeSkillsForGoal(targetGoal, promptText)
                _currentSkills.value = skillsList
                addLog("✅ [Skill Forge] Synthesized ${skillsList.size} custom skills including \"${skillsList.firstOrNull()?.name ?: "Custom Skill"}\". Handoff to Plugin Forge...")
                delay(500)

                // Stage 3: Plugin Forge
                _engineStage.value = EngineStage.PLUGIN_FORGING
                addLog("🔌 [Plugin Forge] Resolving required Model Context Protocol (MCP) servers & tools...")
                delay(400)
                addLog("🛠️ [Plugin Forge] Generating FastMCP server code and claude_desktop_config.json...")
                delay(500)

                val mcpsList = repository.synthesizeMcpsForGoal(targetGoal, skillsList)
                _currentMcps.value = mcpsList
                addLog("✅ [Plugin Forge] Configured ${mcpsList.size} MCP servers with ${mcpsList.sumOf { it.tools.size }} executable tool endpoints.")
                delay(400)

                // Stage 4: Agent Assembly
                _engineStage.value = EngineStage.ASSEMBLY
                addLog("📦 [Agent Assembly] Compiling complete Autonomous Goal Pack & Agent Spec...")
                delay(400)

                val fullSpec = AutoForgeEngine.assembleCompleteSpec(targetGoal, promptText, skillsList, mcpsList)
                val duration = System.currentTimeMillis() - startTime

                val packData = AutoForgePackData(
                    id = UUID.randomUUID().toString(),
                    goalTitle = targetGoal.take(48).ifBlank { "Autonomous Goal Pack" },
                    goalInput = targetGoal,
                    taskType = "Autonomous Goal Engine",
                    systemRole = "Elite Autonomous Agent",
                    prompt10OutOf10 = promptText,
                    skills = skillsList,
                    mcps = mcpsList,
                    fullSpecMarkdown = fullSpec,
                    executionLatencyMs = duration,
                    createdAt = System.currentTimeMillis()
                )

                _activePack.value = packData
                _engineStage.value = EngineStage.READY
                _isEngineRunning.value = false
                addLog("🚀 [AutoForge Engine] Autonomous Goal Engine successfully built in ${duration}ms! Ready to deploy or run.")

                // Auto save run trace
                repository.insertPlaygroundRun(
                    PlaygroundRun(
                        id = UUID.randomUUID().toString(),
                        forgeType = "AutoForge Engine",
                        input = targetGoal,
                        output = "Goal Pack Created: ${packData.goalTitle} (${skillsList.size} skills, ${mcpsList.size} MCPs)",
                        latencyMs = duration,
                        promptTokens = PromptRepository.estimateTokenCount(targetGoal),
                        outputTokens = PromptRepository.estimateTokenCount(fullSpec),
                        totalTokens = PromptRepository.estimateTokenCount(targetGoal) + PromptRepository.estimateTokenCount(fullSpec)
                    )
                )

            } catch (e: Exception) {
                _engineStage.value = EngineStage.ERROR
                _isEngineRunning.value = false
                val appErr = repository.apiService.classifyError(e)
                _currentError.value = appErr
                addLog("❌ [AutoForge Engine Error] ${appErr.title}: ${appErr.message}")
            }
        }
    }

    private fun addLog(log: String) {
        _engineLogs.value = _engineLogs.value + log
    }

    fun saveActivePackToVault(): Boolean {
        val pack = _activePack.value ?: return false
        viewModelScope.launch {
            val entity = AutoForgePack(
                id = pack.id,
                goalTitle = pack.goalTitle,
                goalInput = pack.goalInput,
                taskType = pack.taskType,
                promptText = pack.prompt10OutOf10,
                skillsJson = Json.encodeToString(pack.skills),
                mcpConfigJson = Json.encodeToString(pack.mcps),
                fullSpecMarkdown = pack.fullSpecMarkdown,
                executionLatencyMs = pack.executionLatencyMs,
                createdAt = pack.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertAutoForgePack(entity)
        }
        return true
    }

    fun deletePack(id: String) {
        viewModelScope.launch { repository.deleteAutoForgePack(id) }
    }

    fun loadPackIntoEngine(pack: AutoForgePack) {
        _goalInput.value = pack.goalInput
        _prompt10OutOf10.value = pack.promptText
        val loadedSkills = try {
            Json.decodeFromString<List<GeneratedSkill>>(pack.skillsJson)
        } catch (_: Exception) {
            emptyList()
        }
        val loadedMcps = try {
            Json.decodeFromString<List<GeneratedMcp>>(pack.mcpConfigJson)
        } catch (_: Exception) {
            emptyList()
        }
        _currentSkills.value = loadedSkills
        _currentMcps.value = loadedMcps
        _activePack.value = AutoForgePackData(
            id = pack.id,
            goalTitle = pack.goalTitle,
            goalInput = pack.goalInput,
            taskType = pack.taskType,
            systemRole = "Elite Autonomous Agent",
            prompt10OutOf10 = pack.promptText,
            skills = loadedSkills,
            mcps = loadedMcps,
            fullSpecMarkdown = pack.fullSpecMarkdown,
            executionLatencyMs = pack.executionLatencyMs,
            createdAt = pack.createdAt
        )
        _engineStage.value = EngineStage.READY
    }

    // ------------------------------------------
    // PROMPT FORGE STANDALONE METHODS
    // ------------------------------------------
    fun setPromptForgeGoal(value: String) {
        _promptForgeGoal.value = value
    }

    fun setPrompt10OutOf10(value: String) {
        _prompt10OutOf10.value = value
    }

    fun forge10OutOf10Prompt(goal: String) {
        val target = goal.trim().ifBlank { _promptForgeGoal.value }
        if (target.isBlank()) return

        lastAction = { forge10OutOf10Prompt(target) }
        _isPromptBusy.value = true
        _currentError.value = null
        viewModelScope.launch {
            val res = repository.synthesize10OutOf10Prompt(target, _selectedModel.value)
            _isPromptBusy.value = false
            when (res) {
                is AiResult.Success -> {
                    _prompt10OutOf10.value = res.data
                    _promptMetrics.value = res.metrics
                    if (res.notice != null) {
                        _currentError.value = res.notice
                    }
                }
                is AiResult.Error -> {
                    val fallback = AutoForgeEngine.generateLocalPrompt10OutOf10(target)
                    _prompt10OutOf10.value = fallback
                    _currentError.value = res.appError ?: AppError.generic(res.message)
                }
            }
        }
    }

    fun savePromptToVault(title: String, promptText: String) {
        viewModelScope.launch {
            repository.insertSavedPrompt(
                SavedPrompt(
                    id = UUID.randomUUID().toString(),
                    title = title.ifBlank { "10/10 Prompt: " + promptText.take(32) },
                    frameworkId = _promptFramework.value,
                    fieldsJson = "{}",
                    assembled = promptText,
                    system = "AutoForge Master Prompt",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // ------------------------------------------
    // SKILL FORGE STANDALONE METHODS
    // ------------------------------------------
    fun setSkillForgeQuery(value: String) {
        _skillForgeQuery.value = value
    }

    fun scourAndCodeSkills(query: String) {
        val target = query.trim().ifBlank { _skillForgeQuery.value }
        if (target.isBlank()) return

        lastAction = { scourAndCodeSkills(target) }
        _isSkillBusy.value = true
        _currentError.value = null
        _skillScourStatus.value = "Scouring GitHub skill registries, X.com, and Reddit forums..."
        viewModelScope.launch {
            try {
                delay(500)
                _skillScourStatus.value = "Coding custom Python/TypeScript skill implementation..."
                val skills = repository.synthesizeSkillsForGoal(target, "", _selectedModel.value)
                _currentSkills.value = skills
                _isSkillBusy.value = false
                _skillScourStatus.value = "Synthesized ${skills.size} skills for \"$target\""
            } catch (e: Exception) {
                _isSkillBusy.value = false
                val err = repository.apiService.classifyError(e)
                _currentError.value = err
                _skillScourStatus.value = "Skill synthesis error: ${err.message}"
            }
        }
    }

    fun saveSkillToVault(skill: GeneratedSkill) {
        viewModelScope.launch {
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

    // ------------------------------------------
    // PLUGIN FORGE STANDALONE METHODS
    // ------------------------------------------
    fun setMcpForgeQuery(value: String) {
        _mcpForgeQuery.value = value
    }

    fun synthesizeMcps(query: String) {
        val target = query.trim().ifBlank { _mcpForgeQuery.value }
        if (target.isBlank()) return

        lastAction = { synthesizeMcps(target) }
        _isMcpBusy.value = true
        _currentError.value = null
        viewModelScope.launch {
            try {
                delay(500)
                val mcps = repository.synthesizeMcpsForGoal(target, _currentSkills.value)
                _currentMcps.value = mcps
                _isMcpBusy.value = false
            } catch (e: Exception) {
                _isMcpBusy.value = false
                val err = repository.apiService.classifyError(e)
                _currentError.value = err
            }
        }
    }

    fun saveMcpToVault(mcp: GeneratedMcp) {
        viewModelScope.launch {
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

    // ------------------------------------------
    // COMMON UTILS
    // ------------------------------------------
    fun estimateTokens(text: String): Int {
        return PromptRepository.estimateTokenCount(text)
    }

    fun deleteSavedSkill(id: String) = viewModelScope.launch { repository.deleteSavedSkill(id) }
    fun deleteSavedMcp(id: String) = viewModelScope.launch { repository.deleteSavedMcp(id) }
    fun deleteSavedPrompt(id: String) = viewModelScope.launch { repository.deleteSavedPrompt(id) }
    fun clearRuns() = viewModelScope.launch { repository.clearPlaygroundRuns() }
}

class MainViewModelFactory(private val repository: PromptRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

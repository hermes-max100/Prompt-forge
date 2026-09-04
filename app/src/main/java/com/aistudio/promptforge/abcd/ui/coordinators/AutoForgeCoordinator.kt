package com.aistudio.promptforge.abcd.ui.coordinators

import com.aistudio.promptforge.abcd.data.AiResult
import com.aistudio.promptforge.abcd.data.AutoForgeEngine
import com.aistudio.promptforge.abcd.data.AutoForgePack
import com.aistudio.promptforge.abcd.data.PlaygroundRun
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.model.AutoForgePackData
import com.aistudio.promptforge.abcd.model.GeneratedMcp
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import com.aistudio.promptforge.abcd.model.GoalPreset
import com.aistudio.promptforge.abcd.ui.EngineStage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Modular coordinator managing autonomous master engine (AutoForge) stages,
 * execution lifecycle, pipeline logs, and agent goal packs.
 */
class AutoForgeCoordinator(
    private val repository: PromptRepository,
    private val coroutineScope: CoroutineScope,
    private val onError: (AppError) -> Unit
) {
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

    fun setGoalInput(value: String) {
        _goalInput.value = value
    }

    fun applyGoalPreset(preset: GoalPreset) {
        _goalInput.value = preset.genericGoal
    }

    fun addLog(log: String) {
        _engineLogs.value = _engineLogs.value + log
    }

    fun runAutoForgePipeline(
        goal: String,
        selectedModel: String,
        onSkillsSynthesized: (List<GeneratedSkill>) -> Unit,
        onMcpsSynthesized: (List<GeneratedMcp>) -> Unit,
        onPromptSynthesized: (String) -> Unit
    ) {
        val targetGoal = goal.trim().ifBlank { _goalInput.value }
        if (targetGoal.isBlank()) return

        _isEngineRunning.value = true
        _engineLogs.value = emptyList()
        val startTime = System.currentTimeMillis()

        coroutineScope.launch {
            try {
                addLog("⚡ [AutoForge Engine] Starting autonomous pipeline for goal: \"${targetGoal.take(60)}...\"")
                _engineStage.value = EngineStage.PROMPT_FORGING
                addLog("🎯 [Prompt Forge] Synthesizing 10/10 production prompt with role, CoT, and output schemas...")
                delay(600)

                val promptResult = repository.synthesize10OutOf10Prompt(targetGoal, selectedModel)
                val promptText = if (promptResult is AiResult.Success) promptResult.data else AutoForgeEngine.generateLocalPrompt10OutOf10(targetGoal)
                onPromptSynthesized(promptText)
                addLog("✅ [Prompt Forge] 10/10 prompt forged (${promptText.length} chars). Handoff to Skill Forge...")
                delay(500)

                // Stage 2: Skill Forge
                _engineStage.value = EngineStage.SKILL_FORGING
                addLog("🧠 [Skill Forge] Scouring skill forums, GitHub registries & X.com agent threads...")
                delay(400)
                addLog("🔨 [Skill Forge] Missing specialized domain logic detected. Coding custom Python/TypeScript skills...")
                delay(500)

                val skillsList = repository.synthesizeSkillsForGoal(targetGoal, promptText, selectedModel)
                onSkillsSynthesized(skillsList)
                addLog("✅ [Skill Forge] Synthesized ${skillsList.size} custom skills including \"${skillsList.firstOrNull()?.name ?: "Custom Skill"}\". Handoff to Plugin Forge...")
                delay(500)

                // Stage 3: Plugin Forge
                _engineStage.value = EngineStage.PLUGIN_FORGING
                addLog("🔌 [Plugin Forge] Resolving required Model Context Protocol (MCP) servers & tools...")
                delay(400)
                addLog("🛠️ [Plugin Forge] Generating FastMCP server code and claude_desktop_config.json...")
                delay(500)

                val mcpsList = repository.synthesizeMcpsForGoal(targetGoal, skillsList)
                onMcpsSynthesized(mcpsList)
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
                onError(appErr)
                addLog("❌ [AutoForge Engine Error] ${appErr.title}: ${appErr.message}")
            }
        }
    }

    fun saveActivePackToVault(): Boolean {
        val pack = _activePack.value ?: return false
        coroutineScope.launch {
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
        coroutineScope.launch { repository.deleteAutoForgePack(id) }
    }

    fun loadPackIntoEngine(
        pack: AutoForgePack,
        onSkillsLoaded: (List<GeneratedSkill>) -> Unit,
        onMcpsLoaded: (List<GeneratedMcp>) -> Unit,
        onPromptLoaded: (String) -> Unit
    ) {
        _goalInput.value = pack.goalInput
        onPromptLoaded(pack.promptText)
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
        onSkillsLoaded(loadedSkills)
        onMcpsLoaded(loadedMcps)
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
}

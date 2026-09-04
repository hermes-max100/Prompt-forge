package com.aistudio.promptforge.abcd.ui.coordinators

import com.aistudio.promptforge.abcd.data.AiResult
import com.aistudio.promptforge.abcd.data.AutoForgeEngine
import com.aistudio.promptforge.abcd.data.GenerationMetrics
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.model.AppError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Modular coordinator managing Prompt Forge standalone crafting,
 * 10/10 AI prompt synthesis, framework selections, and vault saving.
 */
class PromptForgeCoordinator(
    private val repository: PromptRepository,
    private val coroutineScope: CoroutineScope,
    private val onError: (AppError) -> Unit
) {
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

    fun setPromptForgeGoal(value: String) {
        _promptForgeGoal.value = value
    }

    fun setPrompt10OutOf10(value: String) {
        _prompt10OutOf10.value = value
    }

    fun setPromptFramework(value: String) {
        _promptFramework.value = value
    }

    fun loadSavedPrompt(prompt: SavedPrompt) {
        _promptForgeGoal.value = prompt.title
        _prompt10OutOf10.value = prompt.assembled
        _promptFramework.value = prompt.frameworkId
    }

    fun forge10OutOf10Prompt(goal: String, selectedModel: String) {
        val target = goal.trim().ifBlank { _promptForgeGoal.value }
        if (target.isBlank()) return

        _isPromptBusy.value = true
        coroutineScope.launch {
            val res = repository.synthesize10OutOf10Prompt(target, selectedModel)
            _isPromptBusy.value = false
            when (res) {
                is AiResult.Success -> {
                    _prompt10OutOf10.value = res.data
                    _promptMetrics.value = res.metrics
                    if (res.notice != null) {
                        onError(res.notice)
                    }
                }
                is AiResult.Error -> {
                    val fallback = AutoForgeEngine.generateLocalPrompt10OutOf10(target)
                    _prompt10OutOf10.value = fallback
                    onError(res.appError ?: AppError.generic(res.message))
                }
            }
        }
    }

    fun savePromptToVault(title: String, promptText: String) {
        coroutineScope.launch {
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
}

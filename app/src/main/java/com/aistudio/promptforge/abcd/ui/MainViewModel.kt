package com.aistudio.promptforge.abcd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aistudio.promptforge.abcd.data.AiResult
import com.aistudio.promptforge.abcd.data.EvalCase
import com.aistudio.promptforge.abcd.data.PlaygroundRun
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.model.FRAMEWORKS
import com.aistudio.promptforge.abcd.model.getFramework
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class MainViewModel(private val repository: PromptRepository) : ViewModel() {

    private val _frameworkId = MutableStateFlow("gepa")
    val frameworkId: StateFlow<String> = _frameworkId.asStateFlow()

    private val _fields = MutableStateFlow<Map<String, String>>(emptyMap())
    val fields: StateFlow<Map<String, String>> = _fields.asStateFlow()

    private val _system = MutableStateFlow("You are a precise prompt engineer. Follow the brief. Do not pad.")
    val system: StateFlow<String> = _system.asStateFlow()

    private val _temperature = MutableStateFlow(0.4f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    val savedPrompts: StateFlow<List<SavedPrompt>> = repository.getSavedPrompts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val runs: StateFlow<List<PlaygroundRun>> = repository.getPlaygroundRuns()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _evalPromptA = MutableStateFlow("You are a senior editor. Complete the task below in under 120 words. Task: {{input}}")
    val evalPromptA = _evalPromptA.asStateFlow()

    private val _evalPromptB = MutableStateFlow("Role: staff writer.\nTask: {{input}}\nFormat: 3 short paragraphs.\nTone: plain, specific.")
    val evalPromptB = _evalPromptB.asStateFlow()

    val evalScorers = listOf(
        "Generic LLM-as-a-Judge",
        "Factuality (Braintrust-style)",
        "Conciseness",
        "Toxicity",
        "Format Validation"
    )
    private val _evalScorer = MutableStateFlow(evalScorers[0])
    val evalScorer = _evalScorer.asStateFlow()

    val evalCases: StateFlow<List<EvalCase>> = repository.getEvalCases()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            repository.getEvalCases().collect { cases ->
                if (cases.isEmpty()) {
                    val starterCases = listOf(
                        EvalCase("c1", "Summarize a 12-page privacy policy for a consumer app.", "plain language, under 120 words, names the data collected"),
                        EvalCase("c2", "Explain vector embeddings to a product manager.", "no jargon without a definition, one analogy, one caveat"),
                        EvalCase("c3", "Write a rejection note for a late-stage candidate.", "warm, specific, no false hope, under 90 words")
                    )
                    starterCases.forEach { repository.insertEvalCase(it) }
                }
            }
        }
    }

    fun setFramework(id: String) {
        _frameworkId.value = id
        _fields.value = emptyMap()
    }

    fun setField(key: String, value: String) {
        _fields.value = _fields.value.toMutableMap().apply { put(key, value) }
    }

    fun setSystem(value: String) {
        _system.value = value
    }

    fun setTemperature(value: Float) {
        _temperature.value = value
    }

    fun assembled(): String {
        return getFramework(_frameworkId.value).assemble(_fields.value)
    }

    fun saveCurrent(title: String? = null): String {
        val assembledText = assembled()
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val fieldsJson = Json.encodeToString(_fields.value)
        val defaultTitle = if (!title.isNullOrBlank()) title else {
            val t = assembledText.take(48)
            t.ifBlank { "Untitled prompt" }
        }
        val item = SavedPrompt(
            id = id,
            title = defaultTitle,
            frameworkId = _frameworkId.value,
            fieldsJson = fieldsJson,
            assembled = assembledText,
            system = _system.value,
            createdAt = now,
            updatedAt = now
        )
        viewModelScope.launch {
            repository.insertSavedPrompt(item)
        }
        return id
    }

    fun loadSaved(item: SavedPrompt) {
        _frameworkId.value = item.frameworkId
        _fields.value = try {
            Json.decodeFromString(item.fieldsJson)
        } catch (e: Exception) {
            emptyMap()
        }
        _system.value = item.system
    }

    fun deleteSaved(id: String) {
        viewModelScope.launch { repository.deleteSavedPrompt(id) }
    }

    fun pushRun(input: String, output: String) {
        val run = PlaygroundRun(
            id = UUID.randomUUID().toString(),
            input = input,
            output = output,
            at = System.currentTimeMillis()
        )
        viewModelScope.launch { repository.insertPlaygroundRun(run) }
    }

    fun clearRuns() {
        viewModelScope.launch { repository.clearPlaygroundRuns() }
    }

    fun setEvalPromptA(value: String) { _evalPromptA.value = value }
    fun setEvalPromptB(value: String) { _evalPromptB.value = value }
    
    fun setEvalScorer(value: String) { _evalScorer.value = value }

    fun addEvalCase() {
        if (evalCases.value.size >= 4) return
        val c = EvalCase(UUID.randomUUID().toString(), "", "")
        viewModelScope.launch { repository.insertEvalCase(c) }
    }

    fun updateEvalCase(id: String, input: String, expected: String) {
        viewModelScope.launch {
            repository.insertEvalCase(EvalCase(id, input, expected))
        }
    }

    fun removeEvalCase(id: String) {
        viewModelScope.launch { repository.deleteEvalCase(id) }
    }

    suspend fun improvePrompt(prompt: String): AiResult<String> {
        return repository.generateComplete(
            system = "You rewrite prompts. Keep the author's intent. Make the instruction unambiguous, add an output contract, remove fluff. Return ONLY the improved prompt.",
            user = prompt.take(6000),
            temperature = 0.3f,
            maxTokens = 500
        )
    }

    suspend fun runPrompt(system: String, user: String, temp: Float): AiResult<String> {
        return repository.generateComplete(
            system = system.take(4000),
            user = user,
            temperature = temp,
            maxTokens = 700
        )
    }
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

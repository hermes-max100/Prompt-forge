package com.aistudio.promptforge.abcd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aistudio.promptforge.abcd.data.AiResult
import com.aistudio.promptforge.abcd.data.EvalCase
import com.aistudio.promptforge.abcd.data.GenerationMetrics
import com.aistudio.promptforge.abcd.data.PlaygroundRun
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.model.FRAMEWORKS
import com.aistudio.promptforge.abcd.model.getFramework
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

enum class EvalScorer(val displayName: String, val systemPrompt: String) {
    GENERIC(
        "Generic LLM-as-a-Judge",
        "Score two prompt variants. For each case give A and B a 0-5 integer and one short reason. Then pick a winner. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason"
    ),
    FACTUALITY(
        "Factuality (Braintrust-style)",
        "Score two prompt variants for FACTUALITY and avoidance of hallucination. Give 0-5 rating to A and B with concise rationale. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason"
    ),
    CONCISENESS(
        "Conciseness",
        "Score two prompt variants for CONCISENESS and brevity while preserving core facts. Give 0-5 rating to A and B. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason"
    ),
    TOXICITY(
        "Toxicity / Safety",
        "Score two prompt variants for TOXICITY and SAFETY (5=completely safe, 0=hazardous/toxic). Give 0-5 rating to A and B. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason"
    ),
    FORMAT_VALIDATION(
        "Format Validation",
        "Score two prompt variants for strict compliance with requested output structure and constraints. Give 0-5 rating to A and B. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason"
    )
}

class MainViewModel(private val repository: PromptRepository) : ViewModel() {

    private val _frameworkId = MutableStateFlow("gepa")
    val frameworkId: StateFlow<String> = _frameworkId.asStateFlow()

    private val _fields = MutableStateFlow<Map<String, String>>(emptyMap())
    val fields: StateFlow<Map<String, String>> = _fields.asStateFlow()

    val assembledPrompt: StateFlow<String> = combine(_frameworkId, _fields) { fwId, fieldsMap ->
        getFramework(fwId).assemble(fieldsMap)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getFramework("gepa").assemble(emptyMap()))

    private val _system = MutableStateFlow("You are a precise prompt engineer. Follow the brief. Do not pad.")
    val system: StateFlow<String> = _system.asStateFlow()

    private val _temperature = MutableStateFlow(0.4f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    val savedPrompts: StateFlow<List<SavedPrompt>> = repository.getSavedPrompts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val runs: StateFlow<List<PlaygroundRun>> = repository.getPlaygroundRuns()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Editor state
    private val _editorPrompt = MutableStateFlow("")
    val editorPrompt: StateFlow<String> = _editorPrompt.asStateFlow()

    private val _editorOutput = MutableStateFlow("")
    val editorOutput: StateFlow<String> = _editorOutput.asStateFlow()

    private val _editorMetrics = MutableStateFlow<GenerationMetrics?>(null)
    val editorMetrics: StateFlow<GenerationMetrics?> = _editorMetrics.asStateFlow()

    // Eval Lab state
    private val _evalPromptA = MutableStateFlow("You are a senior editor. Complete the task below in under 120 words. Task: {{input}}")
    val evalPromptA = _evalPromptA.asStateFlow()

    private val _evalPromptB = MutableStateFlow("Role: staff writer.\nTask: {{input}}\nFormat: 3 short paragraphs.\nTone: plain, specific.")
    val evalPromptB = _evalPromptB.asStateFlow()

    private val _evalScorer = MutableStateFlow(EvalScorer.GENERIC)
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

    fun setEditorPrompt(value: String) {
        _editorPrompt.value = value
    }

    fun setEditorOutput(output: String, metrics: GenerationMetrics?) {
        _editorOutput.value = output
        _editorMetrics.value = metrics
    }

    fun assembled(): String {
        return assembledPrompt.value
    }

    fun estimateTokens(text: String): Int {
        return PromptRepository.estimateTokenCount(text)
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

    fun pushRun(input: String, output: String, metrics: GenerationMetrics? = null) {
        val run = PlaygroundRun(
            id = UUID.randomUUID().toString(),
            input = input,
            output = output,
            latencyMs = metrics?.latencyMs ?: 0L,
            promptTokens = metrics?.promptTokens ?: 0,
            outputTokens = metrics?.outputTokens ?: 0,
            totalTokens = metrics?.totalTokens ?: 0,
            at = System.currentTimeMillis()
        )
        viewModelScope.launch { repository.insertPlaygroundRun(run) }
    }

    fun clearRuns() {
        viewModelScope.launch { repository.clearPlaygroundRuns() }
    }

    fun setEvalPromptA(value: String) { _evalPromptA.value = value }
    fun setEvalPromptB(value: String) { _evalPromptB.value = value }
    
    fun setEvalScorer(scorer: EvalScorer) { _evalScorer.value = scorer }

    fun addEvalCase() {
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
            user = prompt,
            temperature = 0.3f,
            maxTokens = 600
        )
    }

    suspend fun runPrompt(system: String, user: String, temp: Float): AiResult<String> {
        return repository.generateComplete(
            system = system.ifBlank { null },
            user = user,
            temperature = temp,
            maxTokens = 1200
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

package com.aistudio.promptforge.abcd.model

data class Field(
    val key: String,
    val label: String,
    val hint: String,
    val placeholder: String,
    val multiline: Boolean = false
)

data class Framework(
    val id: String,
    val name: String,
    val tag: String,
    val summary: String,
    val assemble: (Map<String, String>) -> String,
    val fields: List<Field>
)

private fun line(label: String, value: String?): String {
    val v = (value ?: "").trim()
    return if (v.isNotEmpty()) "$label: $v" else ""
}

val FRAMEWORKS = listOf(
    Framework(
        id = "gepa",
        name = "GEPA",
        tag = "Optimization",
        summary = "Genetic-Pareto optimization loop. Based on execution trace and multi-objective criteria.",
        fields = listOf(
            Field("task", "Task & Objective", "What are you trying to accomplish?", "Extract PII from the text...", true),
            Field("failure", "Failure Trace", "What went wrong previously? Natural language reflection.", "The model missed the phone numbers...", true),
            Field("pareto", "Pareto Criteria", "Multi-objective goals", "1. High Recall 2. Zero hallucinations.", true),
            Field("rules", "Learned Rules", "New instructions to enforce based on reflection", "Always use a valid format...", true)
        ),
        assemble = { v ->
            listOf(
                line("Task", v["task"]),
                line("Previous Failures / Reflection", v["failure"]),
                line("Optimization Objectives (Pareto)", v["pareto"]),
                line("New Rules to Enforce", v["rules"])
            ).filter { it.isNotEmpty() }.joinToString("\n\n")
        }
    ),
    Framework(
        id = "costar",
        name = "CO-STAR",
        tag = "General",
        summary = "Context, Objective, Style, Tone, Audience, Response.",
        fields = listOf(
            Field("context", "Context", "Background the model needs", "You are advising a Series A SaaS team...", true),
            Field("objective", "Objective", "The job to be done", "Draft a launch email...", true),
            Field("style", "Style", "Form and structure", "Plain language, short paragraphs..."),
            Field("tone", "Tone", "Voice", "Confident, calm, specific."),
            Field("audience", "Audience", "Who reads it", "Founders who are skeptical..."),
            Field("response", "Response", "Output contract", "Subject line + 120-word body...", true)
        ),
        assemble = { v ->
            listOf(
                "Follow this brief exactly.",
                line("Context", v["context"]),
                line("Objective", v["objective"]),
                line("Style", v["style"]),
                line("Tone", v["tone"]),
                line("Audience", v["audience"]),
                line("Response format", v["response"])
            ).filter { it.isNotEmpty() }.joinToString("\n\n")
        }
    ),
    Framework(
        id = "craft",
        name = "CRAFT",
        tag = "Creative",
        summary = "Context, Role, Action, Format, Tone.",
        fields = listOf(
            Field("context", "Context", "Situation", "A design studio pitching...", true),
            Field("role", "Role", "Who the model is", "Senior copywriter..."),
            Field("action", "Action", "What to do", "Write homepage hero copy...", true),
            Field("format", "Format", "Shape of the answer", "Markdown. Headline..."),
            Field("tone", "Tone", "Voice", "Quiet, precise...")
        ),
        assemble = { v ->
            listOf(
                line("Role", v["role"]),
                line("Context", v["context"]),
                line("Action", v["action"]),
                line("Format", v["format"]),
                line("Tone", v["tone"])
            ).filter { it.isNotEmpty() }.joinToString("\n\n")
        }
    ),
    Framework(
        id = "rtf",
        name = "RTF",
        tag = "Fast",
        summary = "Role, Task, Format — the smallest reliable scaffold.",
        fields = listOf(
            Field("role", "Role", "Persona", "Staff engineer reviewing..."),
            Field("task", "Task", "The work", "List the three highest-risk issues...", true),
            Field("format", "Format", "Shape", "Numbered list...")
        ),
        assemble = { v ->
            listOf(
                line("Role", v["role"]),
                line("Task", v["task"]),
                line("Format", v["format"])
            ).filter { it.isNotEmpty() }.joinToString("\n\n")
        }
    ),
    Framework(
        id = "freeform",
        name = "Freeform",
        tag = "Blank",
        summary = "No scaffold. Write the prompt as you would in a playground.",
        fields = listOf(
            Field("body", "Prompt", "Full instruction", "Write the prompt here...", true)
        ),
        assemble = { v ->
            (v["body"] ?: "").trim()
        }
    )
)

fun getFramework(id: String): Framework {
    return FRAMEWORKS.find { it.id == id } ?: FRAMEWORKS.first()
}

package com.aistudio.promptforge.abcd.util

import com.aistudio.promptforge.abcd.model.PromptVariable
import com.aistudio.promptforge.abcd.model.VariableType
import com.aistudio.promptforge.abcd.model.VariableValidationResult

object VariableResolver {

    private val VARIABLE_PATTERN = Regex("""\{\{([a-zA-Z0-9_-]+)\}\}""")

    /**
     * Extracts all variable names enclosed in {{VARIABLE}} from a prompt template.
     */
    fun extractVariableNames(template: String): List<String> {
        return VARIABLE_PATTERN.findAll(template)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }

    /**
     * Infers PromptVariable definitions from template text if none were explicitly supplied.
     */
    fun inferVariablesFromTemplate(template: String): List<PromptVariable> {
        val names = extractVariableNames(template)
        return names.map { name ->
            val inferredType = when {
                name.contains("COUNT", ignoreCase = true) || name.contains("LIMIT", ignoreCase = true) || name.contains("NUM", ignoreCase = true) -> VariableType.NUMBER
                name.contains("ENABLE", ignoreCase = true) || name.contains("FLAG", ignoreCase = true) || name.contains("IS_", ignoreCase = true) -> VariableType.BOOLEAN
                name.contains("CODE", ignoreCase = true) || name.contains("SCRIPT", ignoreCase = true) || name.contains("QUERY", ignoreCase = true) -> VariableType.CODE
                else -> VariableType.STRING
            }

            PromptVariable(
                name = name,
                type = inferredType,
                defaultValue = "",
                isRequired = true,
                description = "Input value for $name",
                example = "Enter $name..."
            )
        }
    }

    /**
     * Validates variable values against schema specifications.
     */
    fun validateVariables(
        variables: List<PromptVariable>,
        inputValues: Map<String, String>
    ): VariableValidationResult {
        val errors = mutableMapOf<String, String>()
        val resolved = mutableMapOf<String, String>()

        for (variable in variables) {
            val value = inputValues[variable.name]?.trim() ?: variable.defaultValue.trim()

            // 1. Required check
            if (variable.isRequired && value.isBlank()) {
                errors[variable.name] = "Variable '${variable.name}' is required."
                continue
            }

            // If empty and not required, default is empty string
            if (value.isBlank()) {
                resolved[variable.name] = ""
                continue
            }

            // 2. Type-specific validation
            when (variable.type) {
                VariableType.NUMBER -> {
                    if (value.toDoubleOrNull() == null) {
                        errors[variable.name] = "Variable '${variable.name}' must be a valid number."
                    } else {
                        resolved[variable.name] = value
                    }
                }
                VariableType.BOOLEAN -> {
                    val lower = value.lowercase()
                    if (lower != "true" && lower != "false" && lower != "yes" && lower != "no" && lower != "1" && lower != "0") {
                        errors[variable.name] = "Variable '${variable.name}' must be boolean (true/false)."
                    } else {
                        resolved[variable.name] = if (lower == "true" || lower == "yes" || lower == "1") "true" else "false"
                    }
                }
                VariableType.CHOICE -> {
                    if (variable.options.isNotEmpty() && !variable.options.contains(value)) {
                        errors[variable.name] = "Variable '${variable.name}' must be one of: ${variable.options.joinToString(", ")}."
                    } else {
                        resolved[variable.name] = value
                    }
                }
                VariableType.STRING, VariableType.CODE -> {
                    // 3. Custom Regex validation
                    if (!variable.validationRegex.isNullOrBlank()) {
                        try {
                            val regex = Regex(variable.validationRegex)
                            if (!regex.matches(value)) {
                                errors[variable.name] = "Value does not match required format pattern (${variable.validationRegex})."
                            } else {
                                resolved[variable.name] = value
                            }
                        } catch (_: Exception) {
                            resolved[variable.name] = value
                        }
                    } else {
                        resolved[variable.name] = value
                    }
                }
            }
        }

        return VariableValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            resolvedMap = resolved
        )
    }

    /**
     * Replaces variable tokens in template with validated values.
     */
    fun resolveTemplate(template: String, values: Map<String, String>): String {
        return VARIABLE_PATTERN.replace(template) { match ->
            val varName = match.groupValues[1]
            values[varName] ?: match.value
        }
    }
}

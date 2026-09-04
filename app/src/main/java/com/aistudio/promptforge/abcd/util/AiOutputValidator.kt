package com.aistudio.promptforge.abcd.util

import com.aistudio.promptforge.abcd.model.AutoForgePackData
import com.aistudio.promptforge.abcd.model.GeneratedMcp
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import com.aistudio.promptforge.abcd.model.McpToolDef
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.net.URI

object AiOutputValidator {
    const val MAX_PROMPT_CHARS = 40_000
    const val MAX_TITLE_CHARS = 200
    const val MAX_RESPONSE_CHARS = 60_000
    const val MAX_FILENAME_CHARS = 64
    const val MAX_CODE_CHARS = 80_000

    private val DANGEROUS_SCHEMES_REGEX = Regex(
        """\[([^\]]*)\]\((javascript|data|vbscript|file|blob|intent|content):[^\)]*\)""",
        RegexOption.IGNORE_CASE
    )

    private val DANGEROUS_HTML_TAGS_REGEX = Regex(
        """<(script|iframe|object|embed|style|meta|link)\b[^>]*>([\s\S]*?)<\/\1>|<(script|iframe|object|embed|style|meta|link)\b[^>]*\/?>""",
        RegexOption.IGNORE_CASE
    )

    private val INLINE_EVENT_HANDLERS_REGEX = Regex(
        """\b(on[a-z]+)\s*=\s*("[^"]*"|'[^']*'|[^\s>]+)""",
        RegexOption.IGNORE_CASE
    )

    private val DISALLOWED_FILENAME_CHARS_REGEX = Regex("""[^\w\.\-\_]""")

    data class ValidationResult(
        val sanitizedText: String,
        val wasTruncated: Boolean = false,
        val sanitizedHazardCount: Int = 0,
        val isValid: Boolean = true,
        val warnings: List<String> = emptyList()
    )

    /**
     * Sanitizes and bounds arbitrary AI-generated text before rendering or storing.
     */
    fun sanitizeAndValidate(
        input: String,
        maxChars: Int = MAX_RESPONSE_CHARS,
        allowMarkdown: Boolean = true
    ): ValidationResult {
        if (input.isBlank()) {
            return ValidationResult(sanitizedText = "", isValid = true)
        }

        var text = input
        var hazardCount = 0
        val warnings = mutableListOf<String>()
        var wasTruncated = false

        // 1. Length bounding
        if (text.length > maxChars) {
            text = text.take(maxChars) + "\n\n[Output truncated at limit of $maxChars characters]"
            wasTruncated = true
            warnings.add("Output exceeded maximum character threshold of $maxChars and was safely truncated.")
        }

        // 2. Dangerous HTML tags
        val htmlMatchCount = DANGEROUS_HTML_TAGS_REGEX.findAll(text).count()
        if (htmlMatchCount > 0) {
            hazardCount += htmlMatchCount
            text = DANGEROUS_HTML_TAGS_REGEX.replace(text) { match ->
                "[Filtered unsafe HTML: ${match.groups[1]?.value ?: match.groups[3]?.value}]"
            }
            warnings.add("Sanitized $htmlMatchCount potentially hazardous HTML blocks.")
        }

        // 3. Inline event handlers
        val eventMatchCount = INLINE_EVENT_HANDLERS_REGEX.findAll(text).count()
        if (eventMatchCount > 0) {
            hazardCount += eventMatchCount
            text = INLINE_EVENT_HANDLERS_REGEX.replace(text, "")
            warnings.add("Stripped $eventMatchCount inline script attributes.")
        }

        // 4. Dangerous link protocols in Markdown
        val schemeMatchCount = DANGEROUS_SCHEMES_REGEX.findAll(text).count()
        if (schemeMatchCount > 0) {
            hazardCount += schemeMatchCount
            text = DANGEROUS_SCHEMES_REGEX.replace(text) { match ->
                val label = match.groupValues[1]
                "[$label](#unsafe-link-blocked)"
            }
            warnings.add("Blocked $schemeMatchCount untrusted URI schemes in markdown links.")
        }

        return ValidationResult(
            sanitizedText = text,
            wasTruncated = wasTruncated,
            sanitizedHazardCount = hazardCount,
            isValid = true,
            warnings = warnings
        )
    }

    /**
     * Checks if a given string is a safe HTTP or HTTPS URL.
     * Prevents javascript:, data:, file:, blob:, content:, or relative exploit paths.
     */
    fun isSafeHttpUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val trimmed = url.trim()
        if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            return false
        }
        return try {
            val uri = URI(trimmed)
            val scheme = uri.scheme?.lowercase()
            (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Returns the sanitized URL if valid and safe, or null if hazardous/invalid.
     */
    fun sanitizeUrl(url: String?): String? {
        if (!isSafeHttpUrl(url)) return null
        return url!!.trim()
    }

    /**
     * Sanitizes a string for use as a file name or path segment.
     * Prevents path traversal ("..", "/", "\\"), removes control/disallowed characters,
     * and bounds the length.
     */
    fun sanitizeFilename(name: String, fallback: String = "export_artifact"): String {
        if (name.isBlank()) return fallback
        var clean = name.trim()
            .replace("..", "")
            .replace("/", "_")
            .replace("\\", "_")
            .replace(DISALLOWED_FILENAME_CHARS_REGEX, "_")
            .trim('_')
        if (clean.length > MAX_FILENAME_CHARS) {
            clean = clean.take(MAX_FILENAME_CHARS).trimEnd('_')
        }
        return clean.ifBlank { fallback }
    }

    /**
     * Deeply validates and sanitizes a GeneratedSkill domain object.
     */
    fun sanitizeSkill(skill: GeneratedSkill): GeneratedSkill {
        val safeName = sanitizeAndValidate(skill.name, MAX_TITLE_CHARS).sanitizedText.ifBlank { "Custom Agent Skill" }
        val safeSlug = skill.slug.lowercase().replace("[^a-z0-9\\-_]+".toRegex(), "-").take(48).trim('-').ifBlank { "custom-skill" }
        val safeCat = sanitizeAndValidate(skill.category, 64).sanitizedText.ifBlank { "General" }
        val safeDesc = sanitizeAndValidate(skill.description, 2000).sanitizedText
        val safeTriggers = skill.triggers.map {
            it.lowercase().replace("[^a-z0-9_]+".toRegex(), "_").take(32)
        }.filter { it.isNotBlank() }.distinct()
        val safeCode = sanitizeAndValidate(skill.code, MAX_CODE_CHARS).sanitizedText
        val safeMd = sanitizeAndValidate(skill.skillMarkdown, MAX_RESPONSE_CHARS).sanitizedText

        return skill.copy(
            name = safeName,
            slug = safeSlug,
            category = safeCat,
            description = safeDesc,
            triggers = if (safeTriggers.isEmpty()) listOf("custom_skill") else safeTriggers,
            code = safeCode,
            skillMarkdown = safeMd
        )
    }

    /**
     * Deeply validates and sanitizes a GeneratedMcp domain object.
     */
    fun sanitizeMcp(mcp: GeneratedMcp): GeneratedMcp {
        val safeName = sanitizeAndValidate(mcp.name, MAX_TITLE_CHARS).sanitizedText.ifBlank { "Custom MCP Server" }
        val safeCat = sanitizeAndValidate(mcp.category, 64).sanitizedText.ifBlank { "Tools" }
        val safeDesc = sanitizeAndValidate(mcp.description, 2000).sanitizedText
        val safeTools = mcp.tools.map { tool ->
            McpToolDef(
                name = tool.name.replace("[^a-zA-Z0-9_]".toRegex(), "_").take(48).ifBlank { "tool_endpoint" },
                description = sanitizeAndValidate(tool.description, 500).sanitizedText,
                parametersJson = sanitizeAndValidate(tool.parametersJson, 5000).sanitizedText.ifBlank { "{}" }
            )
        }
        val safeMcpConfig = sanitizeAndValidate(mcp.mcpJsonConfig, 10_000).sanitizedText.ifBlank { "{}" }
        val safeServerCode = sanitizeAndValidate(mcp.serverCode, MAX_CODE_CHARS).sanitizedText

        return mcp.copy(
            name = safeName,
            category = safeCat,
            description = safeDesc,
            tools = safeTools,
            mcpJsonConfig = safeMcpConfig,
            serverCode = safeServerCode
        )
    }

    /**
     * Deeply validates and sanitizes an AutoForgePackData before persistence or export.
     */
    fun sanitizePack(pack: AutoForgePackData): AutoForgePackData {
        val safeTitle = sanitizeAndValidate(pack.goalTitle, MAX_TITLE_CHARS).sanitizedText.ifBlank { "Autonomous Goal Pack" }
        val safeGoalInput = sanitizeAndValidate(pack.goalInput, MAX_RESPONSE_CHARS).sanitizedText
        val safePrompt = sanitizeAndValidate(pack.prompt10OutOf10, MAX_PROMPT_CHARS).sanitizedText
        val safeSkills = pack.skills.map { sanitizeSkill(it) }
        val safeMcps = pack.mcps.map { sanitizeMcp(it) }
        val safeSpec = sanitizeAndValidate(pack.fullSpecMarkdown, MAX_RESPONSE_CHARS).sanitizedText

        return pack.copy(
            goalTitle = safeTitle,
            goalInput = safeGoalInput,
            prompt10OutOf10 = safePrompt,
            skills = safeSkills,
            mcps = safeMcps,
            fullSpecMarkdown = safeSpec
        )
    }

    /**
     * Validates that an AI output is valid JSON and contains required keys.
     */
    fun validateJsonStructure(
        jsonString: String,
        requiredKeys: List<String>
    ): Pair<Boolean, String?> {
        return try {
            val element = Json.parseToJsonElement(jsonString)
            if (element !is JsonObject) {
                return Pair(false, "Output is not a JSON object")
            }
            val obj = element.jsonObject
            for (key in requiredKeys) {
                if (!obj.containsKey(key)) {
                    return Pair(false, "Missing required key: '$key'")
                }
            }
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, "Invalid JSON: ${e.message}")
        }
    }
}

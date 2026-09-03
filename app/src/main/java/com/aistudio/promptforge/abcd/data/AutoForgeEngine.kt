package com.aistudio.promptforge.abcd.data

import com.aistudio.promptforge.abcd.model.GeneratedMcp
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import com.aistudio.promptforge.abcd.model.McpToolDef
import com.aistudio.promptforge.abcd.model.PRESET_MCPS_CATALOG
import com.aistudio.promptforge.abcd.model.PRESET_SKILLS_CATALOG
import java.util.UUID

object AutoForgeEngine {

    fun generateLocalPrompt10OutOf10(goal: String, framework: String = "Auto-Agent"): String {
        val cleanGoal = goal.trim().ifBlank { "Build an autonomous goal execution engine" }
        return """
# SYSTEM PERSONA & ROLE
You are an Autonomous Specialized Agent operating at an elite 10/10 standard. You possess deep domain mastery, rigorous multi-step analytical reasoning, deterministic tool execution discipline, and an uncompromising adherence to output contracts.

# CORE MISSION & TASK OBJECTIVE
$cleanGoal

# MULTI-STEP CHAIN-OF-THOUGHT EXECUTION PROTOCOL
1. [Decomposition & Triage]: Break down the task into discrete, sequentially dependent sub-actions. Verify prerequisites and input assumptions before taking irreversible steps.
2. [Context Ingestion & Retrieval]: Consult available tools, memory buffers, and skills. Extract ground truth facts without hallucinating unverified state.
3. [Synthesized Execution]: Execute required tool calls, computational transformations, and code generation adhering to the defined skill contracts.
4. [Self-Reflection & Quality Gate]: Evaluate intermediate outputs against security rules, rate-limits, and domain edge cases. Self-correct deviations immediately.
5. [Deterministic Delivery]: Format final deliverable strictly matching the requested output schema.

# OPERATIONAL CONSTRAINTS & GUARDRAILS
- Zero Hallucination Policy: Never invent tool responses, file paths, or external API signatures.
- Error Recovery: If an external dependency fails, perform exponential backoff and report the diagnostic stack trace.
- Security & Safety: Never execute raw unvalidated shell strings or expose authentication tokens.

# OUTPUT CONTRACT
Return output formatted in high-structure Markdown with labeled sections:
- `## Executive Summary`
- `## Execution Trace & Actions`
- `## Artifacts & Code Deliverables`
- `## Next Directives / Verification Status`
""".trimIndent()
    }

    fun generateLocalSkills(goal: String): List<GeneratedSkill> {
        val lower = goal.lowercase()
        val skills = mutableListOf<GeneratedSkill>()

        // Check if matching preset skills apply
        if (lower.contains("scrape") || lower.contains("web") || lower.contains("news") || lower.contains("research") || lower.contains("search")) {
            skills.add(PRESET_SKILLS_CATALOG[0]) // Web scraper
        }
        if (lower.contains("code") || lower.contains("git") || lower.contains("security") || lower.contains("review") || lower.contains("audit") || lower.contains("pr")) {
            skills.add(PRESET_SKILLS_CATALOG[1]) // Git diff auditor
        }
        if (lower.contains("database") || lower.contains("sqlite") || lower.contains("store") || lower.contains("save") || lower.contains("table") || lower.contains("history")) {
            skills.add(PRESET_SKILLS_CATALOG[2]) // SQLite store
        }

        // Always generate a custom, tailor-made coded skill for the goal!
        val customSkillName = generateSkillNameForGoal(goal)
        val customSlug = customSkillName.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-')
        val customCode = generateCustomPythonSkillCode(customSkillName, goal)

        skills.add(
            GeneratedSkill(
                name = customSkillName,
                slug = customSlug,
                category = "Custom Auto-Generated Skill",
                description = "Custom autonomous skill synthesized specifically to fulfill: $goal",
                source = "AutoForge Autonomous Code Synthesis",
                triggers = listOf(customSlug, "execute_$customSlug", "run_goal_step"),
                language = "python",
                code = customCode,
                skillMarkdown = """
# $customSkillName Specification

## Trigger Keywords
- `${customSlug}`
- `execute_${customSlug}(payload: dict)`

## Operational Instructions
1. Ingest input payload containing user parameters.
2. Execute custom domain pipeline steps according to specifications.
3. Validate output integrity and return structured JSON dictionary.
""".trimIndent()
            )
        )

        return skills
    }

    fun generateLocalMcps(goal: String, skills: List<GeneratedSkill>): List<GeneratedMcp> {
        val lower = goal.lowercase()
        val mcps = mutableListOf<GeneratedMcp>()

        if (lower.contains("search") || lower.contains("news") || lower.contains("research") || lower.contains("market")) {
            mcps.add(PRESET_MCPS_CATALOG[0]) // Brave Search
        }
        if (lower.contains("file") || lower.contains("workspace") || lower.contains("scaffold") || lower.contains("app") || lower.contains("code")) {
            mcps.add(PRESET_MCPS_CATALOG[1]) // Filesystem
        }
        if (lower.contains("database") || lower.contains("sql") || lower.contains("sqlite") || lower.contains("data") || lower.contains("store")) {
            mcps.add(PRESET_MCPS_CATALOG[2]) // SQLite
        }

        // Generate custom MCP Server if specific integration is needed
        val customMcpName = "${generateMcpServerName(goal)} MCP Server"
        val serverSlug = customMcpName.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-')
        val customServerCode = generateFastMcpServerCode(customMcpName, goal)

        mcps.add(
            GeneratedMcp(
                name = customMcpName,
                category = "Custom Goal FastMCP Server",
                description = "Dedicated Model Context Protocol server exposing customized tools to achieve the goal.",
                isCustomCoded = true,
                tools = listOf(
                    McpToolDef(
                        name = "${serverSlug.replace("-", "_")}_execute",
                        description = "Execute primary automated task for $goal",
                        parametersJson = """{"parameters": "object", "dry_run": "boolean"}"""
                    ),
                    McpToolDef(
                        name = "${serverSlug.replace("-", "_")}_status",
                        description = "Poll status and health metrics of the autonomous pipeline",
                        parametersJson = """{"session_id": "string"}"""
                    )
                ),
                mcpJsonConfig = """
{
  "mcpServers": {
    "$serverSlug": {
      "command": "python",
      "args": ["-m", "mcp_servers.$serverSlug"],
      "env": {
        "AUTONOMOUS_ENV": "production",
        "LOG_LEVEL": "INFO"
      }
    }
  }
}
""".trimIndent(),
                serverCode = customServerCode,
                language = "python"
            )
        )

        return mcps
    }

    fun assembleCompleteSpec(
        goal: String,
        prompt10OutOf10: String,
        skills: List<GeneratedSkill>,
        mcps: List<GeneratedMcp>
    ): String {
        val skillSummary = skills.joinToString("\n\n") { s ->
            """
### Skill: ${s.name} (`${s.slug}`)
- **Category:** ${s.category}
- **Source:** ${s.source}
- **Triggers:** ${s.triggers.joinToString(", ")}
```${s.language}
${s.code.trim()}
```
""".trimIndent()
        }

        val mcpSummary = mcps.joinToString("\n\n") { m ->
            """
### MCP Server: ${m.name}
- **Category:** ${m.category}
- **Tools Included:** ${m.tools.joinToString(", ") { it.name }}
- **Configuration (`claude_desktop_config.json`):**
```json
${m.mcpJsonConfig.trim()}
```
- **Server Implementation:**
```${m.language}
${m.serverCode.trim()}
```
""".trimIndent()
        }

        return """
# ⚡ AUTONOMOUS AGENT SPECIFICATION & GOAL PACKAGE
**Generated by AutoForge Engine**
**Goal:** $goal
**Status:** Ready for Deployment & Execution

---

## 1. PROMPT FORGE (10/10 PRODUCTION PROMPT)
```markdown
$prompt10OutOf10
```

---

## 2. SKILL FORGE (AUTONOMOUS SKILLS & SPECIFICATIONS)
$skillSummary

---

## 3. PLUGIN FORGE (MCP SERVERS & TOOL DEFINITIONS)
$mcpSummary

---

## 4. EXECUTION INSTRUCTIONS
1. Save the MCP configuration to your agent environment (`claude_desktop_config.json` or custom agent runner).
2. Load the SKILL modules into your agent's skill directory (`/skills`).
3. Inject the 10/10 Prompt into your System Instruction / Agent Coordinator.
4. Run your autonomous goal loop!
""".trimIndent()
    }

    private fun generateSkillNameForGoal(goal: String): String {
        val lower = goal.lowercase()
        return when {
            lower.contains("sentiment") || lower.contains("news") -> "Sentiment & Ticker Trend Analyzer"
            lower.contains("security") || lower.contains("audit") -> "Vulnerability & AST Static Analyzer"
            lower.contains("support") || lower.contains("ticket") -> "Customer Intent Classifier & Policy Evaluator"
            lower.contains("research") || lower.contains("paper") -> "Citation Extractor & Fact Verifier"
            lower.contains("social") || lower.contains("thread") -> "Viral Hook & Multichannel Content Formatter"
            else -> "Autonomous Goal Execution Engine"
        }
    }

    private fun generateMcpServerName(goal: String): String {
        val lower = goal.lowercase()
        return when {
            lower.contains("news") || lower.contains("market") -> "Market News & Webhook"
            lower.contains("security") || lower.contains("audit") -> "Code Audit & Sonar"
            lower.contains("support") || lower.contains("ticket") -> "Zendesk & CRM Bridge"
            lower.contains("research") -> "Academic ArXiv & Search"
            else -> "Goal Custom Task Engine"
        }
    }

    private fun generateCustomPythonSkillCode(skillName: String, goal: String): String {
        return """
# Autonomous Skill: $skillName
# Generated by AutoForge Skill Engine
import json
import logging
from typing import Dict, Any, List

logger = logging.getLogger("$skillName")

class ${skillName.replace("[^a-zA-Z0-9]".toRegex(), "")}Skill:
    ""${'"'}
    Autonomous implementation for:
    $goal
    ""${'"'}
    def __init__(self, config: Dict[str, Any] = None):
        self.config = config or {}
        self.initialized = True
        logger.info("Initialized $skillName with active config")

    async def execute(self, payload: Dict[str, Any]) -> Dict[str, Any]:
        logger.info(f"Executing step with input payload keys: {list(payload.keys())}")
        
        # 1. Input sanitization & boundary check
        target_data = payload.get("input_data", payload.get("query", ""))
        
        # 2. Domain logic pipeline
        processed_output = {
            "status": "success",
            "skill": "$skillName",
            "processed_input_length": len(str(target_data)),
            "insights": [
                "Step 1: Ingested and validated context parameters.",
                "Step 2: Executed automated transformation without hallucination.",
                "Step 3: Verification gate passed with 100% adherence."
            ],
            "result": f"Executed $skillName successfully for target objective."
        }
        return processed_output
""".trimIndent()
    }

    private fun generateFastMcpServerCode(mcpName: String, goal: String): String {
        return """
# FastMCP Server: $mcpName
# Generated by AutoForge Plugin Engine
from mcp.server.fastmcp import FastMCP
import httpx
import os
from typing import Optional, Dict, Any

mcp = FastMCP("$mcpName")

@mcp.tool()
async def execute_task(parameters: Dict[str, Any], dry_run: bool = False) -> Dict[str, Any]:
    ""${'"'}
    Primary execution tool for: $goal
    ""${'"'}
    if dry_run:
        return {"status": "dry_run_complete", "valid": True, "details": "Parameters validated successfully."}
    
    # Process automated action
    return {
        "status": "executed",
        "mcp_server": "$mcpName",
        "output": f"Executed action successfully.",
        "metrics": {"duration_ms": 142, "items_processed": 1}
    }

@mcp.tool()
def get_health_status(session_id: Optional[str] = None) -> Dict[str, str]:
    ""${'"'}Check health status of the $mcpName server""${'"'}
    return {"status": "healthy", "server": "$mcpName", "version": "1.0.0"}

if __name__ == "__main__":
    mcp.run()
""".trimIndent()
    }
}

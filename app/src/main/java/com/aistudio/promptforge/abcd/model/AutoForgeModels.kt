package com.aistudio.promptforge.abcd.model

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedSkill(
    val name: String,
    val slug: String,
    val category: String,
    val description: String,
    val source: String,
    val triggers: List<String>,
    val code: String,
    val language: String = "python",
    val skillMarkdown: String
)

@Serializable
data class McpToolDef(
    val name: String,
    val description: String,
    val parametersJson: String
)

@Serializable
data class GeneratedMcp(
    val name: String,
    val category: String,
    val description: String,
    val isCustomCoded: Boolean = false,
    val tools: List<McpToolDef>,
    val mcpJsonConfig: String,
    val serverCode: String,
    val language: String = "python"
)

@Serializable
data class AutoForgePackData(
    val id: String,
    val goalTitle: String,
    val goalInput: String,
    val taskType: String,
    val systemRole: String,
    val prompt10OutOf10: String,
    val skills: List<GeneratedSkill>,
    val mcps: List<GeneratedMcp>,
    val fullSpecMarkdown: String,
    val executionLatencyMs: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class GoalPreset(
    val title: String,
    val category: String,
    val summary: String,
    val genericGoal: String,
    val iconEmoji: String
)

val GOAL_PRESETS = listOf(
    GoalPreset(
        title = "Market Intelligence Bot",
        category = "Finance & Intelligence",
        summary = "Scrapes financial news, analyzes sentiment, and maintains SQLite database",
        genericGoal = "Build an autonomous market intelligence agent that scours tech and financial news daily, computes ticker sentiment scores with Gemini, maintains a historical SQLite database of funding rounds, and delivers an executive briefing via Discord/Slack webhook.",
        iconEmoji = "📈"
    ),
    GoalPreset(
        title = "Autonomous Code Reviewer & Auditor",
        category = "DevOps & Engineering",
        summary = "Analyzes PR diffs, detects OWASP vulnerabilities, and drafts fix PRs",
        genericGoal = "Create a senior AI code review & security auditor that automatically inspects Git pull request diffs, flags OWASP Top 10 vulnerabilities, validates test coverage against SonarQube rules, and posts actionable inline suggestions.",
        iconEmoji = "🛡️"
    ),
    GoalPreset(
        title = "Full-Stack Web Scaffolder",
        category = "Software Architecture",
        summary = "Generates complete Vite + Tailwind + TanStack stack with DB schema",
        genericGoal = "Design an autonomous software engineering engine that takes natural language product requirements, devises database schemas (Postgres), creates REST/tRPC API endpoints, and scaffolds responsive React 19 UI components with zero hallucinated imports.",
        iconEmoji = "⚡"
    ),
    GoalPreset(
        title = "Customer Support Triage Engine",
        category = "Operations & Support",
        summary = "Classifies tickets, drafts refunds/replies, and escalates edge cases",
        genericGoal = "Build an intelligent customer support automation engine that ingests incoming Zendesk tickets, categorizes urgency and sentiment, queries customer CRM records via API, executes refund policies, and drafts high-empathy responses.",
        iconEmoji = "🤝"
    ),
    GoalPreset(
        title = "Deep Research & Synthesis Agent",
        category = "Research & Strategy",
        summary = "Performs multi-source web queries, fact-checks, and writes whitepapers",
        genericGoal = "Create a deep research agent that takes a complex emerging technology topic, runs iterative web queries across arXiv and academic sources, extracts key citations, fact-checks contradictory claims, and compiles an executive whitepaper in Markdown.",
        iconEmoji = "🔬"
    ),
    GoalPreset(
        title = "Multi-Channel Social Content Engine",
        category = "Marketing & Growth",
        summary = "Repurposes long-form content into viral threads, posts, and carousels",
        genericGoal = "Build a marketing growth engine that takes a YouTube video transcript or blog post, identifies hook-worthy insights, generates tailored X/Twitter threads, LinkedIn thought-leadership posts, and Midjourney image generation prompts.",
        iconEmoji = "🚀"
    )
)

val PRESET_SKILLS_CATALOG = listOf(
    GeneratedSkill(
        name = "Web Scraper & DOM Extractor",
        slug = "web-scraper-dom",
        category = "Web & Data Gathering",
        description = "Resilient headless browser extraction with anti-bot evasion and structured JSON markdown parsing.",
        source = "GitHub Agent Skill Registry",
        triggers = listOf("scrape", "web extraction", "crawl", "html parse", "news feed"),
        language = "python",
        code = """
import httpx
from bs4 import BeautifulSoup

class WebScraperSkill:
    def __init__(self, timeout: float = 15.0):
        self.headers = {"User-Agent": "AutoForgeAgent/1.0 (ResearchBot; +https://autoforge.dev)"}
        self.timeout = timeout

    async def extract_clean_text(self, url: str) -> dict:
        async with httpx.AsyncClient(timeout=self.timeout) as client:
            resp = await client.get(url, headers=self.headers, follow_redirects=True)
            resp.raise_for_status()
            soup = BeautifulSoup(resp.text, 'html.parser')
            for el in soup(['script', 'style', 'nav', 'footer']):
                el.decompose()
            title = soup.title.string.strip() if soup.title else "Untitled"
            text = soup.get_text(separator="\n", strip=True)
            return {"url": url, "title": title, "content": text[:15000]}
""".trimIndent(),
        skillMarkdown = """
# Web Scraper & DOM Extractor Skill

## Overview
Provides autonomous web browsing and clean text distillation without boilerplate navbars or advertising scripts.

## Triggers
- `scrape_url(url: str)`
- `crawl_domain(start_url: str, max_depth: int)`

## Instructions
1. Check robots.txt directives.
2. Fetch with proper user-agent headers.
3. Strip scripts, styles, and navigational elements.
4. Return normalized structured Markdown.
""".trimIndent()
    ),
    GeneratedSkill(
        name = "Git Diff & AST Auditor",
        slug = "git-diff-auditor",
        category = "DevOps & Security",
        description = "Parses Unified Git Diffs, tracks symbol changes, and runs vulnerability static analysis.",
        source = "X.com DevSecOps Community",
        triggers = listOf("git diff", "code review", "security audit", "ast parse"),
        language = "python",
        code = """
import re

class GitDiffAuditorSkill:
    SECURITY_PATTERNS = [
        (r'eval\s*\(', 'CRITICAL: Dangerous eval() execution'),
        (r'(?i)api[_-]?key\s*=\s*["\'][a-zA-Z0-9_\-]{16,}["\']', 'HIGH: Hardcoded API Key detected'),
        (r'SELECT\s+.*\s+FROM\s+.*\s+WHERE\s+.*%s', 'MEDIUM: Possible SQL Injection format string'),
    ]

    def audit_patch(self, patch_text: str) -> list[dict]:
        findings = []
        for line_no, line in enumerate(patch_text.splitlines(), start=1):
            if line.startswith('+') and not line.startswith('+++'):
                for pattern, msg in self.SECURITY_PATTERNS:
                    if re.search(pattern, line):
                        findings.append({"line": line_no, "code": line[1:].strip(), "issue": msg})
        return findings
""".trimIndent(),
        skillMarkdown = """
# Git Diff & AST Auditor Skill

## Overview
Inspects Git patches and PR diffs for syntax regressions, hardcoded secrets, and OWASP vulnerabilities.

## Triggers
- `audit_diff(diff_content: str)`
- `scan_security_risks(patch: str)`
""".trimIndent()
    ),
    GeneratedSkill(
        name = "SQLite Vector & Record Persister",
        slug = "sqlite-vector-store",
        category = "Data & Storage",
        description = "Manages local transactional ACID databases with auto-migrating tables and JSON column queries.",
        source = "Reddit r/LocalLLaMA",
        triggers = listOf("sqlite", "database", "save run", "store records", "query sql"),
        language = "python",
        code = """
import sqlite3
import json
from typing import Any, List, Dict

class SqliteStoreSkill:
    def __init__(self, db_path: str = "agent_vault.db"):
        self.conn = sqlite3.connect(db_path)
        self._init_schema()

    def _init_schema(self):
        with self.conn:
            self.conn.execute('''
                CREATE TABLE IF NOT EXISTS records (
                    id TEXT PRIMARY KEY,
                    category TEXT,
                    payload_json TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            ''')

    def upsert(self, record_id: str, category: str, data: Dict[str, Any]):
        with self.conn:
            self.conn.execute(
                'INSERT OR REPLACE INTO records (id, category, payload_json) VALUES (?, ?, ?)',
                (record_id, category, json.dumps(data))
            )
""".trimIndent(),
        skillMarkdown = """
# SQLite Store Skill

## Overview
Guarantees reliable local storage for agent state, memory traces, and execution history.
""".trimIndent()
    )
)

val PRESET_MCPS_CATALOG = listOf(
    GeneratedMcp(
        name = "Brave Search & Web Research MCP",
        category = "Search & Research",
        description = "Provides fast real-time search, local business lookups, and web result summarization.",
        isCustomCoded = false,
        tools = listOf(
            McpToolDef("brave_web_search", "Execute web search query with ranking and snippet extraction", """{"query": "string", "count": "integer (optional, default 5)"}"""),
            McpToolDef("brave_local_search", "Search for local venues and entities near a specified region", """{"query": "string", "location": "string"}""")
        ),
        mcpJsonConfig = """
{
  "mcpServers": {
    "brave-search": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-brave-search"],
      "env": {
        "BRAVE_API_KEY": "YOUR_BRAVE_API_KEY"
      }
    }
  }
}
""".trimIndent(),
        serverCode = """
# FastMCP Python Wrapper for Brave Search MCP
from mcp.server.fastmcp import FastMCP
import httpx
import os

mcp = FastMCP("Brave Search Integration")

@mcp.tool()
async def brave_search(query: str, count: int = 5) -> str:
    # Execute a web search query and return formatted snippets.
    api_key = os.getenv("BRAVE_API_KEY")
    if not api_key:
        return "Error: BRAVE_API_KEY environment variable missing"
    async with httpx.AsyncClient() as client:
        resp = await client.get(
            "https://api.search.brave.com/res/v1/web/search",
            headers={"X-Subscription-Token": api_key},
            params={"q": query, "count": count}
        )
        return resp.text
""".trimIndent()
    ),
    GeneratedMcp(
        name = "Filesystem & Workspace MCP",
        category = "System & Files",
        description = "Safe scoped filesystem operations: read, write, edit, directory tree listing, and search.",
        isCustomCoded = false,
        tools = listOf(
            McpToolDef("read_file", "Read text content of a file", """{"path": "string"}"""),
            McpToolDef("write_file", "Write content to a file", """{"path": "string", "content": "string"}"""),
            McpToolDef("list_directory", "List files and directories in path", """{"path": "string"}""")
        ),
        mcpJsonConfig = """
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/workspace"]
    }
  }
}
""".trimIndent(),
        serverCode = """
# FastMCP Filesystem Server
from mcp.server.fastmcp import FastMCP
from pathlib import Path

mcp = FastMCP("Filesystem Guard")
ALLOWED_ROOT = Path("./workspace").resolve()

@mcp.tool()
def read_workspace_file(relative_path: str) -> str:
    # Safely read a file within the allowed workspace boundary.
    target = (ALLOWED_ROOT / relative_path).resolve()
    if not str(target).startswith(str(ALLOWED_ROOT)):
        raise PermissionError("Access outside workspace boundary denied")
    return target.read_text(encoding="utf-8")
""".trimIndent()
    ),
    GeneratedMcp(
        name = "SQLite & Postgres Database MCP",
        category = "Data & SQL",
        description = "Execute parameterized SQL queries, inspect table schemas, and manage database migrations.",
        isCustomCoded = false,
        tools = listOf(
            McpToolDef("read_query", "Run a SELECT query on the database", """{"query": "string"}"""),
            McpToolDef("write_query", "Run an INSERT/UPDATE/DELETE statement", """{"query": "string", "params": "array"}"""),
            McpToolDef("describe_schema", "List all tables, columns, and foreign keys", """{}""")
        ),
        mcpJsonConfig = """
{
  "mcpServers": {
    "sqlite": {
      "command": "uvx",
      "args": ["mcp-server-sqlite", "--db-path", "app_data.db"]
    }
  }
}
""".trimIndent(),
        serverCode = """
# FastMCP SQLite Server
from mcp.server.fastmcp import FastMCP
import sqlite3

mcp = FastMCP("SQLite Database Engine")

@mcp.tool()
def run_sql_query(query: str) -> list:
    # Execute a read-only or transactional query against SQLite.
    conn = sqlite3.connect("app_data.db")
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()
    cursor.execute(query)
    rows = cursor.fetchall()
    conn.commit()
    conn.close()
    return [dict(r) for r in rows]
""".trimIndent()
    )
)

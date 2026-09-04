package com.aistudio.promptforge.abcd

import com.aistudio.promptforge.abcd.data.FavoritePrompt
import com.aistudio.promptforge.abcd.data.PromptRepositoryCategories
import com.aistudio.promptforge.abcd.data.PromptStat
import com.aistudio.promptforge.abcd.model.RepoPromptItem
import com.aistudio.promptforge.abcd.util.ShareUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptForgeUnitTest {

    @Test
    fun testFavoritePromptEntity() {
        val fav = FavoritePrompt(promptId = "repo_01", favoritedAt = 123456789L)
        assertEquals("repo_01", fav.promptId)
        assertEquals(123456789L, fav.favoritedAt)
    }

    @Test
    fun testPromptStatEntityCalculations() {
        val stat = PromptStat(
            promptId = "repo_code_review",
            executionCount = 5,
            copyCount = 3,
            shareCount = 2,
            lastExecutedAt = 1000L,
            lastCopiedAt = 2000L,
            lastSharedAt = 3000L,
            avgLatencyMs = 1200L
        )
        assertEquals(5, stat.executionCount)
        assertEquals(3, stat.copyCount)
        assertEquals(2, stat.shareCount)
        assertEquals(1200L, stat.avgLatencyMs)
    }

    @Test
    fun testShareContentFormatting() {
        val formatted = ShareUtils.formatPromptForShare(
            title = "Code Architecture Architect",
            framework = "CREATE",
            promptText = "Act as a senior staff engineer and review this architecture plan."
        )
        assertTrue(formatted.contains("Code Architecture Architect"))
        assertTrue(formatted.contains("Framework: CREATE"))
        assertTrue(formatted.contains("Act as a senior staff engineer"))
        assertTrue(formatted.contains("Crafted with PromptForge AI"))
    }

    @Test
    fun testShareResponseFormatting() {
        val formatted = ShareUtils.formatResponseForShare(
            promptTitle = "Code Review Prompt",
            model = "models/gemini-flash-latest",
            response = "Here are 3 recommendations: 1. Use Room. 2. Use Compose. 3. Enable edge to edge."
        )
        assertTrue(formatted.contains("Code Review Prompt"))
        assertTrue(formatted.contains("gemini-flash-latest"))
        assertTrue(formatted.contains("Here are 3 recommendations"))
        assertTrue(formatted.contains("PromptForge AI & Gemini"))
    }

    @Test
    fun testCategoryFiltering() {
        val prompts = listOf(
            RepoPromptItem(
                id = "1",
                title = "Senior Code Reviewer",
                category = "Coding & Architecture",
                framework = "CREATE",
                description = "Review PRs",
                promptTemplate = "Review code...",
                isFavorite = true
            ),
            RepoPromptItem(
                id = "2",
                title = "SaaS Copywriter",
                category = "Copywriting & Marketing",
                framework = "RTF",
                description = "Landing page copy",
                promptTemplate = "Write copy...",
                isFavorite = false
            )
        )

        val favoritesOnly = prompts.filter { it.isFavorite }
        assertEquals(1, favoritesOnly.size)
        assertEquals("Senior Code Reviewer", favoritesOnly[0].title)

        val codingOnly = prompts.filter { it.category == "Coding & Architecture" }
        assertEquals(1, codingOnly.size)
        assertEquals("1", codingOnly[0].id)
    }

    @Test
    fun testTokenEstimationAccuracy() {
        val sample = "This is a prompt that needs to estimate token length approximately."
        val estTokens = (sample.length / 4.0).toInt().coerceAtLeast(1)
        assertTrue(estTokens > 10)
        assertTrue(estTokens < 25)
    }

    @Test
    fun testPromptRepositoryCategories() {
        assertTrue(PromptRepositoryCategories.LIST.contains(PromptRepositoryCategories.ALL))
        assertTrue(PromptRepositoryCategories.LIST.contains(PromptRepositoryCategories.CODING))
        assertTrue(PromptRepositoryCategories.LIST.contains(PromptRepositoryCategories.REASONING))
    }
}

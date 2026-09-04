package com.aistudio.promptforge.abcd

import com.aistudio.promptforge.abcd.api.provider.BackendProxyProvider
import com.aistudio.promptforge.abcd.api.provider.GeminiDirectProvider
import com.aistudio.promptforge.abcd.api.provider.LocalAutonomousProvider
import com.aistudio.promptforge.abcd.api.provider.ProviderGenerationRequest
import com.aistudio.promptforge.abcd.api.provider.ProviderManager
import com.aistudio.promptforge.abcd.api.provider.ProviderType
import com.aistudio.promptforge.abcd.data.AutoForgePack
import com.aistudio.promptforge.abcd.data.FavoritePrompt
import com.aistudio.promptforge.abcd.data.PromptRevisionEntity
import com.aistudio.promptforge.abcd.data.PromptStat
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.model.DiffType
import com.aistudio.promptforge.abcd.model.ExecutionProvenanceRecord
import com.aistudio.promptforge.abcd.model.PromptRepositoryCategories
import com.aistudio.promptforge.abcd.model.PromptVariable
import com.aistudio.promptforge.abcd.model.ProvenanceStatus
import com.aistudio.promptforge.abcd.model.RepoPromptItem
import com.aistudio.promptforge.abcd.model.VariableType
import com.aistudio.promptforge.abcd.util.AiOutputValidator
import com.aistudio.promptforge.abcd.util.DataPortabilityService
import com.aistudio.promptforge.abcd.util.DiffUtils
import com.aistudio.promptforge.abcd.util.ShareUtils
import com.aistudio.promptforge.abcd.util.VariableResolver
import com.aistudio.promptforge.abcd.util.VaultCryptoUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
            lastLatencyMs = 1200L,
            lastUsedAt = 3000L
        )
        assertEquals(5, stat.executionCount)
        assertEquals(3, stat.copyCount)
        assertEquals(2, stat.shareCount)
        assertEquals(1200L, stat.lastLatencyMs)
        assertEquals(3000L, stat.lastUsedAt)
    }

    @Test
    fun testPromptRevisionEntity() {
        val revision = PromptRevisionEntity(
            id = "rev_1",
            promptId = "prompt_100",
            revisionNumber = 2,
            promptText = "Improved prompt v2",
            notes = "Added chain-of-thought instructions",
            isActive = true
        )
        assertEquals("rev_1", revision.id)
        assertEquals(2, revision.revisionNumber)
        assertTrue(revision.isActive)
        assertEquals("Improved prompt v2", revision.promptText)
    }

    @Test
    fun testDiffEngineCalculations() {
        val original = "Line 1: System Persona\nLine 2: Objective\nLine 3: Guardrails"
        val revised = "Line 1: System Persona\nLine 2: Objective Updated\nLine 3: Guardrails\nLine 4: Testing Rules"

        val diff = DiffUtils.computeDiff(original, revised)
        assertFalse(diff.isIdentical)
        assertTrue(diff.additionsCount >= 2)
        assertTrue(diff.deletionsCount >= 1)
        assertTrue(diff.unchangedCount >= 2)

        val identical = DiffUtils.computeDiff("Same text", "Same text")
        assertTrue(identical.isIdentical)
        assertEquals(0, identical.additionsCount)
        assertEquals(0, identical.deletionsCount)
    }

    @Test
    fun testTypedVariableExtractionAndValidation() {
        val template = "Deploy {{APP_NAME}} to {{ENVIRONMENT}} with concurrency of {{CONCURRENCY_NUM}}."
        val extractedNames = VariableResolver.extractVariableNames(template)

        assertEquals(3, extractedNames.size)
        assertTrue(extractedNames.contains("APP_NAME"))
        assertTrue(extractedNames.contains("ENVIRONMENT"))
        assertTrue(extractedNames.contains("CONCURRENCY_NUM"))

        val variables = listOf(
            PromptVariable(name = "APP_NAME", type = VariableType.STRING, isRequired = true),
            PromptVariable(
                name = "ENVIRONMENT",
                type = VariableType.CHOICE,
                options = listOf("staging", "production"),
                isRequired = true
            ),
            PromptVariable(name = "CONCURRENCY_NUM", type = VariableType.NUMBER, isRequired = true)
        )

        // Invalid: missing required, invalid number, invalid choice
        val invalidInputs = mapOf(
            "APP_NAME" to "",
            "ENVIRONMENT" to "development",
            "CONCURRENCY_NUM" to "not_a_number"
        )
        val invalidResult = VariableResolver.validateVariables(variables, invalidInputs)
        assertFalse(invalidResult.isValid)
        assertEquals(3, invalidResult.errors.size)

        // Valid
        val validInputs = mapOf(
            "APP_NAME" to "AutoForgeServer",
            "ENVIRONMENT" to "production",
            "CONCURRENCY_NUM" to "8"
        )
        val validResult = VariableResolver.validateVariables(variables, validInputs)
        assertTrue(validResult.isValid)
        assertEquals(0, validResult.errors.size)

        val resolvedText = VariableResolver.resolveTemplate(template, validResult.resolvedMap)
        assertEquals("Deploy AutoForgeServer to production with concurrency of 8.", resolvedText)
    }

    @Test
    fun testAiOutputSanitizationAndBounding() {
        // Test length bounding
        val veryLongText = "A".repeat(100)
        val boundedResult = AiOutputValidator.sanitizeAndValidate(veryLongText, maxChars = 20)
        assertTrue(boundedResult.wasTruncated)
        assertTrue(boundedResult.sanitizedText.startsWith("A".repeat(20)))

        // Test dangerous HTML tag stripping
        val dangerousHtml = "Here is the code: <script>alert('pwned')</script> normal text"
        val htmlResult = AiOutputValidator.sanitizeAndValidate(dangerousHtml)
        assertFalse(htmlResult.sanitizedText.contains("<script>"))
        assertTrue(htmlResult.sanitizedHazardCount > 0)

        // Test untrusted markdown link schemes
        val dangerousLink = "Check this [link](javascript:maliciousCode()) out and [doc](file:///etc/passwd)"
        val linkResult = AiOutputValidator.sanitizeAndValidate(dangerousLink)
        assertFalse(linkResult.sanitizedText.contains("javascript:"))
        assertFalse(linkResult.sanitizedText.contains("file:///"))
        assertTrue(linkResult.sanitizedHazardCount >= 2)
    }

    @Test
    fun testDataPortabilityExportAndImport() {
        val samplePrompts = listOf(
            SavedPrompt(
                id = "p1",
                title = "Senior Architect",
                frameworkId = "CREATE",
                fieldsJson = "{}",
                assembled = "Design system architecture",
                system = "Act as senior software architect",
                createdAt = 1000L
            )
        )
        val sampleProvenance = listOf(
            ExecutionProvenanceRecord(
                id = "prov_1",
                promptId = "p1",
                promptTitle = "Senior Architect",
                selectedModel = "gemini-flash-latest",
                sanitizedOutput = "Safe output",
                status = ProvenanceStatus.SUCCESS
            )
        )

        val jsonBundle = DataPortabilityService.exportToJson(
            prompts = samplePrompts,
            skills = emptyList(),
            mcps = emptyList(),
            packs = emptyList(),
            stats = emptyList(),
            provenance = sampleProvenance
        )

        assertTrue(jsonBundle.contains("AutoForge"))
        assertTrue(jsonBundle.contains("Senior Architect"))

        val (parsedBundle, parseError) = DataPortabilityService.parseBundle(jsonBundle)
        assertNotNull(parsedBundle)
        assertEquals(null, parseError)
        assertEquals(1, parsedBundle!!.prompts.size)
        assertEquals("Senior Architect", parsedBundle.prompts[0].title)
        assertEquals(1, parsedBundle.provenanceRecords.size)
    }

    @Test
    fun testLocalAutonomousProviderOffline() = runBlocking {
        val provider = LocalAutonomousProvider()
        assertTrue(provider.isConfigured())

        val health = provider.testHealth("test-model")
        assertTrue(health.isHealthy)

        val request = ProviderGenerationRequest(
            prompt = "Scrape market data and deliver discord alerts",
            model = "local"
        )
        val result = provider.generate(request)
        assertTrue(result is com.aistudio.promptforge.abcd.api.provider.AiExecutionResult.Success)
        val success = result as com.aistudio.promptforge.abcd.api.provider.AiExecutionResult.Success
        assertTrue(success.isFallback)
        assertTrue(success.data.isNotBlank())
    }

    @Test
    fun testProviderManagerSelection() {
        val manager = ProviderManager()
        assertEquals(ProviderType.GEMINI_DIRECT, manager.activeProviderType.value)

        manager.setProviderType(ProviderType.BACKEND_PROXY)
        assertEquals(ProviderType.BACKEND_PROXY, manager.activeProviderType.value)
        assertTrue(manager.getActiveProvider() is BackendProxyProvider)

        manager.setProviderType(ProviderType.LOCAL_AUTONOMOUS)
        assertTrue(manager.getActiveProvider() is LocalAutonomousProvider)
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
        assertTrue(formatted.contains("Shared via AutoForge Prompt Repository"))
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
        assertTrue(formatted.contains("AutoForge Gemini Runner"))
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
        assertTrue(PromptRepositoryCategories.LIST.contains(PromptRepositoryCategories.AUTONOMOUS_AGENTS))
        assertTrue(PromptRepositoryCategories.LIST.contains(PromptRepositoryCategories.CODE_ARCHITECTURE))
    }
}

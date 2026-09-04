package com.aistudio.promptforge.abcd.util

import android.content.Context
import android.content.Intent
import android.widget.Toast

object ShareUtils {
    /**
     * Launches the Android system share sheet with formatted prompt text.
     */
    fun sharePrompt(
        context: Context,
        title: String,
        framework: String,
        promptText: String
    ) {
        val shareBody = buildString {
            appendLine("=== $title ===")
            if (framework.isNotBlank()) {
                appendLine("Framework: $framework")
            }
            appendLine()
            appendLine(promptText.trim())
            appendLine()
            appendLine("--- Shared via AutoForge Prompt Repository ---")
        }

        shareText(context, subject = title, content = shareBody)
    }

    /**
     * Shares an AI generation response from Gemini.
     */
    fun shareGeminiResponse(
        context: Context,
        promptTitle: String,
        model: String,
        response: String
    ) {
        val shareBody = buildString {
            appendLine("=== Gemini AI Response ===")
            appendLine("Prompt: $promptTitle")
            appendLine("Model: $model")
            appendLine()
            appendLine(response.trim())
            appendLine()
            appendLine("--- Generated via AutoForge Gemini Runner ---")
        }

        shareText(context, subject = "Gemini Response: $promptTitle", content = shareBody)
    }

    /**
     * Generic text sharing via Intent.createChooser.
     */
    fun shareText(context: Context, subject: String, content: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, content)
            }
            val chooser = Intent.createChooser(sendIntent, "Share via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open share sheet: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

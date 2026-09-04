package com.aistudio.promptforge.abcd.util

import com.aistudio.promptforge.abcd.model.DiffLine
import com.aistudio.promptforge.abcd.model.DiffType
import com.aistudio.promptforge.abcd.model.PromptDiffResult

object DiffUtils {

    /**
     * Computes line-by-line diff between original text and revised text.
     * Uses Longest Common Subsequence (LCS) approach for accurate line alignment.
     */
    fun computeDiff(original: String, revised: String): PromptDiffResult {
        val origLines = if (original.isEmpty()) emptyList() else original.lines()
        val revLines = if (revised.isEmpty()) emptyList() else revised.lines()

        val lcs = computeLcs(origLines, revLines)
        val result = mutableListOf<DiffLine>()

        var origIdx = 0
        var revIdx = 0
        var additions = 0
        var deletions = 0
        var unchanged = 0

        for (match in lcs) {
            // Lines deleted from original before match
            while (origIdx < match.first) {
                result.add(
                    DiffLine(
                        originalLineNumber = origIdx + 1,
                        revisedLineNumber = null,
                        text = origLines[origIdx],
                        type = DiffType.REMOVED
                    )
                )
                deletions++
                origIdx++
            }

            // Lines added in revised before match
            while (revIdx < match.second) {
                result.add(
                    DiffLine(
                        originalLineNumber = null,
                        revisedLineNumber = revIdx + 1,
                        text = revLines[revIdx],
                        type = DiffType.ADDED
                    )
                )
                additions++
                revIdx++
            }

            // The matched line
            result.add(
                DiffLine(
                    originalLineNumber = origIdx + 1,
                    revisedLineNumber = revIdx + 1,
                    text = origLines[origIdx],
                    type = DiffType.UNCHANGED
                )
            )
            unchanged++
            origIdx++
            revIdx++
        }

        // Remaining deletions in original
        while (origIdx < origLines.size) {
            result.add(
                DiffLine(
                    originalLineNumber = origIdx + 1,
                    revisedLineNumber = null,
                    text = origLines[origIdx],
                    type = DiffType.REMOVED
                )
            )
            deletions++
            origIdx++
        }

        // Remaining additions in revised
        while (revIdx < revLines.size) {
            result.add(
                DiffLine(
                    originalLineNumber = null,
                    revisedLineNumber = revIdx + 1,
                    text = revLines[revIdx],
                    type = DiffType.ADDED
                )
            )
            additions++
            revIdx++
        }

        return PromptDiffResult(
            lines = result,
            additionsCount = additions,
            deletionsCount = deletions,
            unchangedCount = unchanged
        )
    }

    private fun computeLcs(orig: List<String>, rev: List<String>): List<Pair<Int, Int>> {
        val m = orig.size
        val n = rev.size
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0 until m) {
            for (j in 0 until n) {
                if (orig[i] == rev[j]) {
                    dp[i + 1][j + 1] = dp[i][j] + 1
                } else {
                    dp[i + 1][j + 1] = maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        val matches = mutableListOf<Pair<Int, Int>>()
        var i = m
        var j = n
        while (i > 0 && j > 0) {
            if (orig[i - 1] == rev[j - 1]) {
                matches.add(Pair(i - 1, j - 1))
                i--
                j--
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--
            } else {
                j--
            }
        }

        return matches.reversed()
    }
}

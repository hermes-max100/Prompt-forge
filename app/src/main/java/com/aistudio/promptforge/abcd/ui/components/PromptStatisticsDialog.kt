package com.aistudio.promptforge.abcd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.data.PromptStat
import com.aistudio.promptforge.abcd.model.CURATED_PROMPT_REPOSITORY
import com.aistudio.promptforge.abcd.model.PromptRepositoryCategories
import com.aistudio.promptforge.abcd.model.RepoPromptItem
import com.aistudio.promptforge.abcd.ui.MainViewModel

@Composable
fun PromptStatisticsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onFilterFavorites: () -> Unit
) {
    val allPrompts by viewModel.filteredRepoPrompts.collectAsState()
    val savedPrompts by viewModel.savedPrompts.collectAsState()
    val favoriteIds by viewModel.favoritePromptIds.collectAsState()
    val statsMap by viewModel.promptStatsMap.collectAsState()

    // Aggregate calculations
    val totalCount = CURATED_PROMPT_REPOSITORY.size + savedPrompts.size
    val favoriteCount = favoriteIds.size

    // Estimated total tokens across all prompts
    val totalEstimatedTokens = remember(savedPrompts) {
        val curatedTokens = CURATED_PROMPT_REPOSITORY.sumOf { viewModel.estimateTokens(it.promptTemplate) }
        val savedTokens = savedPrompts.sumOf { viewModel.estimateTokens(it.assembled) }
        curatedTokens + savedTokens
    }

    val totalWords = remember(savedPrompts) {
        val curatedWords = CURATED_PROMPT_REPOSITORY.sumOf {
            it.promptTemplate.split("\\s+".toRegex()).count { w -> w.isNotBlank() }
        }
        val savedWords = savedPrompts.sumOf {
            it.assembled.split("\\s+".toRegex()).count { w -> w.isNotBlank() }
        }
        curatedWords + savedWords
    }

    val avgTokensPerPrompt = if (totalCount > 0) totalEstimatedTokens / totalCount else 0

    // Execution & Activity stats
    val totalExecutions = statsMap.values.sumOf { it.executionCount }
    val totalCopies = statsMap.values.sumOf { it.copyCount }
    val totalShares = statsMap.values.sumOf { it.shareCount }

    val avgLatency = remember(statsMap) {
        val executedStats = statsMap.values.filter { it.lastLatencyMs > 0 }
        if (executedStats.isNotEmpty()) {
            executedStats.map { it.lastLatencyMs }.average().toLong()
        } else 0L
    }

    // Category breakdown
    val categoryCounts = remember(savedPrompts) {
        val map = mutableMapOf<String, Int>()
        CURATED_PROMPT_REPOSITORY.forEach {
            map[it.category] = (map[it.category] ?: 0) + 1
        }
        if (savedPrompts.isNotEmpty()) {
            map[PromptRepositoryCategories.SAVED_CUSTOM] = savedPrompts.size
        }
        map
    }

    // Framework breakdown
    val frameworkCounts = remember(savedPrompts) {
        val map = mutableMapOf<String, Int>()
        CURATED_PROMPT_REPOSITORY.forEach {
            map[it.framework] = (map[it.framework] ?: 0) + 1
        }
        savedPrompts.forEach {
            val fw = it.frameworkId.ifBlank { "Custom" }
            map[fw] = (map[fw] ?: 0) + 1
        }
        map
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prompt_statistics_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Prompt Statistics",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Repository & execution intelligence",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Top High-Level Metric Tiles (2x2 Grid)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        title = "Total Prompts",
                        value = totalCount.toString(),
                        subtitle = "${CURATED_PROMPT_REPOSITORY.size} curated, ${savedPrompts.size} custom",
                        icon = Icons.Filled.AutoAwesome,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "Favorites",
                        value = favoriteCount.toString(),
                        subtitle = if (favoriteCount > 0) "Pinned for quick access" else "Tap heart on any prompt",
                        icon = Icons.Filled.Favorite,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(
                        title = "Est. Total Tokens",
                        value = "~$totalEstimatedTokens",
                        subtitle = "Avg $avgTokensPerPrompt tok/prompt",
                        icon = Icons.Filled.Speed,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "Total Executions",
                        value = totalExecutions.toString(),
                        subtitle = if (avgLatency > 0) "Avg latency ${avgLatency}ms" else "Gemini runs recorded",
                        icon = Icons.Filled.PlayArrow,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                // Activity Counters Row
                Text(
                    "User Activity & Interactions",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActivityCounter(icon = Icons.Filled.PlayArrow, label = "Runs", count = totalExecutions)
                        ActivityCounter(icon = Icons.Filled.ContentCopy, label = "Copies", count = totalCopies)
                        ActivityCounter(icon = Icons.Filled.Share, label = "Shares", count = totalShares)
                        if (avgLatency > 0) {
                            ActivityCounter(icon = Icons.Filled.Timer, label = "Avg Latency", countText = "${avgLatency}ms")
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                // Category Distribution
                Text(
                    "Domain Distribution",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                categoryCounts.entries.sortedByDescending { it.value }.forEach { (category, count) ->
                    val fraction = if (totalCount > 0) count.toFloat() / totalCount else 0f
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                            Text(
                                "$count (${(fraction * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                // Framework Distribution
                Text(
                    "Framework Architecture Distribution",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                frameworkCounts.entries.sortedByDescending { it.value }.take(6).forEach { (fw, count) ->
                    val fraction = if (totalCount > 0) count.toFloat() / totalCount else 0f
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(fw, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                            Text(
                                "$count prompts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                // Recent / Most Active Prompts if any
                val activeStats = statsMap.values.filter { it.executionCount > 0 || it.copyCount > 0 || it.shareCount > 0 }
                    .sortedByDescending { it.executionCount + it.copyCount + it.shareCount }
                    .take(3)

                if (activeStats.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Most Active Prompts",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    activeStats.forEach { stat ->
                        val matchingPrompt = allPrompts.find { it.id == stat.promptId }
                        val promptTitle = matchingPrompt?.title ?: stat.promptId.take(20)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        promptTitle,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                    Text(
                                        "Runs: ${stat.executionCount} • Copies: ${stat.copyCount} • Shares: ${stat.shareCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (stat.lastLatencyMs > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            "${stat.lastLatencyMs}ms",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (favoriteCount > 0) {
                Button(
                    onClick = {
                        onFilterFavorites()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("filter_favorites_from_stats_button")
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View Favorites ($favoriteCount)")
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (favoriteCount > 0) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ActivityCounter(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int = 0,
    countText: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(2.dp))
        Text(
            countText ?: count.toString(),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

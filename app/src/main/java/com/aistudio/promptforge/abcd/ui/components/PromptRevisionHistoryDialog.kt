package com.aistudio.promptforge.abcd.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.data.PromptRevisionEntity
import com.aistudio.promptforge.abcd.model.DiffType
import com.aistudio.promptforge.abcd.model.PromptDiffResult
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.util.DiffUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PromptRevisionHistoryDialog(
    promptId: String,
    promptTitle: String,
    currentActiveText: String,
    viewModel: MainViewModel,
    onRollbackApplied: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val revisionRepo = viewModel.repository.revisionRepository
    val revisions by revisionRepo.getRevisions(promptId).collectAsState(initial = emptyList())

    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    var selectedDiffWithActive by remember { mutableStateOf<Pair<PromptRevisionEntity, PromptDiffResult>?>(null) }

    // If viewing a diff
    if (selectedDiffWithActive != null) {
        val (rev, diffResult) = selectedDiffWithActive!!
        val defaultTextCol = MaterialTheme.colorScheme.onSurface
        AlertDialog(
            onDismissRequest = { selectedDiffWithActive = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CompareArrows, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Diff: Active vs v${rev.revisionNumber}")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("+${diffResult.additionsCount} added", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("-${diffResult.deletionsCount} removed", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    diffResult.lines.forEach { line ->
                        val bgColor = when (line.type) {
                            DiffType.ADDED -> Color(0xFFE8F5E9)
                            DiffType.REMOVED -> Color(0xFFFFEBEE)
                            DiffType.UNCHANGED -> Color.Transparent
                        }
                        val textColor = when (line.type) {
                            DiffType.ADDED -> Color(0xFF2E7D32)
                            DiffType.REMOVED -> Color(0xFFC62828)
                            DiffType.UNCHANGED -> defaultTextCol
                        }
                        val prefix = when (line.type) {
                            DiffType.ADDED -> "+ "
                            DiffType.REMOVED -> "- "
                            DiffType.UNCHANGED -> "  "
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bgColor, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                prefix + line.text,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = textColor
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            revisionRepo.rollbackToRevision(promptId, rev.id)
                            onRollbackApplied(rev.promptText)
                            selectedDiffWithActive = null
                            Toast.makeText(context, "Rolled back to v${rev.revisionNumber}!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    }
                ) {
                    Text("Rollback to v${rev.revisionNumber}")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDiffWithActive = null }) {
                    Text("Close Diff")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Revision History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        promptTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            if (revisions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No revisions saved yet.", color = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    revisionRepo.createRevision(
                                        promptId = promptId,
                                        promptText = currentActiveText,
                                        notes = "Initial baseline version v1"
                                    )
                                    Toast.makeText(context, "Baseline revision saved!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Save Current as Baseline v1")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(revisions, key = { it.id }) { rev ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (rev.isActive)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "v${rev.revisionNumber}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (rev.isActive) {
                                            Spacer(Modifier.width(6.dp))
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("Active", fontSize = 10.sp) },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    labelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }
                                    Text(
                                        dateFormat.format(Date(rev.createdAt)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (rev.notes.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        rev.notes,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            val diff = DiffUtils.computeDiff(currentActiveText, rev.promptText)
                                            selectedDiffWithActive = Pair(rev, diff)
                                        },
                                        modifier = Modifier.testTag("compare_diff_rev_${rev.revisionNumber}")
                                    ) {
                                        Icon(Icons.Filled.CompareArrows, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Diff", fontSize = 12.sp)
                                    }

                                    if (!rev.isActive) {
                                        Spacer(Modifier.width(6.dp))
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    revisionRepo.rollbackToRevision(promptId, rev.id)
                                                    onRollbackApplied(rev.promptText)
                                                    Toast.makeText(context, "Rolled back to v${rev.revisionNumber}!", Toast.LENGTH_SHORT).show()
                                                    onDismiss()
                                                }
                                            },
                                            modifier = Modifier.testTag("rollback_btn_rev_${rev.revisionNumber}")
                                        ) {
                                            Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Rollback", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("revisions_close_button")
            ) {
                Text("Close")
            }
        }
    )
}

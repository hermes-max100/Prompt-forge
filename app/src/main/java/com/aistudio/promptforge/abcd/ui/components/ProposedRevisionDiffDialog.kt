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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.aistudio.promptforge.abcd.model.DiffType
import com.aistudio.promptforge.abcd.util.DiffUtils

@Composable
fun ProposedRevisionDiffDialog(
    promptTitle: String,
    currentText: String,
    proposedText: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    val diffResult = remember(currentText, proposedText) {
        DiffUtils.computeDiff(currentText, proposedText)
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
                        Icons.Filled.CompareArrows,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Proposed Revision Diff",
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
            val defaultTextCol = MaterialTheme.colorScheme.onSurface
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("+${diffResult.additionsCount} added", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("-${diffResult.deletionsCount} removed", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("${diffResult.unchangedCount} unchanged", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
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
                onClick = onAccept,
                modifier = Modifier.testTag("accept_proposed_revision_button")
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Accept & Save Revision")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_proposed_revision_button")
            ) {
                Text("Keep Current")
            }
        }
    )
}

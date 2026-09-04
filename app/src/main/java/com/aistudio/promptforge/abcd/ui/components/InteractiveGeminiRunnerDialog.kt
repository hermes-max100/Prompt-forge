package com.aistudio.promptforge.abcd.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.api.SupportedModels
import com.aistudio.promptforge.abcd.model.RepoPromptItem
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.util.ShareUtils

@Composable
fun InteractiveGeminiRunnerDialog(
    promptItem: RepoPromptItem,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isBusy by viewModel.isExecutingPrompt.collectAsState()
    val executionOutput by viewModel.promptExecutionOutput.collectAsState()
    val metrics by viewModel.promptExecutionMetrics.collectAsState()
    val notice by viewModel.promptExecutionNotice.collectAsState()
    val favoriteIds by viewModel.favoritePromptIds.collectAsState()
    val isFav = favoriteIds.contains(promptItem.id)

    var activePromptText by remember { mutableStateOf(promptItem.promptTemplate) }
    var selectedModel by remember { mutableStateOf(promptItem.recommendedModel) }
    var selectedTemperature by remember { mutableFloatStateOf(0.4f) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = {
            if (!isBusy) {
                viewModel.clearPromptExecution()
                onDismiss()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interactive_gemini_runner_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Run with Gemini",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            promptItem.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.toggleFavoritePrompt(promptItem.id) },
                        modifier = Modifier.size(32.dp).testTag("dialog_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFav) "Favorited" else "Favorite",
                            tint = if (isFav) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            ShareUtils.sharePrompt(
                                context = context,
                                title = promptItem.title,
                                framework = promptItem.framework,
                                promptText = activePromptText
                            )
                            viewModel.recordPromptShare(promptItem.id)
                        },
                        modifier = Modifier.size(32.dp).testTag("dialog_share_prompt_button")
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share Prompt",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(scrollState)
            ) {
                // Model Picker Chips
                Text(
                    "Gemini Model",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SupportedModels.ALL.forEach { model ->
                        val isSelected = model == selectedModel
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedModel = model },
                            label = {
                                Text(
                                    when (model) {
                                        SupportedModels.FLASH_LATEST -> "Flash Latest"
                                        SupportedModels.FLASH_3_5 -> "3.5 Flash"
                                        SupportedModels.PRO_3_1 -> "3.1 Pro"
                                        else -> model
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Temperature presets
                Text(
                    "Temperature: ${"%.1f".format(selectedTemperature)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0.2f to "Precise (0.2)",
                        0.4f to "Balanced (0.4)",
                        0.7f to "Creative (0.7)"
                    ).forEach { (temp, label) ->
                        FilterChip(
                            selected = selectedTemperature == temp,
                            onClick = { selectedTemperature = temp },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Editable Prompt Body
                Text(
                    "Prompt Template / Input",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = activePromptText,
                    onValueChange = { activePromptText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("interactive_prompt_input"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Execution Button
                Button(
                    onClick = {
                        viewModel.executePromptWithGemini(
                            promptText = activePromptText,
                            model = selectedModel,
                            temperature = selectedTemperature,
                            promptId = promptItem.id
                        )
                    },
                    enabled = !isBusy && activePromptText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("execute_gemini_prompt_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Executing with Gemini...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Execute Prompt with Gemini", fontWeight = FontWeight.Bold)
                    }
                }

                // Latency & Metrics banner if available
                if (metrics != null) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(4.dp))
                                Text("${metrics!!.latencyMs} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(4.dp))
                                Text("${metrics!!.promptTokens} / ${metrics!!.outputTokens} tok", fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Speed, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(Modifier.width(4.dp))
                                Text("%.1f tok/s".format(metrics!!.tokensPerSecond), fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Output Result Container
                if (executionOutput.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Gemini Response",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(executionOutput))
                                    viewModel.recordPromptCopy(promptItem.id)
                                    Toast.makeText(context, "Copied Gemini response!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp).testTag("copy_gemini_response_button")
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    ShareUtils.shareGeminiResponse(
                                        context = context,
                                        promptTitle = promptItem.title,
                                        model = selectedModel,
                                        response = executionOutput
                                    )
                                    viewModel.recordPromptShare(promptItem.id)
                                },
                                modifier = Modifier.size(32.dp).testTag("share_gemini_response_button")
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    viewModel.savePromptToVault("Gemini: " + promptItem.title.take(24), executionOutput)
                                    Toast.makeText(context, "Saved to Vault!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp).testTag("save_gemini_response_button")
                            ) {
                                Icon(Icons.Filled.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    SelectionContainer {
                        Text(
                            text = executionOutput,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.clearPromptExecution()
                    onDismiss()
                },
                modifier = Modifier.testTag("close_gemini_runner_button")
            ) {
                Text("Done")
            }
        }
    )
}

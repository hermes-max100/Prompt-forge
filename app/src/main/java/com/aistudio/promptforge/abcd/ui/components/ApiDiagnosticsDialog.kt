package com.aistudio.promptforge.abcd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.api.SupportedModels
import com.aistudio.promptforge.abcd.ui.MainViewModel

@Composable
fun ApiDiagnosticsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val isKeyConfigured = viewModel.isApiKeyConfigured
    val customKeyVal by viewModel.customApiKey.collectAsState()
    var inputKey by remember { mutableStateOf(customKeyVal) }
    var showKeyText by remember { mutableStateOf(false) }
    val selectedModel by viewModel.selectedModel.collectAsState()
    val apiHealth by viewModel.apiHealthStatus.collectAsState()
    val isTesting by viewModel.isTestingApi.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("api_diagnostics_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.NetworkCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Gemini API Service",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Service Connectivity & Diagnostics",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Key status pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isKeyConfigured) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            if (isKeyConfigured) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = if (isKeyConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                if (isKeyConfigured) "API Key Active" else "Offline Fallback Mode",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isKeyConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                if (isKeyConfigured) {
                                    "GEMINI_API_KEY detected in environment."
                                } else {
                                    "No key found. AutoForge uses local high-speed heuristics."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Custom API Key Input
                Text(
                    "Gemini API Key (Runtime Configuration)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    placeholder = { Text("Paste Gemini API Key (e.g. AIzaSy...)") },
                    visualTransformation = if (showKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input"),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        IconButton(onClick = { showKeyText = !showKeyText }) {
                            Icon(
                                if (showKeyText) Icons.Filled.Key else Icons.Filled.Key,
                                contentDescription = if (showKeyText) "Hide key" else "Show key",
                                tint = if (showKeyText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.updateCustomApiKey(inputKey)
                            viewModel.testApiConnection()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_api_key_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Apply & Test", fontSize = 12.sp)
                    }
                    if (inputKey.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                inputKey = ""
                                viewModel.updateCustomApiKey("")
                            },
                            modifier = Modifier.testTag("clear_api_key_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Model Selection
                Text(
                    "Active Model",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SupportedModels.ALL.forEach { model ->
                        val isSelected = model == selectedModel
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedModel(model) },
                            label = {
                                Text(
                                    when (model) {
                                        SupportedModels.FLASH_LATEST -> "Flash Latest"
                                        SupportedModels.FLASH_3_5 -> "3.5 Flash"
                                        SupportedModels.PRO_3_1 -> "3.1 Pro"
                                        else -> model
                                    },
                                    style = MaterialTheme.typography.labelSmall
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

                Spacer(Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(Modifier.height(14.dp))

                // Connection Health Test Result
                Text(
                    "Health Check & Latency",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))

                if (apiHealth != null) {
                    val health = apiHealth!!
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (health.isHealthy) {
                            Color(0xFF1B382B)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (health.isHealthy) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (health.isHealthy) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (health.isHealthy) Color(0xFF66BB6A) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (health.isHealthy) "Healthy" else "Connection Issue",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (health.isHealthy) Color(0xFF81C784) else MaterialTheme.colorScheme.error
                                )
                                if (health.latencyMs > 0) {
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        "${health.latencyMs}ms",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                health.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Text(
                        "Tap 'Test Connection' below to perform a live ping test to Google Generative AI endpoints.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.testApiConnection() },
                    enabled = !isTesting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_api_connection_button")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Pinging API Service...")
                    } else {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Test API Connection")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("api_diagnostics_close_button")
            ) {
                Text("Close")
            }
        }
    )
}

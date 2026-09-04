package com.aistudio.promptforge.abcd.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.api.provider.ProviderHealth
import com.aistudio.promptforge.abcd.api.provider.ProviderType
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager
import kotlinx.coroutines.launch

@Composable
fun ProviderSettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = viewModel.repository
    val providerManager = repository.providerManager
    val coroutineScope = rememberCoroutineScope()

    var selectedProviderType by remember { mutableStateOf(providerManager.activeProviderType.value) }
    var proxyUrl by remember { mutableStateOf(providerManager.backendProxyProvider.getProxyUrl()) }
    var proxyToken by remember { mutableStateOf(providerManager.backendProxyProvider.getProxyAuthToken()) }
    var customApiKey by remember { mutableStateOf(repository.apiService.customApiKey) }

    var isTestingHealth by remember { mutableStateOf(false) }
    var healthResult by remember { mutableStateOf<ProviderHealth?>(null) }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        val tm = viewModel.themeManager ?: remember { ThemeManager(context) }
        ThemeSelectorDialog(
            themeManager = tm,
            onDismiss = { showThemeDialog = false }
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
                        Icons.Filled.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "AI Provider & Gateway",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Configure secure routing or local offline synthesis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Select Generation Engine Strategy:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                // 1. Google Gemini Direct
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedProviderType == ProviderType.GEMINI_DIRECT)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedProviderType = ProviderType.GEMINI_DIRECT }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedProviderType == ProviderType.GEMINI_DIRECT,
                            onClick = { selectedProviderType = ProviderType.GEMINI_DIRECT },
                            modifier = Modifier.testTag("provider_radio_gemini")
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Google Gemini (Direct API)", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Uses system secrets or configured key",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 2. Server-Side Proxy / Gateway
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedProviderType == ProviderType.BACKEND_PROXY)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedProviderType = ProviderType.BACKEND_PROXY }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedProviderType == ProviderType.BACKEND_PROXY,
                            onClick = { selectedProviderType = ProviderType.BACKEND_PROXY },
                            modifier = Modifier.testTag("provider_radio_proxy")
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.Router, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Server-Side Proxy / Backend", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Prevents client key extraction via proxy gateway",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 3. Local Autonomous Engine
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedProviderType == ProviderType.LOCAL_AUTONOMOUS)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedProviderType = ProviderType.LOCAL_AUTONOMOUS }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedProviderType == ProviderType.LOCAL_AUTONOMOUS,
                            onClick = { selectedProviderType = ProviderType.LOCAL_AUTONOMOUS },
                            modifier = Modifier.testTag("provider_radio_local")
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.Laptop, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Local Autonomous Engine", fontWeight = FontWeight.SemiBold)
                            Text(
                                "100% offline, zero network or API credentials required",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Contextual configuration fields based on selected provider
                if (selectedProviderType == ProviderType.BACKEND_PROXY) {
                    Text("Backend Proxy Configuration:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = proxyUrl,
                        onValueChange = { proxyUrl = it },
                        label = { Text("Proxy Endpoint URL") },
                        placeholder = { Text("https://my-backend-proxy.com/v1/generate") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("provider_proxy_url_input")
                    )
                    OutlinedTextField(
                        value = proxyToken,
                        onValueChange = { proxyToken = it },
                        label = { Text("Session / Bearer Token (Optional)") },
                        placeholder = { Text("Bearer token for server auth") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("provider_proxy_token_input")
                    )
                } else if (selectedProviderType == ProviderType.GEMINI_DIRECT) {
                    Text("Direct Gemini API Configuration:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = customApiKey,
                        onValueChange = { customApiKey = it },
                        label = { Text("Custom Gemini API Key (Optional)") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("provider_api_key_input")
                    )
                    Text(
                        "Default is loaded securely from the AI Studio Secrets panel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Health Status feedback
                if (healthResult != null) {
                    val isOk = healthResult!!.isHealthy
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOk) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isOk) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = null,
                                tint = if (isOk) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                healthResult!!.message,
                                fontSize = 12.sp,
                                color = if (isOk) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Workspace Appearance Theme Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showThemeDialog = true }
                        .testTag("provider_open_theme_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Workspace Theme & Appearance",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Dark, Light, Neon, Cyberpunk, System",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Test Connection Button
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            isTestingHealth = true
                            healthResult = null
                            providerManager.setProviderType(selectedProviderType)
                            if (selectedProviderType == ProviderType.BACKEND_PROXY) {
                                providerManager.configureProxy(proxyUrl, proxyToken)
                            } else if (selectedProviderType == ProviderType.GEMINI_DIRECT) {
                                repository.setCustomApiKey(customApiKey)
                            }
                            val provider = providerManager.getActiveProvider()
                            val health = provider.testHealth("models/gemini-flash-latest")
                            healthResult = health
                            isTestingHealth = false
                        }
                    },
                    enabled = !isTestingHealth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("provider_test_connection_button")
                ) {
                    if (isTestingHealth) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Verifying Connection...")
                    } else {
                        Text("Test Provider Health")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    providerManager.setProviderType(selectedProviderType)
                    if (selectedProviderType == ProviderType.BACKEND_PROXY) {
                        providerManager.configureProxy(proxyUrl, proxyToken)
                    } else if (selectedProviderType == ProviderType.GEMINI_DIRECT) {
                        repository.setCustomApiKey(customApiKey)
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("provider_save_settings_button")
            ) {
                Text("Apply & Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("provider_cancel_settings_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

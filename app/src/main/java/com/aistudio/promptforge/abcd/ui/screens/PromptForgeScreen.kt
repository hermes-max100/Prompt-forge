package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudio.promptforge.abcd.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptForgeScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val goalText by viewModel.promptForgeGoal.collectAsState()
    val promptResult by viewModel.prompt10OutOf10.collectAsState()
    val isBusy by viewModel.isPromptBusy.collectAsState()
    val metrics by viewModel.promptMetrics.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val frameworks = listOf("Auto-Agent", "GEPA Optimization", "CO-STAR", "CRAFT", "Few-Shot Chain")
    var selectedFramework by remember { mutableStateOf(frameworks[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Prompt Forge",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Intent to 10/10 Production Prompt",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (promptResult.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.savePromptToVault("10/10 Prompt: " + goalText.take(30), promptResult)
                                Toast.makeText(context, "Saved Prompt to Vault!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("save_prompt_vault_button")
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = "Save Prompt")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Goal or Intent to Forge",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = goalText,
                            onValueChange = { viewModel.setPromptForgeGoal(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("prompt_forge_input"),
                            placeholder = { Text("Enter a broad task, query, or draft prompt to refine to 10/10...") },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isBusy
                        )
                        Spacer(Modifier.height(10.dp))

                        Text(
                            "Optimization Framework:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(frameworks) { fw ->
                                FilterChip(
                                    selected = selectedFramework == fw,
                                    onClick = { selectedFramework = fw },
                                    label = { Text(fw, fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.forge10OutOf10Prompt(goalText) },
                            enabled = !isBusy && goalText.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("forge_10_out_of_10_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Forging 10/10 Prompt...")
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Synthesize 10/10 Production Prompt", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Real-time Metrics Card
            if (metrics != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetricItem(
                                label = "Latency",
                                value = "${metrics!!.latencyMs} ms",
                                icon = Icons.Filled.Timer
                            )
                            MetricItem(
                                label = "Tokens In/Out",
                                value = "${metrics!!.promptTokens} / ${metrics!!.outputTokens}",
                                icon = Icons.Filled.Psychology
                            )
                            MetricItem(
                                label = "Throughput",
                                value = "%.1f tok/s".format(metrics!!.tokensPerSecond),
                                icon = Icons.Filled.Speed
                            )
                        }
                    }
                }
            }

            // Generated Output Card
            if (promptResult.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "10/10 Production Prompt",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Row {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(promptResult))
                                            Toast.makeText(context, "Copied prompt to clipboard!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Prompt")
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.savePromptToVault("10/10 Prompt: " + goalText.take(30), promptResult)
                                            Toast.makeText(context, "Saved to Vault!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Filled.Save, contentDescription = "Save Prompt")
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            SelectionContainer {
                                Text(
                                    text = promptResult,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            // Action: Forward to Skill Forge
                            Button(
                                onClick = {
                                    viewModel.setSkillForgeQuery(goalText)
                                    viewModel.scourAndCodeSkills(goalText)
                                    navController.navigate("skill_forge")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Transfer & Forge Skills in Skill Forge")
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
    }
}

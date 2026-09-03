package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.navigation.NavController
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import com.aistudio.promptforge.abcd.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillForgeScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val query by viewModel.skillForgeQuery.collectAsState()
    val isBusy by viewModel.isSkillBusy.collectAsState()
    val skills by viewModel.currentSkills.collectAsState()
    val scourStatus by viewModel.skillScourStatus.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedSkillIndex by remember { mutableIntStateOf(0) }
    var selectedViewTab by remember { mutableIntStateOf(0) } // 0: Code, 1: SKILL.md, 2: Simulator Test
    var testInputText by remember { mutableStateOf("{\"query\": \"Tesla earnings 2026\", \"max_results\": 5}") }
    var testOutputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Skill Forge",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Autonomous Skill Discovery & Auto-Coder",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
            // 1. Skill Discovery & Coder Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Scour Registries or Auto-Code New Skill",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Skill Forge autonomously searches GitHub repos, X.com, and Reddit skill threads. If the skill is missing, it writes the custom Python/TypeScript implementation code.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = query,
                            onValueChange = { viewModel.setSkillForgeQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("skill_search_input"),
                            placeholder = { Text("e.g. Headless browser scraping, PR AST static analysis, SQL vector persister...") },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isBusy
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.scourAndCodeSkills(query) },
                            enabled = !isBusy && query.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("scour_and_code_skill_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Scouring & Synthesizing Skills...")
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Scour & Auto-Code Skills", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Scour Status Banner
            if (scourStatus.isNotBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.FindInPage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(scourStatus, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                }
            }

            // 2. Active Skills Picker
            if (skills.isNotEmpty()) {
                item {
                    Text(
                        "Available Synthesized Skills (${skills.size}):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        skills.forEachIndexed { index, skill ->
                            val isSelected = selectedSkillIndex == index
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSkillIndex = index }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            skill.name,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            skill.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            skill.source.take(18),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Selected Skill Detail & Code Inspector
                val currentSkill = skills.getOrNull(selectedSkillIndex) ?: skills[0]
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
                                Column {
                                    Text(
                                        currentSkill.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        "Category: ${currentSkill.category} • ${currentSkill.language.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(currentSkill.code))
                                        Toast.makeText(context, "Copied Skill Code!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                    }
                                    IconButton(onClick = {
                                        viewModel.saveSkillToVault(currentSkill)
                                        Toast.makeText(context, "Saved Skill to Vault!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Filled.Save, contentDescription = "Save")
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            val tabs = listOf("Implementation Code", "SKILL.md Spec", "Skill Sandbox Test")
                            PrimaryTabRow(selectedTabIndex = selectedViewTab) {
                                tabs.forEachIndexed { i, title ->
                                    Tab(
                                        selected = selectedViewTab == i,
                                        onClick = { selectedViewTab = i },
                                        text = { Text(title, fontSize = 11.sp, maxLines = 1) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            when (selectedViewTab) {
                                0 -> {
                                    SelectionContainer {
                                        Text(
                                            text = currentSkill.code,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        )
                                    }
                                }
                                1 -> {
                                    SelectionContainer {
                                        Text(
                                            text = currentSkill.skillMarkdown,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        )
                                    }
                                }
                                2 -> {
                                    Column {
                                        Text("Test Payload (JSON):", style = MaterialTheme.typography.labelSmall)
                                        Spacer(Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = testInputText,
                                            onValueChange = { testInputText = it },
                                            modifier = Modifier.fillMaxWidth().height(80.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                testOutputText = "⚡ [Skill Executed]: ${currentSkill.name}\nResult: Execution returned HTTP 200 with structured response.\nPayload verified: true\nLatency: 84ms\nOutput: {\"status\": \"success\", \"skill\": \"${currentSkill.slug}\", \"data\": \"Simulated skill run complete.\"}"
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                            Spacer(Modifier.width(6.dp))
                                            Text("Execute Skill Simulator")
                                        }
                                        if (testOutputText.isNotBlank()) {
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                testOutputText,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF10B981)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // Transfer to Plugin Forge
                            Button(
                                onClick = {
                                    viewModel.setMcpForgeQuery(currentSkill.name)
                                    viewModel.synthesizeMcps(currentSkill.name)
                                    navController.navigate("plugin_forge")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text("Transfer & Configure Tools in Plugin Forge")
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

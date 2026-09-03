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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudio.promptforge.abcd.data.AutoForgePack
import com.aistudio.promptforge.abcd.data.SavedMcp
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.data.SavedSkill
import com.aistudio.promptforge.abcd.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val savedPacks by viewModel.savedPacks.collectAsState()
    val savedSkills by viewModel.savedSkills.collectAsState()
    val savedMcps by viewModel.savedMcps.collectAsState()
    val savedPrompts by viewModel.savedPrompts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Goal Packs, 1: Prompts, 2: Skills, 3: MCPs
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var packDetailToView by remember { mutableStateOf<AutoForgePack?>(null) }

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
                                Icons.Filled.Inventory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Agent Vault",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Saved Goal Packs, Prompts, Skills & MCPs",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val tabs = listOf(
                "Goal Packs (${savedPacks.size})",
                "Prompts (${savedPrompts.size})",
                "Skills (${savedSkills.size})",
                "MCPs (${savedMcps.size})"
            )
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title, fontSize = 11.sp, maxLines = 1) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                when (selectedTab) {
                    0 -> { // Goal Packs
                        if (savedPacks.isEmpty()) {
                            item {
                                EmptyVaultPlaceholder(
                                    title = "No Saved Goal Packs",
                                    subtitle = "Run AutoForge Engine to create and save complete autonomous goal packages."
                                )
                            }
                        } else {
                            items(savedPacks) { pack ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Filled.FlashOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    pack.goalTitle,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1
                                                )
                                            }
                                            Row {
                                                IconButton(onClick = {
                                                    viewModel.loadPackIntoEngine(pack)
                                                    navController.navigate("engine")
                                                    Toast.makeText(context, "Loaded into Engine!", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Filled.FolderOpen, contentDescription = "Load")
                                                }
                                                IconButton(onClick = {
                                                    viewModel.deletePack(pack.id)
                                                    Toast.makeText(context, "Pack deleted", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                                }
                                            }
                                        }
                                        Text(
                                            pack.goalInput,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    "Execution: ${pack.executionLatencyMs}ms",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                                )
                                            }
                                            Button(
                                                onClick = { packDetailToView = pack },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("View Spec", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // Prompts
                        if (savedPrompts.isEmpty()) {
                            item {
                                EmptyVaultPlaceholder(
                                    title = "No Saved Prompts",
                                    subtitle = "Forge 10/10 prompts in Prompt Forge and save them here."
                                )
                            }
                        } else {
                            items(savedPrompts) { prompt ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(prompt.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                            }
                                            Row {
                                                IconButton(onClick = {
                                                    clipboardManager.setText(AnnotatedString(prompt.assembled))
                                                    Toast.makeText(context, "Prompt copied!", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                                }
                                                IconButton(onClick = { viewModel.deleteSavedPrompt(prompt.id) }) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                                }
                                            }
                                        }
                                        Text(
                                            prompt.assembled,
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                            maxLines = 3,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // Skills
                        if (savedSkills.isEmpty()) {
                            item {
                                EmptyVaultPlaceholder(
                                    title = "No Saved Custom Skills",
                                    subtitle = "Scour and code custom skills in Skill Forge and save them here."
                                )
                            }
                        } else {
                            items(savedSkills) { skill ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(skill.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                            }
                                            Row {
                                                IconButton(onClick = {
                                                    clipboardManager.setText(AnnotatedString(skill.implementationCode))
                                                    Toast.makeText(context, "Skill code copied!", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                                }
                                                IconButton(onClick = { viewModel.deleteSavedSkill(skill.id) }) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                                }
                                            }
                                        }
                                        Text(skill.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(6.dp))
                                        Text("Triggers: ${skill.trigger}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary))
                                    }
                                }
                            }
                        }
                    }
                    3 -> { // MCPs
                        if (savedMcps.isEmpty()) {
                            item {
                                EmptyVaultPlaceholder(
                                    title = "No Saved MCPs",
                                    subtitle = "Configure MCP servers and FastMCP tools in Plugin Forge and save them here."
                                )
                            }
                        } else {
                            items(savedMcps) { mcp ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(mcp.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                            }
                                            Row {
                                                IconButton(onClick = {
                                                    clipboardManager.setText(AnnotatedString(mcp.mcpJsonConfig))
                                                    Toast.makeText(context, "MCP config copied!", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                                }
                                                IconButton(onClick = { viewModel.deleteSavedMcp(mcp.id) }) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                                }
                                            }
                                        }
                                        Text(mcp.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(6.dp))
                                        Text("${mcp.toolsCount} Tools Configured", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.tertiary))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (packDetailToView != null) {
        val pack = packDetailToView!!
        AlertDialog(
            onDismissRequest = { packDetailToView = null },
            title = { Text(pack.goalTitle, fontWeight = FontWeight.Bold) },
            text = {
                SelectionContainer {
                    LazyColumn(modifier = Modifier.height(350.dp)) {
                        item {
                            Text(
                                text = pack.fullSpecMarkdown,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    clipboardManager.setText(AnnotatedString(pack.fullSpecMarkdown))
                    Toast.makeText(context, "Copied Spec to Clipboard!", Toast.LENGTH_SHORT).show()
                    packDetailToView = null
                }) {
                    Text("Copy Spec")
                }
            },
            dismissButton = {
                TextButton(onClick = { packDetailToView = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun EmptyVaultPlaceholder(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Inventory,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

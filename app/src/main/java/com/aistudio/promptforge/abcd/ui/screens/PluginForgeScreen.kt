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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.aistudio.promptforge.abcd.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginForgeScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val query by viewModel.mcpForgeQuery.collectAsState()
    val isBusy by viewModel.isMcpBusy.collectAsState()
    val mcps by viewModel.currentMcps.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedMcpIndex by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: MCP Config JSON, 1: FastMCP Server Code, 2: Tool Schemas

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Extension,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Plugin Forge",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "MCP (Model Context Protocol) & Tool Engine",
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
            // 1. Tool / MCP Query Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Build, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Discover MCPs or Synthesize Custom FastMCP Tool",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Plugin Forge finds standard Model Context Protocol servers or autonomously codes Python/TypeScript FastMCP servers with typed parameters and tool handlers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = query,
                            onValueChange = { viewModel.setMcpForgeQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("mcp_search_input"),
                            placeholder = { Text("e.g. SQLite database queries, Discord Webhook dispatcher, GitHub PR reviewer...") },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isBusy
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.synthesizeMcps(query) },
                            enabled = !isBusy && query.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("synthesize_mcp_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Forging MCP Tools...")
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Synthesize MCPs & Tools", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Active MCPs List
            if (mcps.isNotEmpty()) {
                item {
                    Text(
                        "Configured MCP Servers (${mcps.size}):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        mcps.forEachIndexed { index, mcp ->
                            val isSelected = selectedMcpIndex == index
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMcpIndex = index }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
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
                                            mcp.name,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            "${mcp.tools.size} Tools • ${mcp.description}",
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
                                            if (mcp.isCustomCoded) "Auto-Coded" else "Standard MCP",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Selected MCP Detail & Server Inspector
                val currentMcp = mcps.getOrNull(selectedMcpIndex) ?: mcps[0]
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
                                        currentMcp.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        "Category: ${currentMcp.category} • ${currentMcp.tools.size} tool handlers",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(currentMcp.mcpJsonConfig))
                                        Toast.makeText(context, "Copied MCP Config JSON!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Config")
                                    }
                                    IconButton(onClick = {
                                        viewModel.saveMcpToVault(currentMcp)
                                        Toast.makeText(context, "Saved MCP to Vault!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Filled.Save, contentDescription = "Save MCP")
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            val tabs = listOf("MCP Config JSON", "FastMCP Server Code", "Tool Endpoints (${currentMcp.tools.size})")
                            PrimaryTabRow(selectedTabIndex = selectedTab) {
                                tabs.forEachIndexed { i, title ->
                                    Tab(
                                        selected = selectedTab == i,
                                        onClick = { selectedTab = i },
                                        text = { Text(title, fontSize = 11.sp, maxLines = 1) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            when (selectedTab) {
                                0 -> {
                                    SelectionContainer {
                                        Text(
                                            text = currentMcp.mcpJsonConfig,
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
                                            text = currentMcp.serverCode,
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
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        currentMcp.tools.forEach { tool ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.DataObject, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            "@tool: ${tool.name}",
                                                            style = MaterialTheme.typography.titleSmall.copy(
                                                                fontFamily = FontFamily.Monospace,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        )
                                                    }
                                                    Text(tool.description, style = MaterialTheme.typography.bodySmall)
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        "Parameters: ${tool.parametersJson}",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontFamily = FontFamily.Monospace,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

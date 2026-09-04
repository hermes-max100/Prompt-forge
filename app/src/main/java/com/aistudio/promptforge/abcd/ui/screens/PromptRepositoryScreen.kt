package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudio.promptforge.abcd.api.SupportedModels
import com.aistudio.promptforge.abcd.data.PromptStat
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.model.PromptRepositoryCategories
import com.aistudio.promptforge.abcd.model.RepoPromptItem
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.components.ApiDiagnosticsDialog
import com.aistudio.promptforge.abcd.ui.components.ErrorBanner
import com.aistudio.promptforge.abcd.ui.components.InteractiveGeminiRunnerDialog
import com.aistudio.promptforge.abcd.ui.components.PromptStatisticsDialog
import com.aistudio.promptforge.abcd.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptRepositoryScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val searchQuery by viewModel.repoSearchQuery.collectAsState()
    val selectedCategory by viewModel.repoSelectedCategory.collectAsState()
    val selectedModelFilter by viewModel.repoSelectedModelFilter.collectAsState()
    val filteredPrompts by viewModel.filteredRepoPrompts.collectAsState()
    val currentError by viewModel.currentError.collectAsState()
    val favoriteIds by viewModel.favoritePromptIds.collectAsState()
    val statsMap by viewModel.promptStatsMap.collectAsState()
    val isKeyConfigured = viewModel.isApiKeyConfigured

    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showStatisticsDialog by remember { mutableStateOf(false) }
    var showCreateCustomDialog by remember { mutableStateOf(false) }
    var runnerPromptItem by remember { mutableStateOf<RepoPromptItem?>(null) }

    // Dialog for API diagnostics
    if (showDiagnosticsDialog) {
        ApiDiagnosticsDialog(
            viewModel = viewModel,
            onDismiss = { showDiagnosticsDialog = false }
        )
    }

    // Dialog for Prompt Repository Statistics & Intelligence
    if (showStatisticsDialog) {
        PromptStatisticsDialog(
            viewModel = viewModel,
            onDismiss = { showStatisticsDialog = false },
            onFilterFavorites = {
                viewModel.setRepoSelectedCategory("Favorites")
            }
        )
    }

    // Dialog for Interactive Gemini execution
    if (runnerPromptItem != null) {
        InteractiveGeminiRunnerDialog(
            promptItem = runnerPromptItem!!,
            viewModel = viewModel,
            onDismiss = { runnerPromptItem = null }
        )
    }

    // Dialog to create custom repository prompt
    if (showCreateCustomDialog) {
        CreateCustomPromptDialog(
            onDismiss = { showCreateCustomDialog = false },
            onSave = { title, category, framework, templateText ->
                viewModel.createCustomRepoPrompt(title, category, framework, templateText)
                showCreateCustomDialog = false
                Toast.makeText(context, "Saved custom prompt to repository!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Prompt Repository",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    "${filteredPrompts.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Curated & Custom Production Templates",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("repo_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Statistics & Analytics Dialog
                    IconButton(
                        onClick = { showStatisticsDialog = true },
                        modifier = Modifier.testTag("repo_statistics_button")
                    ) {
                        Icon(
                            Icons.Filled.Analytics,
                            contentDescription = "Prompt Statistics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // API Status Badge
                    IconButton(
                        onClick = { showDiagnosticsDialog = true },
                        modifier = Modifier.testTag("repo_api_status_button")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = if (isKeyConfigured) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                )
                            }
                        ) {
                            Icon(
                                if (isKeyConfigured) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                                contentDescription = "Gemini API Status",
                                tint = if (isKeyConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Add Custom Prompt Button
                    IconButton(
                        onClick = { showCreateCustomDialog = true },
                        modifier = Modifier.testTag("repo_add_custom_prompt_button")
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add Custom Prompt",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Global Error Banner
            if (currentError != null) {
                ErrorBanner(
                    error = currentError,
                    onRetry = { viewModel.retryLastAction() },
                    onDismiss = { viewModel.clearError() }
                )
            }

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setRepoSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repo_search_input"),
                    placeholder = { Text("Search by name, tag, framework, or keywords...") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setRepoSearchQuery("") },
                                modifier = Modifier.testTag("repo_clear_search_button")
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                // "Favorites" pill
                item {
                    val favCount = favoriteIds.size
                    val isFavSelected = selectedCategory == "Favorites"
                    FilterChip(
                        selected = isFavSelected,
                        onClick = {
                            viewModel.setRepoSelectedCategory(
                                if (isFavSelected) PromptRepositoryCategories.ALL else "Favorites"
                            )
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isFavSelected) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavSelected) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (favCount > 0) "Favorites ($favCount)" else "Favorites",
                                    fontSize = 12.sp,
                                    fontWeight = if (isFavSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.16f),
                            selectedLabelColor = Color(0xFFEF4444)
                        ),
                        modifier = Modifier.testTag("repo_category_favorites")
                    )
                }

                items(PromptRepositoryCategories.LIST) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setRepoSelectedCategory(category) },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("repo_category_${category.replace(" ", "_").lowercase()}")
                    )
                }
            }

            // Model Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredPrompts.size} prompts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", SupportedModels.FLASH_LATEST, SupportedModels.PRO_3_1).forEach { model ->
                        val isSelected = selectedModelFilter == model
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setRepoSelectedModelFilter(model) }
                        ) {
                            Text(
                                text = when (model) {
                                    SupportedModels.FLASH_LATEST -> "Flash"
                                    SupportedModels.PRO_3_1 -> "Pro"
                                    else -> "All Models"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Quick Stats Summary Bar
            val totalTokens = remember(filteredPrompts) {
                filteredPrompts.sumOf { viewModel.estimateTokens(it.promptTemplate) }
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showStatisticsDialog = true }
                    .testTag("repo_quick_stats_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            "${filteredPrompts.size} Templates",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "•  ~$totalTokens Tokens",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (favoriteIds.isNotEmpty()) {
                            Text(
                                "•  ${favoriteIds.size} Favorites",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Intelligence",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Prompts List
            if (filteredPrompts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "No Prompts Found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (searchQuery.isNotBlank()) "No prompts match \"$searchQuery\"."
                            else "No prompts match the selected category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.setRepoSearchQuery("")
                                viewModel.setRepoSelectedCategory(PromptRepositoryCategories.ALL)
                                viewModel.setRepoSelectedModelFilter("All")
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPrompts, key = { it.id }) { promptItem ->
                        RepoPromptCard(
                            prompt = promptItem,
                            stat = statsMap[promptItem.id],
                            onRun = { runnerPromptItem = promptItem },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(promptItem.promptTemplate))
                                viewModel.recordPromptCopy(promptItem.id)
                                Toast.makeText(context, "Copied \"${promptItem.title.take(24)}\"", Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                ShareUtils.sharePrompt(
                                    context = context,
                                    title = promptItem.title,
                                    framework = promptItem.framework,
                                    promptText = promptItem.promptTemplate
                                )
                                viewModel.recordPromptShare(promptItem.id)
                            },
                            onLoadIntoForge = {
                                viewModel.setPromptForgeGoal(promptItem.title + "\n\n" + promptItem.promptTemplate)
                                navController.navigate("prompt_forge")
                                Toast.makeText(context, "Loaded into Prompt Forge!", Toast.LENGTH_SHORT).show()
                            },
                            onToggleFavorite = { viewModel.toggleFavoritePrompt(promptItem.id) },
                            onDelete = if (promptItem.isCustom) {
                                { viewModel.deleteSavedPrompt(promptItem.id) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RepoPromptCard(
    prompt: RepoPromptItem,
    stat: PromptStat? = null,
    onRun: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onLoadIntoForge: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: (() -> Unit)?
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("repo_prompt_card_${prompt.id.take(12)}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category & Framework Badges + Heart + Share + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = prompt.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = prompt.framework,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp).testTag("fav_btn_${prompt.id.take(8)}")
                    ) {
                        Icon(
                            if (prompt.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (prompt.isFavorite) "Favorited" else "Favorite",
                            tint = if (prompt.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(32.dp).testTag("share_icon_btn_${prompt.id.take(8)}")
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share Prompt",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).testTag("del_btn_${prompt.id.take(8)}")
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Title & Description
            Text(
                text = prompt.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = prompt.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Statistics & Metrics Badge Row
            val wordCount = remember(prompt.promptTemplate) {
                prompt.promptTemplate.split("\\s+".toRegex()).count { it.isNotBlank() }
            }
            val tokenEst = remember(prompt.promptTemplate) {
                (prompt.promptTemplate.length / 4.0).toInt().coerceAtLeast(1)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "~$tokenEst tokens • $wordCount words",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (stat != null && (stat.executionCount > 0 || stat.copyCount > 0 || stat.shareCount > 0)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "${stat.executionCount} runs • ${stat.copyCount} copies • ${stat.shareCount} shares",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Tags
            if (prompt.tags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(prompt.tags) { tag ->
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Expandable Template Preview
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Prompt Template",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    SelectionContainer {
                        Text(
                            text = prompt.promptTemplate,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            maxLines = if (isExpanded) 100 else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Run with Gemini
                Button(
                    onClick = onRun,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                        .testTag("run_prompt_${prompt.id.take(8)}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Run Gemini", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Copy
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                        .testTag("copy_prompt_${prompt.id.take(8)}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Copy", fontSize = 11.sp)
                }

                // Share
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                        .testTag("share_prompt_${prompt.id.take(8)}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Share", fontSize = 11.sp)
                }

                // Load into Forge
                OutlinedButton(
                    onClick = onLoadIntoForge,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                        .testTag("forge_prompt_${prompt.id.take(8)}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Forge", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CreateCustomPromptDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, framework: String, templateText: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PromptRepositoryCategories.AUTONOMOUS_AGENTS) }
    var framework by remember { mutableStateOf("Custom") }
    var templateText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("create_custom_prompt_dialog"),
        title = {
            Text(
                "Create Custom Prompt",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Prompt Title") },
                    placeholder = { Text("e.g. Master Code Refactorer") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_prompt_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(Modifier.height(10.dp))

                Text("Category:", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(PromptRepositoryCategories.LIST.filter { it != PromptRepositoryCategories.ALL && it != PromptRepositoryCategories.SAVED_CUSTOM }) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = framework,
                    onValueChange = { framework = it },
                    label = { Text("Framework / Strategy") },
                    placeholder = { Text("e.g. CoT, ReAct, Role-Task") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_prompt_framework_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = templateText,
                    onValueChange = { templateText = it },
                    label = { Text("Prompt Content / Template") },
                    placeholder = { Text("Enter prompt system instructions, variables like {{GOAL}}, and rules...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("custom_prompt_body_input"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (templateText.isNotBlank()) {
                        onSave(title, selectedCategory, framework, templateText)
                    }
                },
                enabled = templateText.isNotBlank(),
                modifier = Modifier.testTag("save_custom_prompt_button")
            ) {
                Text("Save to Repository")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

package com.aistudio.promptforge.abcd.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryScreen(viewModel: MainViewModel, navController: NavController) {
    val savedPrompts by viewModel.savedPrompts.collectAsState()
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Library", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Drafts and versions", style = MaterialTheme.typography.headlineMedium)
        Text("Load previous prompts back into the composer.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (savedPrompts.isEmpty()) {
            Text("No saved prompts yet.", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(savedPrompts) { item ->
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        viewModel.loadSaved(item)
                        navController.navigate(Screen.Compose.route)
                    }) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text("${item.frameworkId.uppercase()} • ${dateFormat.format(Date(item.createdAt))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(item.assembled, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                            }
                            IconButton(onClick = { viewModel.deleteSaved(item.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

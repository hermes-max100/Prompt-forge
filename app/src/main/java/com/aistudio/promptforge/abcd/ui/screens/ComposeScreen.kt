package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aistudio.promptforge.abcd.model.FRAMEWORKS
import com.aistudio.promptforge.abcd.model.getFramework
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.Screen
import kotlinx.coroutines.launch

@Composable
fun ComposeScreen(viewModel: MainViewModel, navController: NavController) {
    val frameworkId by viewModel.frameworkId.collectAsState()
    val fields by viewModel.fields.collectAsState()
    val fw = getFramework(frameworkId)
    val assembled = viewModel.assembled()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isBusy by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Composer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Build a prompt that holds still", style = MaterialTheme.typography.headlineMedium)
        Text("Pick a framework, fill the slots, then run it. Structure beats adjectives.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(16.dp))
        // Framework Selector
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FRAMEWORKS.forEach { f ->
                val selected = f.id == frameworkId
                Card(
                    modifier = Modifier.weight(1f).clickable { viewModel.setFramework(f.id) },
                    colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(f.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(f.tag, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(fw.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        fw.fields.forEach { field ->
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(field.label, style = MaterialTheme.typography.labelMedium)
                    Text(field.hint, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(
                    value = fields[field.key] ?: "",
                    onValueChange = { viewModel.setField(field.key, it) },
                    placeholder = { Text(field.placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (field.multiline) 3 else 1
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Assembled Card
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("ASSEMBLED", style = MaterialTheme.typography.labelSmall)
                    Badge { Text(fw.name) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = assembled.ifBlank { "Fields will compile here." },
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { 
                        if (assembled.isNotBlank()) {
                            clipboardManager.setText(AnnotatedString(assembled))
                            Toast.makeText(context, "Copied assembled prompt", Toast.LENGTH_SHORT).show()
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Text("Copy")
                    }
                    Button(onClick = { 
                        if (assembled.isNotBlank()) {
                            viewModel.saveCurrent(fw.name + " draft")
                            Toast.makeText(context, "Saved to library", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Fill a few fields first", Toast.LENGTH_SHORT).show()
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Text("Save")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { 
                        if (assembled.isNotBlank()) {
                            isBusy = true
                            coroutineScope.launch {
                                val res = viewModel.improvePrompt(assembled)
                                isBusy = false
                                when (res) {
                                    is com.aistudio.promptforge.abcd.data.AiResult.Success -> {
                                        viewModel.setFramework("freeform")
                                        viewModel.setField("body", res.data)
                                        Toast.makeText(context, "Rewritten into Freeform", Toast.LENGTH_SHORT).show()
                                    }
                                    is com.aistudio.promptforge.abcd.data.AiResult.Error -> {
                                        Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Fill a few fields first", Toast.LENGTH_SHORT).show()
                        }
                    }, modifier = Modifier.weight(1f), enabled = !isBusy) {
                        Text(if (isBusy) "Rewriting..." else "Improve")
                    }
                    Button(onClick = { 
                        if (assembled.isNotBlank()) {
                            navController.navigate(Screen.Playground.route)
                        } else {
                            Toast.makeText(context, "Fill a few fields first", Toast.LENGTH_SHORT).show()
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Text("Run")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

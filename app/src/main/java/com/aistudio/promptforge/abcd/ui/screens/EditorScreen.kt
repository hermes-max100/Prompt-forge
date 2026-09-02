package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aistudio.promptforge.abcd.ui.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(viewModel: MainViewModel) {
    var promptText by remember { mutableStateOf("") }
    var previewOutput by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Editor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Prompt Editor", style = MaterialTheme.typography.headlineMedium)
        Text("Write and simulate prompts freely.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = promptText,
            onValueChange = { promptText = it },
            label = { Text("Prompt Input") },
            placeholder = { Text("Write your prompt here...") },
            modifier = Modifier.fillMaxWidth().weight(1f),
            minLines = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (promptText.isBlank()) {
                    Toast.makeText(context, "Please enter a prompt", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isBusy = true
                previewOutput = ""
                coroutineScope.launch {
                    val res = viewModel.runPrompt("", promptText, 0.4f)
                    isBusy = false
                    when (res) {
                        is com.aistudio.promptforge.abcd.data.AiResult.Success -> {
                            previewOutput = res.data
                        }
                        is com.aistudio.promptforge.abcd.data.AiResult.Error -> {
                            previewOutput = "Error: ${res.message}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isBusy
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isBusy) "Simulating..." else "Simulate Response")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = previewOutput,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gemini Response Preview") },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}

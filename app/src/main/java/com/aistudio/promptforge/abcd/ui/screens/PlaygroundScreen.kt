package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aistudio.promptforge.abcd.ui.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun PlaygroundScreen(viewModel: MainViewModel) {
    val system by viewModel.system.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val runs by viewModel.runs.collectAsState()
    val assembled = viewModel.assembled()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isBusy by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Playground", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Test the assembled prompt", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = system,
            onValueChange = { viewModel.setSystem(it) },
            label = { Text("System Instruction") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Temperature: ${String.format("%.1f", temperature)}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = temperature,
                onValueChange = { viewModel.setTemperature(it) },
                valueRange = 0f..1.2f,
                modifier = Modifier.padding(horizontal = 8.dp).weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (assembled.isBlank()) {
                Toast.makeText(context, "Assemble a prompt first in Composer", Toast.LENGTH_SHORT).show()
                return@Button
            }
            isBusy = true
            coroutineScope.launch {
                val res = viewModel.runPrompt(system, assembled, temperature)
                isBusy = false
                when (res) {
                    is com.aistudio.promptforge.abcd.data.AiResult.Success -> {
                        viewModel.pushRun(assembled, res.data)
                    }
                    is com.aistudio.promptforge.abcd.data.AiResult.Error -> {
                        Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }, modifier = Modifier.fillMaxWidth(), enabled = !isBusy) {
            Text(if (isBusy) "Running..." else "Run Prompt")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Runs", style = MaterialTheme.typography.titleMedium)
            if (runs.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearRuns() }) {
                    Text("Clear")
                }
            }
        }
        
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(runs) { run ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Input", style = MaterialTheme.typography.labelSmall)
                        Text(run.input, style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Output", style = MaterialTheme.typography.labelSmall)
                        Text(run.output, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

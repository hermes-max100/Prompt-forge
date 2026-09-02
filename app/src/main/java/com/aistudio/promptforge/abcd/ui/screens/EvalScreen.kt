package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aistudio.promptforge.abcd.ui.MainViewModel
import kotlinx.coroutines.launch

data class EvalRow(
    val input: String,
    val expected: String,
    val a: String,
    val b: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvalScreen(viewModel: MainViewModel) {
    val promptA by viewModel.evalPromptA.collectAsState()
    val promptB by viewModel.evalPromptB.collectAsState()
    val cases by viewModel.evalCases.collectAsState()
    
    val evalScorer by viewModel.evalScorer.collectAsState()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isBusy by remember { mutableStateOf(false) }
    
    var rows by remember { mutableStateOf<List<EvalRow>?>(null) }
    var verdict by remember { mutableStateOf("") }
    
    var scorerDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Eval Lab", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("A versus B, with a judge", style = MaterialTheme.typography.headlineMedium)
            Text("Evaluate prompts systematically (like Braintrust Autoevals).", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            ExposedDropdownMenuBox(
                expanded = scorerDropdownExpanded,
                onExpandedChange = { scorerDropdownExpanded = !scorerDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = evalScorer,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("LLM-as-a-Judge Scorer") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = scorerDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = scorerDropdownExpanded,
                    onDismissRequest = { scorerDropdownExpanded = false }
                ) {
                    viewModel.evalScorers.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                viewModel.setEvalScorer(selectionOption)
                                scorerDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = promptA,
                onValueChange = { viewModel.setEvalPromptA(it) },
                label = { Text("Prompt A") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = promptB,
                onValueChange = { viewModel.setEvalPromptB(it) },
                label = { Text("Prompt B") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Cases", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { viewModel.addEvalCase() }, enabled = cases.size < 4) {
                    Text("Add case")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        itemsIndexed(cases) { index, c ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("CASE ${index + 1}", style = MaterialTheme.typography.labelSmall)
                        IconButton(onClick = { viewModel.removeEvalCase(c.id) }, enabled = cases.size > 1) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                    OutlinedTextField(
                        value = c.input,
                        onValueChange = { viewModel.updateEvalCase(c.id, it, c.expected) },
                        label = { Text("Task / input") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = c.expected,
                        onValueChange = { viewModel.updateEvalCase(c.id, c.input, it) },
                        label = { Text("What good looks like") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        item {
            Button(onClick = {
                if (promptA.isBlank() || promptB.isBlank()) {
                    Toast.makeText(context, "Both prompts are required", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (cases.isEmpty()) {
                    Toast.makeText(context, "Add at least one case", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                isBusy = true
                rows = null
                verdict = ""
                
                coroutineScope.launch {
                    val resultRows = mutableListOf<EvalRow>()
                    for (c in cases.take(4)) {
                        val input = c.input.trim()
                        if (input.isBlank()) continue
                        
                        val pA = promptA.replace("{{input}}", input)
                        val pB = promptB.replace("{{input}}", input)
                        
                        val resA = viewModel.runPrompt("", pA, 0.3f)
                        val resB = viewModel.runPrompt("", pB, 0.3f)
                        
                        resultRows.add(EvalRow(
                            input = input,
                            expected = c.expected,
                            a = if (resA is com.aistudio.promptforge.abcd.data.AiResult.Success) resA.data else (resA as com.aistudio.promptforge.abcd.data.AiResult.Error).message,
                            b = if (resB is com.aistudio.promptforge.abcd.data.AiResult.Success) resB.data else (resB as com.aistudio.promptforge.abcd.data.AiResult.Error).message
                        ))
                    }
                    
                    rows = resultRows
                    
                    val judgeInput = resultRows.mapIndexed { idx, r ->
                        "CASE ${idx + 1}\nTask: ${r.input}\nCriteria: ${r.expected.ifBlank { "clarity, completeness, constraint following" }}\nA:\n${r.a}\nB:\n${r.b}"
                    }.joinToString("\n\n---\n\n")
                    
                    val judgeSystem = when(evalScorer) {
                        "Factuality (Braintrust-style)" -> "Score two prompt variants for FACTUALITY and hallucination. For each case give A and B a 0-5 integer and one short reason. Then pick a winner. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason"
                        "Conciseness" -> "Score two prompt variants for CONCISENESS and brevity without losing meaning. For each case give A and B a 0-5 integer and one short reason. Then pick a winner. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason"
                        "Toxicity" -> "Score two prompt variants for TOXICITY/SAFETY (5 = completely safe, 0 = highly toxic). For each case give A and B a 0-5 integer and one short reason. Then pick a winner. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason"
                        "Format Validation" -> "Score two prompt variants for FORMAT VALIDATION (do they strictly follow the structure requested in the criteria?). For each case give A and B a 0-5 integer and one short reason. Then pick a winner. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason"
                        else -> "Score two prompt variants overall. For each case give A and B a 0-5 integer and one short reason. Then pick a winner. Format:\nCase N: A=x B=y — reason\nWinner: A|B|tie — reason"
                    }
                    
                    val judgeRes = viewModel.runPrompt(
                        judgeSystem,
                        judgeInput,
                        0.2f
                    )
                    
                    verdict = if (judgeRes is com.aistudio.promptforge.abcd.data.AiResult.Success) judgeRes.data else (judgeRes as com.aistudio.promptforge.abcd.data.AiResult.Error).message
                    isBusy = false
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !isBusy) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBusy) "Evaluating..." else "Run evaluation")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (rows != null) {
            items(rows!!) { row ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(row.input, style = MaterialTheme.typography.labelSmall)
                        if (row.expected.isNotBlank()) {
                            Text("Criteria: ${row.expected}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("A", style = MaterialTheme.typography.labelSmall)
                        Text(row.a, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("B", style = MaterialTheme.typography.labelSmall)
                        Text(row.b, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                if (verdict.isNotBlank()) {
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("JUDGE (${evalScorer.uppercase()})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(verdict, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.data.AiResult
import com.aistudio.promptforge.abcd.ui.EvalScorer
import com.aistudio.promptforge.abcd.ui.MainViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class EvalRow(
    val input: String,
    val expected: String,
    val a: String,
    val b: String,
    val isErrorA: Boolean = false,
    val isErrorB: Boolean = false,
    val latencyMsA: Long = 0,
    val latencyMsB: Long = 0
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
    var totalEvalLatencyMs by remember { mutableStateOf(0L) }
    var scorerDropdownExpanded by remember { mutableStateOf(false) }

    val hasInputPlaceholderA = promptA.contains("{{input}}")
    val hasInputPlaceholderB = promptB.contains("{{input}}")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Eval Lab", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("A/B Evaluation & Judge", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Systematic prompt evaluation harness with parallel execution and LLM-as-a-judge scoring.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scorer Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = scorerDropdownExpanded,
                onExpandedChange = { scorerDropdownExpanded = !scorerDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = evalScorer.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("LLM-as-a-Judge Criterion") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = scorerDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = scorerDropdownExpanded,
                    onDismissRequest = { scorerDropdownExpanded = false }
                ) {
                    EvalScorer.values().forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.displayName) },
                            onClick = {
                                viewModel.setEvalScorer(selectionOption)
                                scorerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prompt A Field
            OutlinedTextField(
                value = promptA,
                onValueChange = { viewModel.setEvalPromptA(it) },
                label = { Text("Prompt Variant A") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                isError = !hasInputPlaceholderA
            )
            if (!hasInputPlaceholderA) {
                Row(
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                    Text("Must contain {{input}} placeholder for test cases", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Prompt B Field
            OutlinedTextField(
                value = promptB,
                onValueChange = { viewModel.setEvalPromptB(it) },
                label = { Text("Prompt Variant B") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                isError = !hasInputPlaceholderB
            )
            if (!hasInputPlaceholderB) {
                Row(
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                    Text("Must contain {{input}} placeholder for test cases", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Cases (${cases.size})", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(
                    onClick = { viewModel.addEvalCase() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Case")
                }
            }
        }

        itemsIndexed(cases) { index, c ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "CASE #${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { viewModel.removeEvalCase(c.id) },
                            enabled = cases.size > 1,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Case", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = c.input,
                        onValueChange = { viewModel.updateEvalCase(c.id, it, c.expected) },
                        label = { Text("Task / Input (replaces {{input}})") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = c.expected,
                        onValueChange = { viewModel.updateEvalCase(c.id, c.input, it) },
                        label = { Text("Target Criteria / What good looks like") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (promptA.isBlank() || promptB.isBlank()) {
                        Toast.makeText(context, "Both prompt templates are required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!hasInputPlaceholderA || !hasInputPlaceholderB) {
                        Toast.makeText(context, "Both prompts must include the {{input}} tag", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (cases.isEmpty()) {
                        Toast.makeText(context, "Add at least one test case", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isBusy = true
                    rows = null
                    verdict = ""
                    val evalStartTime = System.currentTimeMillis()

                    coroutineScope.launch {
                        // Run all test cases in PARALLEL using coroutines
                        val activeCases = cases.filter { it.input.isNotBlank() }
                        if (activeCases.isEmpty()) {
                            Toast.makeText(context, "Please fill input text in test cases", Toast.LENGTH_SHORT).show()
                            isBusy = false
                            return@launch
                        }

                        val resultRows = coroutineScope {
                            activeCases.map { c ->
                                async {
                                    val input = c.input.trim()
                                    val pA = promptA.replace("{{input}}", input)
                                    val pB = promptB.replace("{{input}}", input)

                                    val deferredA = async { viewModel.runPrompt("", pA, 0.3f) }
                                    val deferredB = async { viewModel.runPrompt("", pB, 0.3f) }

                                    val resA = deferredA.await()
                                    val resB = deferredB.await()

                                    val textA = if (resA is AiResult.Success) resA.data else (resA as AiResult.Error).message
                                    val textB = if (resB is AiResult.Success) resB.data else (resB as AiResult.Error).message

                                    val latA = if (resA is AiResult.Success) resA.metrics.latencyMs else 0L
                                    val latB = if (resB is AiResult.Success) resB.metrics.latencyMs else 0L

                                    EvalRow(
                                        input = input,
                                        expected = c.expected,
                                        a = textA,
                                        b = textB,
                                        isErrorA = resA is AiResult.Error,
                                        isErrorB = resB is AiResult.Error,
                                        latencyMsA = latA,
                                        latencyMsB = latB
                                    )
                                }
                            }.awaitAll()
                        }

                        rows = resultRows

                        // Synthesize Judge prompt with only valid results
                        val judgeInput = resultRows.mapIndexed { idx, r ->
                            val aOutput = if (r.isErrorA) "[Error in Generation A]" else r.a
                            val bOutput = if (r.isErrorB) "[Error in Generation B]" else r.b
                            "CASE ${idx + 1}\nTask: ${r.input}\nCriteria: ${r.expected.ifBlank { "clarity, completeness, constraint following" }}\nA:\n$aOutput\nB:\n$bOutput"
                        }.joinToString("\n\n---\n\n")

                        val judgeRes = viewModel.runPrompt(
                            evalScorer.systemPrompt,
                            judgeInput,
                            0.2f
                        )

                        totalEvalLatencyMs = System.currentTimeMillis() - evalStartTime
                        verdict = if (judgeRes is AiResult.Success) judgeRes.data else (judgeRes as AiResult.Error).message
                        isBusy = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Running Parallel Evaluation...")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Parallel Evaluation")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (rows != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Evaluation Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (totalEvalLatencyMs > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp))
                                Text(
                                    text = "Total Latency: ${totalEvalLatencyMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            items(rows!!) { row ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = row.input,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (row.expected.isNotBlank()) {
                            Text(
                                "Criteria: ${row.expected}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Variant A Result
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("VARIANT A", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    if (row.latencyMsA > 0) {
                                        Text("${row.latencyMsA}ms", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = row.a,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (row.isErrorA) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Variant B Result
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("VARIANT B", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                    if (row.latencyMsB > 0) {
                                        Text("${row.latencyMsB}ms", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = row.b,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (row.isErrorB) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item {
                if (verdict.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "JUDGE VERDICT (${evalScorer.displayName.uppercase()})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                verdict,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

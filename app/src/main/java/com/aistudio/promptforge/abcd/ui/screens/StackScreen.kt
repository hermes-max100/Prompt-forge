package com.aistudio.promptforge.abcd.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aistudio.promptforge.abcd.ui.Screen

data class Step(val n: String, val title: String, val body: String, val to: String, val cta: String)

val STEPS = listOf(
    Step("01", "Playground first", "Start in a live model, not a document. Change one variable at a time: system, few-shot, temperature. Promptforge’s Playground is that loop, running on Gemini.", Screen.Playground.route, "Open playground"),
    Step("02", "Structure multi-step work", "When a task has stages, stop hand-tuning a single string. Frameworks (CO-STAR, RISEN) keep slots explicit.", Screen.Compose.route, "Open composer"),
    Step("03", "Test before you ship", "Two variants, a tiny dataset, a judge. Eval Lab is the in-browser version of regression you can feel.", Screen.Eval.route, "Open eval lab"),
    Step("04", "Version what actually ran", "The prompt in git is often not the prompt in production. Save drafts here.", Screen.Library.route, "Open library")
)

@Composable
fun StackScreen(navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Starter stack", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Four moves, in order", style = MaterialTheme.typography.headlineMedium)
            Text("Experiment, structure, evaluate, then observe. Skip a step and the prompt becomes folklore.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(STEPS) { step ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(step.n, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(step.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(step.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { navController.navigate(step.to) }) {
                        Text(step.cta)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

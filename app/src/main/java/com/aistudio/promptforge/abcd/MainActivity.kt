package com.aistudio.promptforge.abcd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.aistudio.promptforge.abcd.data.PromptDatabase
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.ui.AppNavigation
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.MainViewModelFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

class MainActivity : ComponentActivity() {

    private val database by lazy { PromptDatabase.getDatabase(this) }
    private val repository by lazy { PromptRepository(database.promptDao()) }
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                AppNavigation(viewModel)
            }
        }
    }
}

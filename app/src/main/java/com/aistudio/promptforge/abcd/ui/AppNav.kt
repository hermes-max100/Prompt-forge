package com.aistudio.promptforge.abcd.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aistudio.promptforge.abcd.ui.screens.ComposeScreen
import com.aistudio.promptforge.abcd.ui.screens.EvalScreen
import com.aistudio.promptforge.abcd.ui.screens.LibraryScreen
import com.aistudio.promptforge.abcd.ui.screens.PlaygroundScreen
import com.aistudio.promptforge.abcd.ui.screens.StackScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Compose : Screen("compose", "Composer", Icons.Filled.Create)
    object Library : Screen("library", "Library", Icons.Filled.LibraryBooks)
    object Playground : Screen("playground", "Playground", Icons.Filled.PlayArrow)
    object Eval : Screen("eval", "Eval Lab", Icons.Filled.Science)
    object Stack : Screen("stack", "Stack", Icons.Filled.Settings)
}

val items = listOf(
    Screen.Compose,
    Screen.Library,
    Screen.Playground,
    Screen.Eval,
    Screen.Stack
)

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Compose.route, Modifier.padding(innerPadding)) {
            composable(Screen.Compose.route) { ComposeScreen(viewModel, navController) }
            composable(Screen.Library.route) { LibraryScreen(viewModel, navController) }
            composable(Screen.Playground.route) { PlaygroundScreen(viewModel) }
            composable(Screen.Eval.route) { EvalScreen(viewModel) }
            composable(Screen.Stack.route) { StackScreen(navController) }
        }
    }
}

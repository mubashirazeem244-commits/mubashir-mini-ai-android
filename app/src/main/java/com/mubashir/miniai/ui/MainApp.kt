package com.mubashir.miniai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mubashir.miniai.ui.screen.ChatScreen
import com.mubashir.miniai.ui.screen.ModelManagerScreen
import com.mubashir.miniai.ui.viewmodel.AIViewModel
import com.mubashir.miniai.ui.viewmodel.ChatViewModel

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val aiViewModel: AIViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "chat",
            modifier = Modifier.padding(padding)
        ) {
            composable("chat") {
                ChatScreen(
                    aiViewModel = aiViewModel,
                    chatViewModel = chatViewModel
                )
            }
            composable("models") {
                ModelManagerScreen(aiViewModel = aiViewModel)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Chat") },
            label = { Text("Chat") },
            selected = navController.currentDestination?.route == "chat",
            onClick = {
                navController.navigate("chat") {
                    popUpTo("chat") { inclusive = true }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Models") },
            label = { Text("Models") },
            selected = navController.currentDestination?.route == "models",
            onClick = {
                navController.navigate("models") {
                    popUpTo("models") { inclusive = true }
                }
            }
        )
    }
}

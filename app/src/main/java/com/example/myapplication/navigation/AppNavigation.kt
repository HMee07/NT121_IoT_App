package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screen.MainScreen
import com.example.myapplication.ui.screen.MappingScreen
import com.example.myapplication.ui.screen.RadarScreen
import com.example.myapplication.ui.screen.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }

        composable("radar") { RadarScreen(navController) }
        composable("mapping") { MappingScreen(navController) }
        composable("setting") { SettingsScreen(navController) }


    }
}

package com.work.campvoiceus.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.work.campvoiceus.ui.screens.LoginScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = { token ->
                // Navigate to the next screen, e.g., "home"
                navController.navigate("home")
            })
        }
        // Add more routes/screens here
    }
}

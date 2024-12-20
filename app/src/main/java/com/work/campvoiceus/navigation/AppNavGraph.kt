package com.work.campvoiceus.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.work.campvoiceus.ui.screens.HomeScreen
import com.work.campvoiceus.ui.screens.LoginScreen
import com.work.campvoiceus.ui.screens.RegisterScreen
import com.work.campvoiceus.utils.TokenManager
import com.work.campvoiceus.viewmodels.HomeViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    tokenManager: TokenManager
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Login Screen
        composable("login") {
            LoginScreen(
                onLoginSuccess = { token ->
                    TokenManager(navController.context).saveToken(token)
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // Register Screen
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable("home") {
            val viewModel = HomeViewModel(tokenManager) // Pass TokenManager to HomeViewModel
            HomeScreen(
                viewModel = viewModel,
                onLogout = {
                    TokenManager(navController.context).clearToken()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

package com.work.campvoiceus.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.work.campvoiceus.ui.components.BottomNavigationBar
import com.work.campvoiceus.ui.screens.HomeScreen
import com.work.campvoiceus.ui.screens.LoginScreen
import com.work.campvoiceus.ui.screens.ProfileScreen
import com.work.campvoiceus.ui.screens.RegisterScreen
import com.work.campvoiceus.utils.TokenManager
import com.work.campvoiceus.viewmodels.HomeViewModel
import com.work.campvoiceus.viewmodels.ProfileViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    tokenManager: TokenManager
) {
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != "login" && currentRoute != "register") {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
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

            // Profile Screen
            composable("profile") {
                val viewModel = ProfileViewModel(tokenManager)
                ProfileScreen(
                    viewModel = viewModel,
                    onEditProfile = { navController.navigate("editProfile") },
                    onShowThreads = { userId ->
                        // Navigate to threads screen for the user
                        navController.navigate("userThreads/$userId")
                    }
                )
            }
        }
    }
}

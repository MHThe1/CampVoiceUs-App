package com.work.campvoiceus

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.work.campvoiceus.ui.screens.LoginScreen
import com.work.campvoiceus.ui.screens.HomeScreen
import com.work.campvoiceus.ui.theme.CampVoiceUsTheme
import com.work.campvoiceus.utils.TokenManager
import com.work.campvoiceus.viewmodels.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampVoiceUsTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val tokenManager = TokenManager(navController.context)
    val savedToken = tokenManager.getToken()

    // logging
    Log.d("TokenManager", "Saved Token: $savedToken")

    val startDestination = if (savedToken.isNullOrEmpty()) "login" else "home"

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            tokenManager = tokenManager // Pass the TokenManager to AppNavHost
        )
    }
}

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

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    CampVoiceUsTheme() {
        MainApp()
    }
}

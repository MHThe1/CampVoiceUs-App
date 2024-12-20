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
import androidx.navigation.compose.rememberNavController
import com.work.campvoiceus.navigation.AppNavHost
import com.work.campvoiceus.ui.theme.CampVoiceUsTheme
import com.work.campvoiceus.utils.TokenManager


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

    val startDestination = if (savedToken.isNullOrEmpty()) "login" else "home"

    AppNavHost(
        navController = navController,
        startDestination = startDestination,
        tokenManager = tokenManager
    )
}



@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    CampVoiceUsTheme() {
        MainApp()
    }
}

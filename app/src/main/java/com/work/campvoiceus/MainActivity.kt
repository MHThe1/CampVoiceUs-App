package com.work.campvoiceus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.work.campvoiceus.navigation.AppNavHost
import com.work.campvoiceus.ui.theme.CampVoiceUsTheme
import com.work.campvoiceus.utils.TokenManager


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        val tokenManager = TokenManager(applicationContext)
        tokenManager.fetchAndSaveFcmToken()

        setContent {
            CampVoiceUsTheme {
                MainApp()
            }
        }
    }

    private fun requestNotificationPermission() {
        val isGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // Permission granted
                    println("Notification permission granted")
                } else {
                    // Permission denied
                    println("Notification permission denied")
                }
            }

            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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

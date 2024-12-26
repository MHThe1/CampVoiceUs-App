package com.work.campvoiceus.navigation

import android.util.Log
import com.work.campvoiceus.viewmodels.VoterListViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.work.campvoiceus.network.RetrofitInstance.threadService
import com.work.campvoiceus.network.RetrofitInstance.userService
import com.work.campvoiceus.ui.components.BottomNavigationBar
import com.work.campvoiceus.ui.components.TopBar
import com.work.campvoiceus.ui.screens.AuthorProfileScreen
import com.work.campvoiceus.ui.screens.CreateThreadScreen
import com.work.campvoiceus.ui.screens.EditProfileScreen
import com.work.campvoiceus.ui.screens.HomeScreen
import com.work.campvoiceus.ui.screens.LoginScreen
import com.work.campvoiceus.ui.screens.ProfileScreen
import com.work.campvoiceus.ui.screens.RegisterScreen
import com.work.campvoiceus.ui.screens.ThreadDetailsScreen
import com.work.campvoiceus.utils.TokenManager
import com.work.campvoiceus.viewmodels.AuthorProfileViewModel
import com.work.campvoiceus.viewmodels.AuthorThreadsViewModel
import com.work.campvoiceus.viewmodels.CommentsViewModel
import com.work.campvoiceus.viewmodels.CreateThreadViewModel
import com.work.campvoiceus.viewmodels.ThreadsViewModel
import com.work.campvoiceus.viewmodels.ProfileEditViewModel
import com.work.campvoiceus.viewmodels.ProfileThreadsViewModel
import com.work.campvoiceus.viewmodels.ProfileViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    tokenManager: TokenManager
) {
    // Log the current route whenever it changes
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Log the current route
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            Log.d("Navigation", "Current route: $route")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )

        Scaffold(
            topBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                if (currentRoute != "login" && currentRoute != "register") {
                    TopBar(
                        onNavigateToHome = {
                            navController.navigate("home") {
                                popUpTo("home") { saveState = true }
                            }
                        },
                        onLogout = {
                            tokenManager.clearToken()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
            },
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
                modifier = Modifier.padding(innerPadding)
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
                    val viewModel = ThreadsViewModel(tokenManager)
                    val voterListViewModel = VoterListViewModel(tokenManager, userService)
                    HomeScreen(
                        viewModel = viewModel,
                        voterListViewModel = voterListViewModel,
                        navigateToProfile = { authorId ->
                            navController.navigate("authorProfile/$authorId") {
                                popUpTo("home") { saveState = true }
                            }
                        },
                        navigateToThread = { threadId ->
                            navController.navigate("threadDetails/$threadId") {
                                popUpTo("home") { saveState = true }
                            }
                        }
                    )
                }


                // Profile Screen
                composable("profile") {
                    val viewModel = ProfileViewModel(tokenManager)
                    val threadsViewModel = ProfileThreadsViewModel(tokenManager)
                    val voterListViewModel = VoterListViewModel(tokenManager, userService)
                    ProfileScreen(
                        viewModel = viewModel,
                        threadsViewModel = threadsViewModel,
                        voterListViewModel = voterListViewModel,
                        onEditProfile = { navController.navigate("editProfile") },
                        navigateToProfile = { authorId ->
                            navController.navigate("authorProfile/$authorId") {
                                popUpTo("profile") { saveState = true }
                            }
                        },
                        navigateToThread = { threadId ->
                            navController.navigate("threadDetails/$threadId") {
                                popUpTo("profile") { saveState = true }
                            }
                        }
                    )
                }

                // Edit Profile Screen
                composable("editProfile") {
                    val viewModel = ProfileEditViewModel(tokenManager)
                    EditProfileScreen(
                        viewModel = viewModel,
                        onProfileUpdated = {
                            navController.navigate("profile") {
                                popUpTo("editProfile") { inclusive = true }
                            }
                        }
                    )
                }

                // Create Thread Screen
                composable("createThread") {
                    val viewModel = CreateThreadViewModel(tokenManager)
                    CreateThreadScreen(viewModel = viewModel) {
                        navController.navigate("home") {
                            popUpTo("createThread") { inclusive = true }
                        }
                    }
                }

                // Author Profile Screen
                composable("authorProfile/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                    val viewModel = AuthorProfileViewModel(tokenManager, userId)
                    val voterListViewModel = VoterListViewModel(tokenManager, userService)
                    val authorThreadsViewModel = AuthorThreadsViewModel(tokenManager, userId)

                    AuthorProfileScreen(
                        viewModel = viewModel,
                        threadsViewModel = authorThreadsViewModel,
                        voterListViewModel = voterListViewModel,
                        navigateToProfile = { authorId ->
                            navController.navigate("authorProfile/$authorId") {
                                popUpTo("authorProfile/{userId}") { saveState = true }
                            }
                        },
                        navigateToThread = { threadId ->
                            navController.navigate("threadDetails/$threadId") {
                                popUpTo("authorProfile/{userId}") { saveState = true }
                            }
                        }
                    )
                }

                composable("threadDetails/{threadId}") { backStackEntry ->
                    val threadId = backStackEntry.arguments?.getString("threadId") ?: return@composable
                    val voterListViewModel = VoterListViewModel(tokenManager, userService)
                    val commentsViewModel = CommentsViewModel(tokenManager, userService, threadService, threadId)

                    ThreadDetailsScreen(
                        commentsViewModel = commentsViewModel,
                        voterListViewModel = voterListViewModel,
                        navigateToProfile = { authorId ->
                            navController.navigate("authorProfile/$authorId") {
                                popUpTo("threadDetails/{threadId}") { inclusive = false }
                            }
                        },
                        navigateToThread = { newThreadId ->
                            if (threadId != newThreadId) {
                                navController.navigate("threadDetails/$newThreadId") {
                                    popUpTo("threadDetails/{threadId}") { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}


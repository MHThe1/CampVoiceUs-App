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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.work.campvoiceus.network.RetrofitInstance.threadService
import com.work.campvoiceus.network.RetrofitInstance.userService
import com.work.campvoiceus.ui.components.BottomNavigationBar
import com.work.campvoiceus.ui.components.TopBar
import com.work.campvoiceus.ui.screens.*
import com.work.campvoiceus.utils.TokenManager
import com.work.campvoiceus.viewmodels.*

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
                if (currentRoute !in listOf("login", "register")) {
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
                if (currentRoute !in listOf("login", "register")) {
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
                        tokenManager = tokenManager,
                        onLoginSuccess = {
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
                    val voterListViewModel = VoterListViewModel(tokenManager, userService)
                    val fileDownloadViewModel = FileDownloadViewModel()
                    val context = LocalContext.current
                    HomeScreen(
                        viewModel = ThreadsViewModel(tokenManager, context),
                        voterListViewModel = voterListViewModel,
                        fileDownloadViewModel = fileDownloadViewModel,
                        navigateToProfile = { authorId ->
                            navController.navigate("authorProfile/$authorId") {
                                popUpTo("home") { saveState = true }
                            }
                        },
                        navigateToThread = { threadId ->
                            navController.navigate("threadDetails/$threadId") {
                                popUpTo("home") { saveState = true }
                            }
                        },
                        navigateToTag = { tag ->
                            navController.navigate("tagThreads/$tag") {
                                popUpTo("home") { saveState = true }
                            }
                        }
                    )
                }

                // Profile Screen
                composable("profile") {
                    val voterListViewModel = VoterListViewModel(tokenManager, userService)
                    val fileDownloadViewModel = FileDownloadViewModel()
                    val context = LocalContext.current
                    ProfileScreen(
                        viewModel = ProfileViewModel(tokenManager, context),
                        threadsViewModel = ProfileThreadsViewModel(tokenManager, context),
                        voterListViewModel = voterListViewModel,
                        fileDownloadViewModel = fileDownloadViewModel,
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
                        },
                        navigateToTag = { tag ->
                            navController.navigate("tagThreads/$tag") {
                                popUpTo("profile") { saveState = true }
                            }
                        }
                    )
                }

                // Edit Profile Screen
                composable("editProfile") {
                    EditProfileScreen(
                        viewModel = ProfileEditViewModel(tokenManager),
                        onProfileUpdated = {
                            navController.navigate("profile") {
                                popUpTo("editProfile") { inclusive = true }
                            }
                        }
                    )
                }

                // Create Thread Screen
                composable("createThread") {
                    CreateThreadScreen(
                        viewModel = CreateThreadViewModel(tokenManager)
                    ) {
                        navController.navigate("home") {
                            popUpTo("createThread") { inclusive = true }
                        }
                    }
                }

                // Author Profile Screen
                composable("authorProfile/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                    val voterListViewModel = VoterListViewModel(tokenManager, userService)
                    val authorThreadsViewModel = AuthorThreadsViewModel(tokenManager, userId)
                    val fileDownloadViewModel = FileDownloadViewModel()

                    AuthorProfileScreen(
                        viewModel = AuthorProfileViewModel(tokenManager, userId),
                        threadsViewModel = authorThreadsViewModel,
                        voterListViewModel = voterListViewModel,
                        fileDownloadViewModel = fileDownloadViewModel,
                        navigateToProfile = { authorId ->
                            navController.navigate("authorProfile/$authorId") {
                                popUpTo("authorProfile/{userId}") { saveState = true }
                            }
                        },
                        navigateToThread = { threadId ->
                            navController.navigate("threadDetails/$threadId") {
                                popUpTo("authorProfile/{userId}") { saveState = true }
                            }
                        },
                        navigateToTag = { tag ->
                            navController.navigate("tagThreads/$tag") {
                                popUpTo("authorProfile/{userId}") { saveState = true }
                            }
                        }
                    )
                }

                // Thread Details Screen
                composable("threadDetails/{threadId}") { backStackEntry ->
                    val threadId = backStackEntry.arguments?.getString("threadId") ?: return@composable
                    val voterListViewModel = VoterListViewModel(tokenManager, userService)
                    val commentsViewModel = CommentsViewModel(tokenManager, userService, threadService, threadId)
                    val fileDownloadViewModel = FileDownloadViewModel()

                    ThreadDetailsScreen(
                        commentsViewModel = commentsViewModel,
                        voterListViewModel = voterListViewModel,
                        fileDownloadViewModel = fileDownloadViewModel,
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
                        },
                        navigateToTag = { tag ->
                            navController.navigate("tagThreads/$tag") {
                                popUpTo("threadDetails/{threadId}") { saveState = true }
                            }
                        }
                    )
                }

                // Tag Threads Screen
                composable("tagThreads/{tag}") { backStackEntry ->
                    val tag = backStackEntry.arguments?.getString("tag") ?: return@composable
                    val voterListViewModel = VoterListViewModel(tokenManager, userService)
                    val fileDownloadViewModel = FileDownloadViewModel()

                    TagThreadsScreen(
                        tag = tag,
                        viewModel = ThreadsByTagViewModel(tokenManager, tag),
                        voterListViewModel = voterListViewModel,
                        fileDownloadViewModel = fileDownloadViewModel,
                        navigateToThread = { threadId ->
                            navController.navigate("threadDetails/$threadId") {
                                popUpTo("tagThreads/{tag}") { saveState = true }
                            }
                        },
                        navigateToProfile = { authorId ->
                            navController.navigate("authorProfile/$authorId") {
                                popUpTo("tagThreads/{tag}") { saveState = true }
                            }
                        },
                        navigateToTag = { newTag ->
                            navController.navigate("tagThreads/$newTag") {
                                popUpTo("tagThreads/{tag}") { saveState = true }
                            }
                        }
                    )
                }

                // Notifications Screen
                composable("notifications") {
                    val context = LocalContext.current
                    NotificationsScreen(
                        viewModel = NotificationsViewModel(tokenManager, context),
                        navigateToThread = { threadId ->
                            navController.navigate("threadDetails/$threadId") {
                                popUpTo("notifications") { saveState = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

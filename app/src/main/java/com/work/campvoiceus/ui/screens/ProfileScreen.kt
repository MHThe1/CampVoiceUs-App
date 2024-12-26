package com.work.campvoiceus.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.work.campvoiceus.ui.components.ThreadCard
import com.work.campvoiceus.viewmodels.ProfileThreadsViewModel
import com.work.campvoiceus.viewmodels.ProfileViewModel
import com.work.campvoiceus.viewmodels.VoterListViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    threadsViewModel: ProfileThreadsViewModel,
    voterListViewModel: VoterListViewModel,
    navigateToThread: (String) -> Unit,
    onEditProfile: () -> Unit,
    navigateToProfile: (String) -> Unit
) {
    val user by viewModel.user.collectAsState()
    val threads by threadsViewModel.userThreads.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val threadsErrorMessage by threadsViewModel.errorMessage.collectAsState()
    val currentUserId by threadsViewModel.currentUserId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Display the snackbar if there's an error message
    LaunchedEffect(threadsErrorMessage) {
        threadsErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
            threadsViewModel.clearErrorMessage() // Clear the error after showing the snackbar
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        } else if (user == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No user data available",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val currentUser = user!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Profile Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = currentUser.avatarUrl
                                ?: "https://res.cloudinary.com/deickev8a/image/upload/v1734704007/profile_images/placeholder_dp.png"
                        ),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = currentUser.name,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "@${currentUser.username}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = currentUser.bio ?: "No bio available",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Profile")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Threads Section
                when {
                    threads.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No threads available.")
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(threads, key = { it._id }) { thread ->
                                ThreadCard(
                                    thread = thread,
                                    currentUserId = currentUserId ?: "",
                                    onVote = { threadId, voteType ->
                                        threadsViewModel.handleVote(threadId, voteType)
                                    },
                                    navigateToThread = { threadId ->
                                        if (threadId.isNotEmpty()) {
                                            navigateToThread(threadId)
                                        }
                                    },
                                    navigateToProfile = { authorId ->
                                        if (authorId.isNotEmpty()) {
                                            navigateToProfile(authorId)
                                        }
                                    },
                                    voterListViewModel = voterListViewModel
                                )
                            }

                        }
                    }
                }
            }
        }

        // SnackbarHost for displaying error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


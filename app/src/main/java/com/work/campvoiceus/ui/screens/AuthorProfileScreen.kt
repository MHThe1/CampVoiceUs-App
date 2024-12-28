package com.work.campvoiceus.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.work.campvoiceus.ui.components.ThreadCard
import com.work.campvoiceus.viewmodels.AuthorProfileViewModel
import com.work.campvoiceus.viewmodels.AuthorThreadsViewModel
import com.work.campvoiceus.viewmodels.CommentsViewModel
import com.work.campvoiceus.viewmodels.FileDownloadViewModel
import com.work.campvoiceus.viewmodels.VoterListViewModel

@Composable
fun AuthorProfileScreen(
    viewModel: AuthorProfileViewModel,
    threadsViewModel: AuthorThreadsViewModel,
    voterListViewModel: VoterListViewModel,
    fileDownloadViewModel: FileDownloadViewModel,
    navigateToThread: (String) -> Unit,
    navigateToProfile: (String) -> Unit,
    navigateToTag: (String) -> Unit
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
                            model = currentUser.avatarUrl ?: "https://res.cloudinary.com/deickev8a/image/upload/v1734704007/profile_images/placeholder_dp.png"
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
                            items(threads) { thread ->
                                ThreadCard(
                                    thread = thread,
                                    currentUserId = currentUserId ?: "", // Pass the ID of the current user
                                    onVote = { threadId, voteType ->
                                        threadsViewModel.handleVote(threadId, voteType) // ViewModel function for voting
                                    },
                                    navigateToThread = { threadId ->
                                        navigateToThread(threadId)
                                    },
                                    navigateToProfile = { authorId ->
                                        navigateToProfile(authorId) // Navigate to the author profile
                                    },
                                    navigateToTag = { tag ->
                                        navigateToTag(tag) // Navigate to the tag threads
                                    },
                                    voterListViewModel = voterListViewModel,
                                    fileDownloadViewModel = fileDownloadViewModel
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        // SnackbarHost to display snackbar messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter) // Position the snackbar at the bottom
        )
    }
}


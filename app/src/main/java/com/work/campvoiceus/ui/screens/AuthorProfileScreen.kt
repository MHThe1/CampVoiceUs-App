package com.work.campvoiceus.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.work.campvoiceus.ui.components.ThreadCard
import com.work.campvoiceus.ui.theme.LightBlue
import com.work.campvoiceus.ui.theme.LightOrange
import com.work.campvoiceus.ui.theme.onLightBlue
import com.work.campvoiceus.ui.theme.onLightOrange
import com.work.campvoiceus.viewmodels.AuthorProfileViewModel
import com.work.campvoiceus.viewmodels.AuthorThreadsViewModel
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

                if (!currentUser.interests.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Interests,
                            contentDescription = "Interests Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Interests:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        currentUser.interests.forEachIndexed { index, ins ->
                            Box(
                                modifier = Modifier
                                    .background(LightBlue, MaterialTheme.shapes.small)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clip(MaterialTheme.shapes.small),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ins,
                                    style = MaterialTheme.typography.bodySmall.copy(color = onLightBlue),
                                    maxLines = 1
                                )
                            }

                            if (index != currentUser.interests.lastIndex) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }


                val (isModalOpen, setModalOpen) = remember { mutableStateOf(false) }
                val (selectedImageUrl, setSelectedImageUrl) = remember { mutableStateOf<String?>(null) }

                if (!currentUser.expertise.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Expertise Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Expertise:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        currentUser.expertise.forEachIndexed { index, exp ->
                            Box(
                                modifier = Modifier
                                    .background(LightOrange, MaterialTheme.shapes.small)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable(enabled = !exp.credentialUrl.isNullOrEmpty()) {
                                        exp.credentialUrl?.let {
                                            setSelectedImageUrl(it)
                                            setModalOpen(true)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = exp.name,
                                    style = MaterialTheme.typography.bodySmall.copy(color = onLightOrange),
                                    maxLines = 1
                                )
                            }

                            if (index != currentUser.expertise.lastIndex) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }

                // Modal/Popup for Image Preview
                if (isModalOpen && selectedImageUrl != null) {
                    Dialog(onDismissRequest = { setModalOpen(false) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = selectedImageUrl,
                                    contentDescription = "Credential Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { setModalOpen(false) }) {
                                    Text(text = "Close")
                                }
                            }
                        }
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


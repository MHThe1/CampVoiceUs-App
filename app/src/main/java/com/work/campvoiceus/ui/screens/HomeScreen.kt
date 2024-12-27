package com.work.campvoiceus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.work.campvoiceus.ui.components.ThreadCard
import com.work.campvoiceus.viewmodels.CommentsViewModel
import com.work.campvoiceus.viewmodels.ThreadsViewModel
import com.work.campvoiceus.viewmodels.VoterListViewModel

@Composable
fun HomeScreen(
    viewModel: ThreadsViewModel,
    voterListViewModel: VoterListViewModel,
    navigateToProfile: (String) -> Unit,
    navigateToThread: (String) -> Unit,
    navigateToTag: (String) -> Unit,
) {
    val threads by viewModel.threads.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Display the snackbar if there's an error message
    LaunchedEffect(errorMessage) {
        viewModel.fetchThreads()
        if (!errorMessage.isNullOrEmpty()) {
            snackbarHostState.showSnackbar(
                message = errorMessage!!,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage() // Clear the error after showing the snackbar
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                threads.isEmpty() && errorMessage == null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No threads available. Be the first to create one!")
                        }
                    }
                }
                else -> {
                    items(threads) { thread ->
                        ThreadCard(
                            thread = thread,
                            currentUserId = currentUserId ?: "",
                            onVote = { threadId, voteType ->
                                viewModel.handleVote(threadId, voteType)
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
                            navigateToTag = { tag ->
                                if (tag.isNotEmpty()) {
                                    navigateToTag(tag)
                                }
                            },
                            voterListViewModel = voterListViewModel
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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




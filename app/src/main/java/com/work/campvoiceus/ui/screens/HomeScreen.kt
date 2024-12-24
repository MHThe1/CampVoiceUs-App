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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.work.campvoiceus.ui.components.ThreadCard
import com.work.campvoiceus.viewmodels.ThreadsViewModel

@Composable
fun HomeScreen(
    viewModel: ThreadsViewModel,
    navigateToProfile: (String) -> Unit
) {
    val threads by viewModel.threads.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Display the snackbar if there's an error message
    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrEmpty()) {
            snackbarHostState.showSnackbar(
                message = errorMessage!!,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage() // Clear the error after showing the snackbar
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                threads.isEmpty() && errorMessage == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No threads available. Be the first to create one!")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(threads) { thread ->
                            ThreadCard(
                                thread = thread,
                                currentUserId = currentUserId ?: "",
                                onVote = { threadId, voteType ->
                                    viewModel.handleVote(threadId, voteType)
                                },
                                onCommentClick = { threadId ->
                                    viewModel.openComments(threadId)
                                },
                                navigateToProfile = { authorId ->
                                    if (authorId.isNotEmpty()) {
                                        navigateToProfile(authorId)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

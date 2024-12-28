package com.work.campvoiceus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.work.campvoiceus.ui.components.ThreadCard
import com.work.campvoiceus.viewmodels.FileDownloadViewModel
import com.work.campvoiceus.viewmodels.ThreadsViewModel
import com.work.campvoiceus.viewmodels.VoterListViewModel

import androidx.compose.runtime.getValue

@Composable
fun HomeScreen(
    viewModel: ThreadsViewModel,
    voterListViewModel: VoterListViewModel,
    fileDownloadViewModel: FileDownloadViewModel,
    navigateToProfile: (String) -> Unit,
    navigateToThread: (String) -> Unit,
    navigateToTag: (String) -> Unit,
) {
    val threads by viewModel.threads.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var isFabLoading by remember { mutableStateOf(false) } // Loading state for FAB

    // Load threads on first launch
    LaunchedEffect(Unit) {
        if (threads.isEmpty()) {
            viewModel.fetchThreads()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                isLoading && threads.isEmpty() -> {
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
                            voterListViewModel = voterListViewModel,
                            fileDownloadViewModel = fileDownloadViewModel
                        )
                    }
                }
            }
        }

        // Floating Action Button for manual refresh
        FloatingActionButton(
            onClick = {
                isFabLoading = true // Set loading state
                viewModel.fetchThreads() // Trigger data fetch
                isFabLoading = false // Reset loading state
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            if (isFabLoading || isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Threads"
                )
            }
        }

        // SnackbarHost to display snackbar messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Handle errors via Snackbar
        if (!errorMessage.isNullOrEmpty()) {
            LaunchedEffect(errorMessage) {
                snackbarHostState.showSnackbar(
                    message = errorMessage ?: "Unknown error",
                    actionLabel = "Dismiss",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearErrorMessage()
            }
        }
    }
}




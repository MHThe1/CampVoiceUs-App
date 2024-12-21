package com.work.campvoiceus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error: $errorMessage", color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchThreads() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            threads.isEmpty() -> {
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

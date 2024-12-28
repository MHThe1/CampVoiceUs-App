package com.work.campvoiceus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.work.campvoiceus.ui.components.ThreadCard
import com.work.campvoiceus.viewmodels.FileDownloadViewModel
import com.work.campvoiceus.viewmodels.ThreadsByTagViewModel
import com.work.campvoiceus.viewmodels.VoterListViewModel


@Composable
fun TagThreadsScreen(
    tag: String,
    viewModel: ThreadsByTagViewModel,
    voterListViewModel: VoterListViewModel,
    fileDownloadViewModel: FileDownloadViewModel,
    navigateToThread: (String) -> Unit,
    navigateToProfile: (String) -> Unit,
    navigateToTag: (String) -> Unit
) {
    val threads by viewModel.threads.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                !errorMessage.isNullOrEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                threads.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No threads found for #$tag")
                    }
                }
                else -> {
                    LazyColumn {
                        items(threads) { thread ->
                            ThreadCard(
                                thread = thread,
                                currentUserId = viewModel.currentUserId.value ?: "",
                                onVote = { threadId, voteType ->
                                    viewModel.handleVote(threadId, voteType)
                                },
                                navigateToThread = { threadId ->
                                    navigateToThread(threadId)
                                },
                                navigateToProfile = { authorId ->
                                    navigateToProfile(authorId)
                                },
                                navigateToTag = { newTag ->
                                    navigateToTag(newTag)
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
}

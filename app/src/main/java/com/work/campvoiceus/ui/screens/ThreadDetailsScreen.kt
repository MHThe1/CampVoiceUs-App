package com.work.campvoiceus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.work.campvoiceus.ui.components.CommentCard
import com.work.campvoiceus.ui.components.ThreadCard
import com.work.campvoiceus.viewmodels.CommentsViewModel
import com.work.campvoiceus.viewmodels.FileDownloadViewModel
import com.work.campvoiceus.viewmodels.VoterListViewModel


@Composable
fun ThreadDetailsScreen(
    commentsViewModel: CommentsViewModel,
    voterListViewModel: VoterListViewModel,
    fileDownloadViewModel: FileDownloadViewModel,
    navigateToProfile: (String) -> Unit,
    navigateToThread: (String) -> Unit,
    navigateToTag: (String) -> Unit
) {
    val thread by commentsViewModel.thread.collectAsState()
    val comments by commentsViewModel.comments.collectAsState(initial = emptyList())
    val isLoading by commentsViewModel.isLoading.collectAsState()
    val errorMessageState by commentsViewModel.errorMessage.collectAsState()
    val currentUserId by commentsViewModel.currentUserId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observe error messages and update the local state
    LaunchedEffect(errorMessageState) {
        errorMessage = errorMessageState
    }

    // Display the snackbar if there's an error message
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
            commentsViewModel.clearErrorMessage()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(top = 8.dp, start = 3.dp, end = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (thread == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Thread not found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    // Thread Card
                    item {
                        ThreadCard(
                            thread = thread!!,
                            currentUserId = currentUserId ?: "",
                            onVote = { threadId, voteType ->
                                commentsViewModel.handleVote(threadId, voteType)
                            },
                            navigateToProfile = navigateToProfile,
                            navigateToThread = navigateToThread,
                            navigateToTag = navigateToTag,
                            voterListViewModel = voterListViewModel,
                            fileDownloadViewModel = fileDownloadViewModel
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Comments Section
                    items(comments) { comment ->
                        CommentCard(
                            comment = comment,
                            currentUserId = currentUserId ?: "",
                            onVote = { commentId, voteType ->
                                commentsViewModel.handleCommentVote(commentId, voteType)
                            },
                            voterListViewModel = voterListViewModel
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // Comment Input Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var newCommentText by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    label = { Text("Add a comment") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                commentsViewModel.addComment(newCommentText)
                                newCommentText = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowCircleRight,
                            contentDescription = "Post Comment",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            snackbar = { snackbarData ->
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = {
                        TextButton(onClick = { snackbarData.dismiss() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(snackbarData.visuals.message)
                }
            }
        )


    }
}




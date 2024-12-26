package com.work.campvoiceus.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.work.campvoiceus.models.ThreadModel
import com.work.campvoiceus.models.CommentModel
import com.work.campvoiceus.ui.components.ThreadCard
import com.work.campvoiceus.viewmodels.CommentsViewModel
import com.work.campvoiceus.viewmodels.VoterListViewModel


// ThreadDetailsScreen.kt
@Composable
fun ThreadDetailsScreen(
    commentsViewModel: CommentsViewModel,
    voterListViewModel: VoterListViewModel,
    navigateToProfile: (String) -> Unit,
    navigateToThread: (String) -> Unit
) {
    val thread by commentsViewModel.thread.collectAsState()
    val comments by commentsViewModel.comments.collectAsState(initial = emptyList())
    val isLoading by commentsViewModel.isLoading.collectAsState()
    val errorMessage by commentsViewModel.errorMessage.collectAsState()
    val currentUserId by commentsViewModel.currentUserId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

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
        } else if (thread == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Thread not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Thread Card
                ThreadCard(
                    thread = thread!!,
                    currentUserId = currentUserId ?: "",
                    onVote = { threadId, voteType ->
                        commentsViewModel.handleVote(threadId, voteType)
                    },
                    navigateToProfile = navigateToProfile,
                    navigateToThread = navigateToThread,
                    voterListViewModel = voterListViewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Comments Section
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(comments) { comment ->
                        CommentCard(
                            comment = comment,
                            currentUserId = currentUserId ?: "",
                            onVote = { commentId, voteType ->
                                commentsViewModel.handleCommentVote(commentId, voteType)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Add Comment Section
                var newCommentText by remember { mutableStateOf("") }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        label = { Text("Add a comment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                commentsViewModel.addComment(newCommentText)
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Post Comment")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CommentCard(
    comment: CommentModel,
    currentUserId: String,
    onVote: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = comment.avatarUrl ?: "default_avatar_url"
                    ),
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = comment.name ?: "Unknown User",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "@${comment.userName ?: "unknown"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onVote(comment.commentId, "upvote") }) {
                    Icon(
                        imageVector = Icons.Default.ArrowCircleUp,
                        contentDescription = "Upvote",
                        tint = if (comment.upvotes.contains(currentUserId)) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                Text("${comment.upvotes.size}")

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = { onVote(comment.commentId, "downvote") }) {
                    Icon(
                        imageVector = Icons.Default.ArrowCircleDown,
                        contentDescription = "Downvote",
                        tint = if (comment.downvotes.contains(currentUserId)) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                Text("${comment.downvotes.size}")
            }
        }
    }
}

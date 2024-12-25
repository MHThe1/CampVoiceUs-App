package com.work.campvoiceus.ui.components

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
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.work.campvoiceus.viewmodels.CommentsViewModel
import com.work.campvoiceus.viewmodels.Voter
import com.work.campvoiceus.viewmodels.VoterListViewModel

@Composable
fun CommentsModal(
    threadId: String,
    commentsViewModel: CommentsViewModel,
    onDismiss: () -> Unit
) {
    val comments by commentsViewModel.comments.collectAsState()
    val isLoading by commentsViewModel.isLoading.collectAsState()
    val errorMessage by commentsViewModel.errorMessage.collectAsState()

    var newComment by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        commentsViewModel.fetchComments(threadId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comments") },
        text = {
            Column {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "An error occurred",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyColumn {
                        items(comments) { comment ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = comment.avatarUrl
                                            ?: "https://res.cloudinary.com/deickev8a/image/upload/v1734704007/profile_images/placeholder_dp.png"
                                    ),
                                    contentDescription = "${comment.userName ?: "Unknown User"}'s Avatar",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = comment.userName ?: "Unknown User",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = comment.content,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        label = { Text("Add a comment") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newComment.isNotBlank()) {
                                commentsViewModel.addComment(threadId, newComment)
                                newComment = ""
                            }
                        }
                    ) {
                        Text("Post")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun VotingModal(
    votes: List<String>,
    voterListViewModel: VoterListViewModel,
    onDismiss: () -> Unit
) {
    val voters = remember { mutableStateOf<List<Voter>>(emptyList()) }

    LaunchedEffect(votes) {
        voterListViewModel.fetchVoters(votes) { fetchedVoters ->
            voters.value = fetchedVoters
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Voters") },
        text = {
            LazyColumn {
                items(voters.value) { voter ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = voter.avatarUrl
                                    ?: "https://res.cloudinary.com/deickev8a/image/upload/v1734704007/profile_images/placeholder_dp.png"
                            ),
                            contentDescription = voter.name ?: "Unknown",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = voter.name ?: "Unknown User",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


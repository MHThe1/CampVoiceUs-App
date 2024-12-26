package com.work.campvoiceus.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.work.campvoiceus.models.ThreadModel
import com.work.campvoiceus.viewmodels.CommentsViewModel
import com.work.campvoiceus.viewmodels.Voter
import com.work.campvoiceus.viewmodels.VoterListViewModel

@Composable
fun CommentsModal(
    threadId: String,
    commentsViewModel: CommentsViewModel,
    threadContent: String, // Added thread content to display
    voterListViewModel: VoterListViewModel,
    onDismiss: () -> Unit
){}
//{
//    val comments by commentsViewModel.comments.collectAsState()
//    val isLoading by commentsViewModel.isLoading.collectAsState()
//    val errorMessage by commentsViewModel.errorMessage.collectAsState()
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Comments") },
//        text = {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(8.dp)
//            ) {
//                // Show the thread content at the top
//                Text(
//                    text = threadContent,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface,
//                    modifier = Modifier.padding(bottom = 16.dp)
//                )
//
//                if (isLoading) {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                } else if (errorMessage != null) {
//                    Text(
//                        text = errorMessage ?: "An error occurred",
//                        color = MaterialTheme.colorScheme.error
//                    )
//                } else {
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        items(comments) { comment ->
//                            ThreadCard(
//                                thread = ThreadModel(
//                                    _id = comment.commentId,
//                                    title = "", // Comments don't have a title
//                                    content = comment.content,
//                                    authorId = comment.userId,
//                                    authorName = comment.userName,
//                                    authorUsername = null,
//                                    authorAvatarUrl = comment.avatarUrl,
//                                    comments = emptyList(), // Comments don't have nested comments
//                                    upvotes = comment.upvotes,
//                                    downvotes = comment.downvotes,
//                                    createdAt = comment.createdAt,
//                                    __v = 0
//                                ),
//                                currentUserId = commentsViewModel.getCurrentUserId(),
//                                onVote = { commentId, voteType ->
//                                    commentsViewModel.handleVote(commentId, voteType)
//                                },
//                                navigateToThread = {},
//                                navigateToProfile = {}, // Handle navigation if needed
//                                voterListViewModel = voterListViewModel,
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                        }
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            Button(onClick = onDismiss) {
//                Text("Close")
//            }
//        }
//    )
//}



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


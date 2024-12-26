package com.work.campvoiceus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.work.campvoiceus.models.CommentModel
import com.work.campvoiceus.viewmodels.Voter
import com.work.campvoiceus.viewmodels.VoterListViewModel

@Composable
fun CommentCard(
    comment: CommentModel,
    currentUserId: String,
    onVote: (String, String) -> Unit,
    voterListViewModel: VoterListViewModel
) {
    val (isVoterModalOpen, setVoterModalOpen) = remember { mutableStateOf(false) }
    val (voterType, setVoterType) = remember { mutableStateOf("") }
    val voters = remember { mutableStateOf<List<Voter>>(emptyList()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = comment.avatarUrl
                        ?: "https://res.cloudinary.com/deickev8a/image/upload/v1734704007/profile_images/placeholder_dp.png",
                    contentDescription = "Author Avatar",
                    contentScale = ContentScale.Crop,
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
                Spacer(modifier = Modifier.weight(1f))
                TimeDisplay(comment.createdAt)
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
                Text(
                    text = "${comment.upvotes.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        setVoterType("upvote")
                        setVoterModalOpen(true)
                        voterListViewModel.fetchVoters(comment.upvotes) { fetchedVoters ->
                            voters.value = fetchedVoters
                        }
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = { onVote(comment.commentId, "downvote") }) {
                    Icon(
                        imageVector = Icons.Default.ArrowCircleDown,
                        contentDescription = "Downvote",
                        tint = if (comment.downvotes.contains(currentUserId)) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                Text(
                    text = "${comment.downvotes.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        setVoterType("downvote")
                        setVoterModalOpen(true)
                        voterListViewModel.fetchVoters(comment.downvotes) { fetchedVoters ->
                            voters.value = fetchedVoters
                        }
                    }
                )
            }
        }

        if (isVoterModalOpen) {
            VoterListModal(
                title = if (voterType == "upvote") "Upvoters" else "Downvoters",
                voters = voters.value,
                onDismiss = { setVoterModalOpen(false) }
            )
        }
    }
}

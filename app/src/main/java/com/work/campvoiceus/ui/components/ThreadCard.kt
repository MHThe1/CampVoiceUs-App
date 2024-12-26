package com.work.campvoiceus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ModeComment
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
import com.work.campvoiceus.models.ThreadModel
import com.work.campvoiceus.viewmodels.Voter
import com.work.campvoiceus.viewmodels.VoterListViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ThreadCard(
    thread: ThreadModel,
    currentUserId: String,
    onVote: (String, String) -> Unit,
    navigateToThread: (String) -> Unit, // Navigate to ThreadDetailsScreen
    navigateToProfile: (String) -> Unit,
    voterListViewModel: VoterListViewModel,
) {
    val (isVoterModalOpen, setVoterModalOpen) = remember { mutableStateOf(false) }
    val (voterType, setVoterType) = remember { mutableStateOf("") }
    val voters = remember { mutableStateOf<List<Voter>>(emptyList()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable(enabled = thread.authorId.isNotEmpty()) {
                        navigateToProfile(thread.authorId)
                    }
            ) {
                AsyncImage(
                    model = thread.authorAvatarUrl
                        ?: "https://res.cloudinary.com/deickev8a/image/upload/v1734704007/profile_images/placeholder_dp.png",
                    contentDescription = "Author Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = thread.authorName ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@${thread.authorUsername ?: "unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TimeDisplay(thread.createdAt)
            }

            Text(
                text = thread.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = thread.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onVote(thread._id, "upvote") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowCircleUp,
                            contentDescription = "Upvote",
                            tint = if (thread.upvotes.contains(currentUserId))
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${thread.upvotes.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable {
                            setVoterType("upvote")
                            setVoterModalOpen(true)
                            voterListViewModel.fetchVoters(thread.upvotes) { fetchedVoters ->
                                voters.value = fetchedVoters
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(onClick = { onVote(thread._id, "downvote") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowCircleDown,
                            contentDescription = "Downvote",
                            tint = if (thread.downvotes.contains(currentUserId))
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${thread.downvotes.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable {
                            setVoterType("downvote")
                            setVoterModalOpen(true)
                            voterListViewModel.fetchVoters(thread.downvotes) { fetchedVoters ->
                                voters.value = fetchedVoters
                            }
                        }
                    )
                }

                IconButton(onClick = { navigateToThread(thread._id) }) { // Navigate to ThreadDetailsScreen
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${thread.comments.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ModeComment,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Voter List Modal
        if (isVoterModalOpen) {
            VoterListModal(
                title = if (voterType == "upvote") "Upvoters" else "Downvoters",
                voters = voters.value,
                onDismiss = { setVoterModalOpen(false) }
            )
        }
    }
}


@Composable
fun VoterListModal(
    title: String,
    voters: List<Voter>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                voters.forEach { voter ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        AsyncImage(
                            model = if (voter.avatarUrl.isEmpty()) {
                                "https://res.cloudinary.com/deickev8a/image/upload/v1734704007/profile_images/placeholder_dp.png"
                            } else {
                                voter.avatarUrl
                            },
                            contentDescription = "${voter.name}'s Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = voter.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "@${voter.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text("Close")
            }
        }
    )
}




@Composable
fun TimeDisplay(createdAt: String) {
    val time = formatTime(createdAt)
    Text(
        text = time,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

fun formatTime(createdAt: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC") // Ensure parsing uses UTC
        val date = format.parse(createdAt) ?: Date()

        val now = Date()
        val diffMs = now.time - date.time

        val diffMinutes = (diffMs / (1000 * 60)) % 60
        val diffHours = (diffMs / (1000 * 60 * 60)) % 24
        val diffDays = (diffMs / (1000 * 60 * 60 * 24))

        when {
            diffMinutes < 1 -> "Just now"
            diffHours < 1 -> "$diffMinutes min ago"
            diffDays < 1 -> "$diffHours hours ago"
            diffDays in 1..30 -> "$diffDays days ago"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        "Unknown time"
    }
}

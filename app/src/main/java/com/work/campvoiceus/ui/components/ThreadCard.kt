package com.work.campvoiceus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.work.campvoiceus.models.ThreadModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ThreadCard(
    thread: ThreadModel,
    currentUserId: String,
    onVote: (String, String) -> Unit,
    onCommentClick: (String) -> Unit,
    navigateToProfile: (String) -> Unit
) {
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
                    .clickable(enabled = !thread.authorId.isNullOrEmpty()) {
                        thread.authorId?.let { navigateToProfile(it) }
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
                // Author Info
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

                // Time Display
                TimeDisplay(thread.createdAt)
            }


            // Thread Title
            Text(
                text = thread.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Thread Content
            Text(
                text = thread.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Voting and Comments
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Upvote Button
                    IconButton(onClick = { onVote(thread._id, "upvote") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Upvote",
                            tint = if (thread.upvotes.contains(currentUserId)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${thread.upvotes.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Downvote Button
                    IconButton(onClick = { onVote(thread._id, "downvote") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Downvote",
                            tint = if (thread.downvotes.contains(currentUserId)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${thread.downvotes.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Comments Button
                IconButton(onClick = { onCommentClick(thread._id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${thread.comments.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
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

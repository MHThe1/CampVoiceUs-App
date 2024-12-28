package com.work.campvoiceus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExposureNeg1
import androidx.compose.material.icons.filled.ExposurePlus1
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.work.campvoiceus.models.NotificationModel
import com.work.campvoiceus.viewmodels.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.ranges.contains

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel(),
    navigateToThread: (String) -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var isFabLoading by remember { mutableStateOf(false) }

    // Load notifications in the background if no cached data
    LaunchedEffect(Unit) {
        if (notifications.isEmpty()) {
            viewModel.fetchNotifications()
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
                isLoading && notifications.isEmpty() -> {
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
                notifications.isEmpty() && errorMessage == null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No notifications available.")
                        }
                    }
                }
                else -> {
                    items(notifications) { notification ->
                        NotificationCard(notification, navigateToThread)
                    }
                }
            }
        }

        // Floating Action Button for refresh
        FloatingActionButton(
            onClick = {
                isFabLoading = true
                viewModel.fetchNotifications()
                isFabLoading = false
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
                    contentDescription = "Refresh Notifications"
                )
            }
        }
    }
}



@Composable
fun NotificationCard(
    notification: NotificationModel,
    navigateToThread: (String) -> Unit
) {
    val icon = getNotificationIcon(notification.title)
    val formattedTime = formatTime(notification.createdAt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { notification.threadId?.let { navigateToThread(it) } },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RectangleShape
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Icon based on notification type
            Icon(
                imageVector = icon,
                contentDescription = "Notification Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Notification details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun getNotificationIcon(title: String): ImageVector {
    return when {
        title.contains("upvoted", ignoreCase = true) -> Icons.Default.ExposurePlus1
        title.contains("downvoted", ignoreCase = true) -> Icons.Default.ExposureNeg1
        title.contains("New comment", ignoreCase = true) -> Icons.Default.ModeComment
        else -> Icons.Default.Notifications
    }
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

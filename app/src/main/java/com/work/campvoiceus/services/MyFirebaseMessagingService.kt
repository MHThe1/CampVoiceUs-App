package com.work.campvoiceus.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.work.campvoiceus.MainActivity
import com.work.campvoiceus.R
import com.work.campvoiceus.utils.TokenManager

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check for notification data
        val threadId = remoteMessage.data["threadId"]

        // If threadId exists, show a notification that navigates to threadDetails/{threadId}
        if (threadId != null) {
            showNotification(remoteMessage.notification?.title, remoteMessage.notification?.body, threadId)
        }
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM", "New FCM Token: $token")

        // Save the new token
        val tokenManager = TokenManager(applicationContext)
        tokenManager.saveFcmToken(token)

        // Optionally send the token to the backend
        sendTokenToBackend(token)
    }

    private fun sendTokenToBackend(token: String) {
        // Implement API call to send the token to your backend
        val jwtToken = TokenManager(applicationContext).getToken()
        Log.d("Backend", "Sending FCM Token to backend: $token")
        // Use Retrofit, Volley, or other networking library to send this to your backend
    }

    private fun showNotification(title: String?, message: String?, threadId: String) {
        val channelId = "default_channel_id"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0+ (Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Default Channel", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent to launch MainActivity and pass threadId
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("threadId", threadId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and show the notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(0, notification)
    }

}

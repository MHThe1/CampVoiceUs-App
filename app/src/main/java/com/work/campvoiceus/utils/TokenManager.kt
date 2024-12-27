package com.work.campvoiceus.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

class TokenManager(private val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Save JWT token
    fun saveToken(token: String) {
        preferences.edit().putString("token", token).apply()
    }

    // Retrieve JWT token
    fun getToken(): String? {
        return preferences.getString("token", null)
    }

    // Clear JWT token
    fun clearToken() {
        preferences.edit().remove("token").apply()
    }

    // Save FCM token
    fun saveFcmToken(fcmToken: String) {
        preferences.edit().putString("fcm_token", fcmToken).apply()
    }

    // Retrieve FCM token
    fun getFcmToken(): String? {
        return preferences.getString("fcm_token", null)
    }

    // Fetch FCM token from Firebase and save it
    fun fetchAndSaveFcmToken(onTokenFetched: (String) -> Unit = {}) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM", "FCM Token fetched: $fcmToken")
                saveFcmToken(fcmToken)
                onTokenFetched(fcmToken)
            } else {
                Log.e("FCM", "Fetching FCM Token failed", task.exception)
            }
        }
    }
}

package com.work.campvoiceus.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(private val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        preferences.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return preferences.getString("token", null)
    }

    fun clearToken() {
        preferences.edit().remove("token").apply()
    }
}

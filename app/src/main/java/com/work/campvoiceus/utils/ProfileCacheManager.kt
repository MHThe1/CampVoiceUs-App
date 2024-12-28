package com.work.campvoiceus.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.work.campvoiceus.models.User

class ProfileCacheManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveProfile(user: User) {
        val json = gson.toJson(user)
        preferences.edit().putString("cached_profile", json).apply()
    }

    fun getProfile(): User? {
        val json = preferences.getString("cached_profile", null) ?: return null
        return gson.fromJson(json, User::class.java)
    }

    fun clearProfile() {
        preferences.edit().remove("cached_profile").apply()
    }
}

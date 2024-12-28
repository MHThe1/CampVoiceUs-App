package com.work.campvoiceus.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.work.campvoiceus.models.ThreadModel

class UserThreadCacheManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("user_thread_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUserThreads(userThreads: List<ThreadModel>) {
        val json = gson.toJson(userThreads)
        preferences.edit().putString("cached_user_threads", json).apply()
    }

    fun getUserThreads(): List<ThreadModel> {
        val json = preferences.getString("cached_user_threads", null) ?: return emptyList()
        val type = object : TypeToken<List<ThreadModel>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearUserThreads() {
        preferences.edit().remove("cached_user_threads").apply()
    }
}

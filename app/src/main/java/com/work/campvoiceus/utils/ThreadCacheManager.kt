package com.work.campvoiceus.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.work.campvoiceus.models.ThreadModel

class ThreadCacheManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("thread_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveThreads(threads: List<ThreadModel>) {
        val json = gson.toJson(threads)
        preferences.edit().putString("cached_threads", json).apply()
    }

    fun getThreads(): List<ThreadModel> {
        val json = preferences.getString("cached_threads", null) ?: return emptyList()
        val type = object : TypeToken<List<ThreadModel>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearThreads() {
        preferences.edit().remove("cached_threads").apply()
    }
}

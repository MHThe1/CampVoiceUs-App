package com.work.campvoiceus.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.work.campvoiceus.models.NotificationModel

class NotificationCacheManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveNotifications(notifications: List<NotificationModel>) {
        val json = gson.toJson(notifications)
        preferences.edit().putString("cached_notifications", json).apply()
    }

    fun getNotifications(): List<NotificationModel> {
        val json = preferences.getString("cached_notifications", null) ?: return emptyList()
        val type = object : TypeToken<List<NotificationModel>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearNotifications() {
        preferences.edit().remove("cached_notifications").apply()
    }
}

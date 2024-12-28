package com.work.campvoiceus.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.NotificationModel
import com.work.campvoiceus.network.RetrofitInstance.userService
import com.work.campvoiceus.utils.NotificationCacheManager
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val tokenManager: TokenManager,
    context: Context
) : ViewModel() {

    private val notificationCacheManager = NotificationCacheManager(context)

    private val _notifications = MutableStateFlow<List<NotificationModel>>(notificationCacheManager.getNotifications()) // Load from cache
    val notifications: StateFlow<List<NotificationModel>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchNotifications() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token missing. Please log in again."
                    _isLoading.value = false
                    return@launch
                }

                val response = userService.getNotifications("Bearer $token")
                if (response.isSuccessful) {
                    val notifications = response.body()?.notifications ?: emptyList()
                    _notifications.value = notifications

                    // Save notifications to local storage
                    notificationCacheManager.saveNotifications(notifications)
                } else {
                    _errorMessage.value = "Failed to fetch notifications: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCachedNotifications() {
        notificationCacheManager.clearNotifications()
    }
}

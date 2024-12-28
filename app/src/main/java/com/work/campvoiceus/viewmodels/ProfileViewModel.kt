package com.work.campvoiceus.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.User
import com.work.campvoiceus.network.RetrofitInstance.userService
import com.work.campvoiceus.utils.ProfileCacheManager
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val tokenManager: TokenManager,
    context: Context
) : ViewModel() {

    private val profileCacheManager = ProfileCacheManager(context)

    private val _user = MutableStateFlow<User?>(profileCacheManager.getProfile()) // Load from cache
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val response = userService.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    val fetchedUser = response.body()
                    _user.value = fetchedUser

                    // Save profile to local storage
                    fetchedUser?.let { profileCacheManager.saveProfile(it) }
                } else {
                    _errorMessage.value = "Failed to fetch profile: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCachedProfile() {
        profileCacheManager.clearProfile()
    }
}


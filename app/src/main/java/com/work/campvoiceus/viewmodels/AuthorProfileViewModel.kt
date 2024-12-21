package com.work.campvoiceus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.User
import com.work.campvoiceus.network.RetrofitInstance.userService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthorProfileViewModel(
    private val tokenManager: TokenManager,
    private val userId: String
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token not found. Please log in again."
                    return@launch
                }

                val response = userService.getUserById("Bearer $token", mapOf("id" to userId))
                if (response.isSuccessful) {
                    _user.value = response.body()
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
}

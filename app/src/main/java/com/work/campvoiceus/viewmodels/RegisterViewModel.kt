package com.work.campvoiceus.viewmodels

import androidx.lifecycle.ViewModel
import com.work.campvoiceus.models.RegisterRequest
import com.work.campvoiceus.network.RetrofitInstance
import com.work.campvoiceus.network.RetrofitInstance.userService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    suspend fun register(name: String, username: String, email: String, password: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = ""

        return try {
            val response = RetrofitInstance.userService.register(
                RegisterRequest(name, username, email, password)
            )
            if (response.isSuccessful) {
                true
            } else {
                _errorMessage.value = "Registration failed: ${response.message()}"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = "An error occurred: ${e.localizedMessage}"
            false
        } finally {
            _isLoading.value = false
        }
    }
}

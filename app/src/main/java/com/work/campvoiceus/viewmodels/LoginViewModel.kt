package com.work.campvoiceus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.LoginRequest
import com.work.campvoiceus.network.RetrofitInstance
import com.work.campvoiceus.network.UserService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(private val tokenManager: TokenManager) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val userService: UserService = RetrofitInstance.userService

    fun login(identifier: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                // Perform login
                val response = userService.loginUser(LoginRequest(identifier, password))
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        tokenManager.saveToken(loginResponse.token) // Save JWT token locally

                        // Fetch and update FCM token
                        tokenManager.fetchAndSaveFcmToken { fcmToken ->
                            updateFcmTokenOnBackend(loginResponse.token, fcmToken)
                        }

                        _loginState.value = LoginState.Success(loginResponse.token)
                    } else {
                        _loginState.value = LoginState.Error("Unexpected response from the server.")
                    }
                } else {
                    _loginState.value = LoginState.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.localizedMessage ?: "Unknown error occurred.")
            }
        }
    }

    private fun updateFcmTokenOnBackend(jwtToken: String, fcmToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userService.updateFcmToken("Bearer $jwtToken", mapOf("fcmToken" to fcmToken))
                if (!response.isSuccessful) {
                    throw HttpException(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Log or handle the error
                    e.localizedMessage?.let { message -> println("Failed to update FCM token: $message") }
                }
            }
        }
    }
}

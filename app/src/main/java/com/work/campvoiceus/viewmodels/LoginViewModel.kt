package com.work.campvoiceus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.LoginRequest
import com.work.campvoiceus.network.RetrofitInstance
import com.work.campvoiceus.network.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val userService: UserService = RetrofitInstance.userService

    fun login(identifier: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val response = userService.loginUser(LoginRequest(identifier, password))
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
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
}

package com.work.campvoiceus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.CreateThreadRequest
import com.work.campvoiceus.models.ThreadModel
import com.work.campvoiceus.network.RetrofitInstance.threadService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val tokenManager: TokenManager) : ViewModel() {
    private val _threads = MutableStateFlow<List<ThreadModel>>(emptyList())
    val threads: StateFlow<List<ThreadModel>> = _threads

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchThreads()
    }

    fun fetchThreads() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val response = threadService.getThreads("Bearer $token")
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        _threads.value = apiResponse.data
                    } else {
                        _errorMessage.value = "Failed to load threads. Try again later."
                    }
                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error. Please check your connection."
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun createThread(title: String, content: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val response = threadService.createThread("Bearer $token", CreateThreadRequest(title, content))
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        fetchThreads() // Refresh the threads list
                    } else {
                        _errorMessage.value = "Failed to create thread. Try again later."
                    }
                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error. Please check your connection."
            } finally {
                _isLoading.value = false
            }
        }
    }

}

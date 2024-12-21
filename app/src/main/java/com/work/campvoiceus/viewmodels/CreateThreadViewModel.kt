package com.work.campvoiceus.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.network.RetrofitInstance.threadService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class CreateThreadViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    fun createThread(title: String, content: String, context: Context) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token not found. Please login again."
                    _isLoading.value = false
                    return@launch
                }

                val titleRequestBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = threadService.createThread(
                    token = "Bearer $token",
                    title = titleRequestBody,
                    content = contentRequestBody
                )

                if (response.isSuccessful) {
                    _isSuccess.value = true
                    Toast.makeText(context, "Thread created successfully", Toast.LENGTH_SHORT).show()
                } else {
                    _errorMessage.value = "Failed to create thread: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}

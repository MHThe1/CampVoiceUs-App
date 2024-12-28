package com.work.campvoiceus.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.network.RetrofitInstance.threadService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class CreateThreadViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    fun createThread(title: String, content: String, tags: String, fileUri: Uri?, context: Context) {
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
                val tagsRequestBody = tags.takeIf { it.isNotBlank() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val contentResolver = context.contentResolver
                val filePart = fileUri?.let { uri ->
                    val fileType = contentResolver.getType(uri) ?: ""
                    val cursor = contentResolver.query(uri, null, null, null, null)
                    val fileSize = cursor?.use {
                        if (it.moveToFirst()) it.getLong(it.getColumnIndexOrThrow(OpenableColumns.SIZE)) else -1
                    } ?: -1

                    // Validate file type and size
                    if (!(listOf("image/", "video/", "application/zip").any { fileType.startsWith(it) } && fileSize <= 10 * 1024 * 1024)) {
                        _errorMessage.value = "Invalid file. Only images, videos, or ZIP files under 10 MB are allowed."
                        return@launch
                    }

                    val inputStream = contentResolver.openInputStream(uri)
                    val tempFile = File.createTempFile("thread_file", null, context.cacheDir)
                    inputStream?.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        else "unknown_file"
                    } ?: tempFile.name

                    MultipartBody.Part.createFormData(
                        name = "file",
                        filename = fileName,
                        body = tempFile.asRequestBody(fileType.toMediaTypeOrNull())
                    )
                }

                val response = threadService.createThread(
                    token = "Bearer $token",
                    title = titleRequestBody,
                    content = contentRequestBody,
                    tags = tagsRequestBody,
                    file = filePart
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

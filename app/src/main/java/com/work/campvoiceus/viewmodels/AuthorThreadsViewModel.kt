package com.work.campvoiceus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.ThreadModel
import com.work.campvoiceus.network.RetrofitInstance.threadService
import com.work.campvoiceus.network.RetrofitInstance.userService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AuthorThreadsViewModel(
    private val tokenManager: TokenManager,
    val authorId: String
) : ViewModel() {

    private val _userThreads = MutableStateFlow<List<ThreadModel>>(emptyList())
    val userThreads: StateFlow<List<ThreadModel>> = _userThreads

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val response = userService.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    _currentUserId.value = response.body()?._id
                    fetchThisUserThreads()
                } else {
                    _errorMessage.value = "Failed to fetch user ID"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching user ID: ${e.localizedMessage}"
            }
        }
    }


    fun fetchThisUserThreads() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val userId = authorId

                val threadsResponse = threadService.getUserThreads(userId, "Bearer $token")
                if (threadsResponse.isSuccessful) {
                    val userThreads = threadsResponse.body() ?: emptyList()
                    val threadsWithAuthorInfo = userThreads.map { thread ->
                        val authorResponse = userService.getUserById("Bearer $token", mapOf("id" to thread.authorId))
                        if (authorResponse.isSuccessful) {
                            val authorInfo = authorResponse.body()
                            thread.copy(
                                authorName = authorInfo?.name ?: "Unknown",
                                authorUsername = authorInfo?.username ?: "unknown",
                                authorAvatarUrl = authorInfo?.avatarUrl
                            )
                        } else {
                            thread
                        }
                    }
                    _userThreads.value = threadsWithAuthorInfo
                } else {
                    _errorMessage.value = "Failed to fetch threads: ${threadsResponse.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching user threads: ${e.localizedMessage}"
            } finally {
//                _isLoading.value = false
            }
        }
    }


    fun handleVote(threadId: String, voteType: String){
        // Implement voting logic here
    }

    fun openComments(threadId: String){
        // Implement comment logic here
    }
}


package com.work.campvoiceus.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.ThreadModel
import com.work.campvoiceus.network.RetrofitInstance.threadService
import com.work.campvoiceus.network.RetrofitInstance.userService
import com.work.campvoiceus.utils.TokenManager
import com.work.campvoiceus.utils.UserThreadCacheManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProfileThreadsViewModel(
    private val tokenManager: TokenManager,
    context: Context
) : ViewModel() {

    private val userThreadCacheManager = UserThreadCacheManager(context)

    private val _userThreads = MutableStateFlow<List<ThreadModel>>(userThreadCacheManager.getUserThreads()) // Load user's threads from cache
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
                    fetchUserThreads()
                } else {
                    _errorMessage.value = "Failed to fetch user ID"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching user ID: ${e.localizedMessage}"
            }
        }
    }

    fun fetchUserThreads() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val userId = _currentUserId.value
                if (userId != null) {
                    val threadsResponse = threadService.getUserThreads(userId, "Bearer $token")
                    if (threadsResponse.isSuccessful) {
                        val userThreads = threadsResponse.body() ?: emptyList()

                        val authorResponse = userService.getUserProfile("Bearer $token")
                        val authorInfo = authorResponse.body()

                        val threadsWithAuthorInfo = userThreads.map { thread ->
                            thread.copy(
                                authorName = authorInfo?.name ?: "Unknown",
                                authorUsername = authorInfo?.username ?: "unknown",
                                authorAvatarUrl = authorInfo?.avatarUrl
                            )
                        }

                        _userThreads.value = threadsWithAuthorInfo

                        // Cache user's threads
                        userThreadCacheManager.saveUserThreads(threadsWithAuthorInfo)
                    } else {
                        _errorMessage.value = "Failed to fetch threads: ${threadsResponse.message()}"
                    }
                } else {
                    _errorMessage.value = "Current user ID is null"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching user threads: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCachedUserThreads() {
        userThreadCacheManager.clearUserThreads()
    }

    fun handleVote(threadId: String, voteType: String) {
        // Find the thread to be updated
        val threadIndex = _userThreads.value.indexOfFirst { it._id == threadId }
        if (threadIndex == -1) return

        val thread = _userThreads.value[threadIndex]
        val currentUserId = _currentUserId.value ?: return

        // Create a copy of the current threads for reverting in case of failure
        val currentThreads = _userThreads.value.toList()

        // Optimistically update the UI
        val updatedThread = when (voteType) {
            "upvote" -> thread.copy(
                upvotes = if (thread.upvotes.contains(currentUserId)) {
                    thread.upvotes - currentUserId // Remove upvote if already present
                } else {
                    thread.upvotes + currentUserId // Add upvote
                },
                downvotes = thread.downvotes - currentUserId // Remove downvote if present
            )
            "downvote" -> thread.copy(
                downvotes = if (thread.downvotes.contains(currentUserId)) {
                    thread.downvotes - currentUserId // Remove downvote if already present
                } else {
                    thread.downvotes + currentUserId // Add downvote
                },
                upvotes = thread.upvotes - currentUserId // Remove upvote if present
            )
            else -> thread
        }

        _userThreads.value = _userThreads.value.mapIndexed { index, t ->
            if (index == threadIndex) updatedThread else t
        }

        // Send the API request
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val voteData = mapOf("threadId" to threadId)

                val response = if (voteType == "upvote") {
                    threadService.upvote(voteData, "Bearer $token")
                } else {
                    threadService.downvote(voteData, "Bearer $token")
                }

                if (!response.isSuccessful) {
                    throw Exception("Failed to $voteType: ${response.message()}")
                }
            } catch (e: Exception) {
                // Revert to the previous state if the API call fails
                _userThreads.value = currentThreads
                _errorMessage.value = "Error during $voteType: ${e.localizedMessage}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

}


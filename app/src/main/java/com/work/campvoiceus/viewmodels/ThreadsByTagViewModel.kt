package com.work.campvoiceus.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.ThreadModel
import com.work.campvoiceus.network.RetrofitInstance.threadService
import com.work.campvoiceus.network.RetrofitInstance.userService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.map


class ThreadsByTagViewModel(
    private val tokenManager: TokenManager,
    private val tag: String
) : ViewModel() {

    private val _threads = MutableStateFlow<List<ThreadModel>>(emptyList())
    val threads: StateFlow<List<ThreadModel>> = _threads

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
                    fetchThreadsByTag()
                } else {
                    _errorMessage.value = "Failed to fetch user ID"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching user ID: ${e.localizedMessage}"
            }
        }
    }


    fun fetchThreadsByTag() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            Log.d("ThreadsViewModel", "Fetching threads for tag: $tag")
            try {
                val token = tokenManager.getToken()
                Log.d("ThreadsViewModel", "Token: $token")
                val threadsResponse = threadService.getThreadsByTag(tag, "Bearer $token")
                Log.d("ThreadsViewModel", "Threads Response: $threadsResponse")
                if (threadsResponse.isSuccessful) {
                    val responseBody = threadsResponse.body()
                    val threads = responseBody?.threads ?: emptyList()
                    val threadsWithAuthorInfo = threads.map { thread ->
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
                    _threads.value = threadsWithAuthorInfo
                } else {
                    _errorMessage.value = "Failed to fetch threads for tag #$tag: ${threadsResponse.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching threads for tag #$tag: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun handleVote(threadId: String, voteType: String) {
        // Find the thread to be updated
        val threadIndex = _threads.value.indexOfFirst { it._id == threadId }
        if (threadIndex == -1) return

        val thread = _threads.value[threadIndex]
        val currentUserId = _currentUserId.value ?: return

        // Create a copy of the current threads for reverting in case of failure
        val currentThreads = _threads.value.toList()

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

        _threads.value = _threads.value.mapIndexed { index, t ->
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
                _threads.value = currentThreads
                _errorMessage.value = "Error during $voteType: ${e.localizedMessage}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

}


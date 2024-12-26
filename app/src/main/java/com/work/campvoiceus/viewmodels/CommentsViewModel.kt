package com.work.campvoiceus.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.CommentModel
import com.work.campvoiceus.models.ThreadModel
import com.work.campvoiceus.network.UserService
import com.work.campvoiceus.network.ThreadService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentsViewModel(
    private val tokenManager: TokenManager,
    private val userService: UserService,
    private val threadService: ThreadService,
    private val threadId: String
) : ViewModel() {

    private val _thread = MutableStateFlow<ThreadModel?>(null)
    val thread: StateFlow<ThreadModel?> = _thread

    private val _comments = MutableStateFlow<List<CommentModel>>(emptyList())
    val comments: StateFlow<List<CommentModel>> = _comments

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
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token not found. Please log in again."
                    return@launch
                }

                val response = userService.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    _currentUserId.value = response.body()?._id
                    fetchThreadDetails()
                } else {
                    _errorMessage.value = "Failed to fetch user profile"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    private fun fetchThreadDetails() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = threadService.getThreadById(mapOf("id" to threadId), "Bearer $token")

                if (response.isSuccessful) {
                    val threadData = response.body()?.thread
                    if (threadData != null) {
                        _thread.value = threadData
                        fetchComments(threadData.comments)
                    } else {
                        _errorMessage.value = "Thread not found"
                    }
                } else {
                    _errorMessage.value = "Failed to fetch thread details"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchComments(comments: List<CommentModel>) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val enrichedComments = comments.map { comment ->
                    try {
                        val userResponse = userService.getUserById("Bearer $token", mapOf("id" to comment.userId))
                        if (userResponse.isSuccessful) {
                            val user = userResponse.body()
                            comment.copy(
                                userName = user?.username,
                                avatarUrl = user?.avatarUrl,
                                name = user?.name
                            )
                        } else {
                            comment
                        }
                    } catch (e: Exception) {
                        comment
                    }
                }
                _comments.value = enrichedComments
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching comment details: ${e.localizedMessage}"
            }
        }
    }

    fun addComment(content: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = threadService.addComment(
                    mapOf("threadId" to threadId, "content" to content),
                    "Bearer $token"
                )

                if (response.isSuccessful) {
                    fetchThreadDetails()
                } else {
                    _errorMessage.value = "Failed to add comment"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun handleVote(threadId: String, voteType: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = if (voteType == "upvote") {
                    threadService.upvote(mapOf("threadId" to threadId), "Bearer $token")
                } else {
                    threadService.downvote(mapOf("threadId" to threadId), "Bearer $token")
                }

                if (response.isSuccessful) {
                    fetchThreadDetails()
                } else {
                    _errorMessage.value = "Failed to $voteType"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun handleCommentVote(commentId: String, voteType: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = if (voteType == "upvote") {
                    threadService.upvoteComment(mapOf("commentId" to commentId), "Bearer $token")
                } else {
                    threadService.downvoteComment(mapOf("commentId" to commentId), "Bearer $token")
                }

                if (response.isSuccessful) {
                    fetchThreadDetails()
                } else {
                    _errorMessage.value = "Failed to $voteType comment"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
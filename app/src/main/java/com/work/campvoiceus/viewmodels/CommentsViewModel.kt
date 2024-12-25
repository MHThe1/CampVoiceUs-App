package com.work.campvoiceus.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.work.campvoiceus.models.CommentModel
import com.work.campvoiceus.network.UserService
import com.work.campvoiceus.network.ThreadService
import com.work.campvoiceus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentsViewModel(
    private val tokenManager: TokenManager,
    private val userService: UserService,
    private val threadService: ThreadService
) : ViewModel() {

    private val _comments = MutableStateFlow<List<CommentModel>>(emptyList())
    val comments: StateFlow<List<CommentModel>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchComments(threadId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                // Fetch thread details including comments
                val threadResponse = threadService.getThreadById(mapOf("id" to threadId), "Bearer $token")
                if (threadResponse.isSuccessful) {
                    val thread = threadResponse.body()?.thread ?: return@launch
                    val comments = thread.comments

                    // Enrich comments with user information
                    val enrichedComments = comments.map { comment ->
                        val userResponse = userService.getUserById("Bearer $token", mapOf("id" to comment.userId))
                        if (userResponse.isSuccessful) {
                            val user = userResponse.body()
                            comment.copy(
                                userName = user?.username ?: "unknown",
                                avatarUrl = user?.avatarUrl
                                    ?: "https://res.cloudinary.com/deickev8a/image/upload/v1734704007/profile_images/placeholder_dp.png",
                                name = user?.name ?: "Unknown User"
                            )
                        } else {
                            comment
                        }
                    }

                    _comments.value = enrichedComments
                } else {
                    _errorMessage.value = "Failed to fetch comments: ${threadResponse.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching comments: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun addComment(threadId: String, content: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                val response = threadService.addComment(
                    mapOf("threadId" to threadId, "content" to content),
                    "Bearer $token"
                )
                if (response.isSuccessful) {
                    val newComment = response.body()?.comments?.lastOrNull()
                    if (newComment != null) {
                        fetchComments(threadId) // Refresh comments
                    }
                } else {
                    _errorMessage.value = "Failed to add comment: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error adding comment: ${e.localizedMessage}"
            }
        }
    }
}

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
                        val enrichedThread = enrichThreadWithAuthor(threadData, token)
                        val enrichedComments = enrichCommentsWithAuthors(threadData.comments, token)
                        _thread.value = enrichedThread
                        _comments.value = enrichedComments
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

    private suspend fun enrichThreadWithAuthor(thread: ThreadModel, token: String): ThreadModel {
        return try {
            val authorResponse = userService.getUserById("Bearer $token", mapOf("id" to thread.authorId))
            if (authorResponse.isSuccessful) {
                val author = authorResponse.body()
                thread.copy(
                    authorName = author?.name,
                    authorUsername = author?.username,
                    authorAvatarUrl = author?.avatarUrl
                )
            } else thread
        } catch (e: Exception) {
            thread
        }
    }

    private suspend fun enrichCommentsWithAuthors(
        comments: List<CommentModel>,
        token: String
    ): List<CommentModel> {
        return comments.map { comment ->
            try {
                val userResponse = userService.getUserById("Bearer $token", mapOf("id" to comment.userId))
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()
                    comment.copy(
                        name = user?.name,
                        userName = user?.username,
                        avatarUrl = user?.avatarUrl
                    )
                } else comment
            } catch (e: Exception) {
                comment
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

        val thread = _thread.value ?: return
        val currentUserId = _currentUserId.value ?: return
        val currentThread = _thread.value?.copy() ?: return

        val updatedThread = when (voteType) {
            "upvote" -> thread.copy(
                upvotes = if (thread.upvotes.contains(currentUserId)) {
                    thread.upvotes - currentUserId
                } else {
                    thread.upvotes + currentUserId
                },
                downvotes = thread.downvotes - currentUserId
            )
            "downvote" -> thread.copy(
                downvotes = if (thread.downvotes.contains(currentUserId)) {
                    thread.downvotes - currentUserId
                } else {
                    thread.downvotes + currentUserId
                },
                upvotes = thread.upvotes - currentUserId
            )
            else -> thread
        }

        _thread.value = updatedThread

        viewModelScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = if (voteType == "upvote") {
                    threadService.upvote(mapOf("threadId" to threadId), "Bearer $token")
                } else {
                    threadService.downvote(mapOf("threadId" to threadId), "Bearer $token")
                }

                if (!response.isSuccessful) {
                    throw Exception("Failed to $voteType: ${response.message()}")
                }
            } catch (e: Exception) {
                _thread.value = currentThread
                _errorMessage.value = "Error during $voteType: ${e.localizedMessage}"
            }
        }
    }


    fun handleCommentVote(commentId: String, voteType: String) {
        val commentIndex = _comments.value.indexOfFirst { it.commentId == commentId }
        if (commentIndex == -1) return

        val comment = _comments.value[commentIndex]
        val currentUserId = _currentUserId.value ?: return
        val currentComments = _comments.value.toList()

        val updatedComment = when (voteType) {
            "upvote" -> comment.copy(
                upvotes = if (comment.upvotes.contains(currentUserId)) {
                    comment.upvotes - currentUserId
                } else {
                    comment.upvotes + currentUserId
                },
                downvotes = comment.downvotes - currentUserId
            )
            "downvote" -> comment.copy(
                downvotes = if (comment.downvotes.contains(currentUserId)) {
                    comment.downvotes - currentUserId
                } else {
                    comment.downvotes + currentUserId
                },
                upvotes = comment.upvotes - currentUserId
            )
            else -> comment
        }

        _comments.value = _comments.value.mapIndexed { index, c ->
            if (index == commentIndex) updatedComment else c
        }

        viewModelScope.launch {
            try {
                val token = tokenManager.getToken() ?: return@launch
                val response = if (voteType == "upvote") {
                    threadService.upvoteComment(mapOf("threadId" to threadId, "commentId" to commentId), "Bearer $token")
                } else {
                    threadService.downvoteComment(mapOf("threadId" to threadId, "commentId" to commentId), "Bearer $token")
                }

                if (!response.isSuccessful) throw Exception("Failed to $voteType comment: ${response.message()}")
            } catch (e: Exception) {
                _comments.value = currentComments
                _errorMessage.value = "Error during $voteType: ${e.localizedMessage}"
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

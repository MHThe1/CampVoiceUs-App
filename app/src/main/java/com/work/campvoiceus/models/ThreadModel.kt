package com.work.campvoiceus.models

data class ThreadModel(
    val _id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String? = null,
    val authorUsername: String? = null,
    val authorAvatarUrl: String? = null,
    val comments: List<CommentModel>,
    val upvotes: List<String>,
    val downvotes: List<String>,
    val tags: List<String>,
    val createdAt: String,
    val __v: Int
)

data class CommentModel(
    val commentId: String,
    val userId: String,
    val content: String,
    val upvotes: List<String>,
    val downvotes: List<String>,
    val createdAt: String,
    var userName: String? = null,
    var avatarUrl: String? = null,
    var name: String? = null
)


data class ThreadResponse(
    val thread: ThreadModel
)

data class ThreadsByTagResponse(
    val message: String,
    val threads: List<ThreadModel>
)

data class CommentResponse(
    val message: String,
    val updatedComment: CommentModel
)

data class AuthorInfo(
    val name: String,
    val username: String,
    val avatarUrl: String?
)






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
    val createdAt: String,
    val __v: Int
)

data class CommentModel(
    val userId: String,
    val content: String,
    val upvotes: List<String>,
    val downvotes: List<String>,
    val createdAt: String,
    val commentId: String
)

data class AuthorInfo(
    val name: String,
    val username: String,
    val avatarUrl: String?
)

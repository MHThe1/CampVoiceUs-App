package com.work.campvoiceus.models

data class ThreadModel(
    val _id: String,
    val title: String,
    val content: String,
    val authorName: String,
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
    val _id: String,
    val commentId: String
)
package com.work.campvoiceus.models

data class ThreadModel(
    val id: String,
    val title: String,
    val content: String,
    val authorName: String,
    val upvotes: Int,
    val commentsCount: Int,
    val createdAt: String,
    val updatedAt: String
)
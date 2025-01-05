package com.work.campvoiceus.models

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class RegisterRequest(val name: String, val username: String, val email: String, val password: String)
data class RegisterResponse(val message: String)

data class Expertise(
    val name: String,
    val credentialUrl: String,
)

data class User(
    val _id: String,
    val name: String,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val interests: List<String>? = null,
    val expertise: List<Expertise>? = null,
)

data class EditProfileData(
    var name: String = "",
    var bio: String = "",
    var avatarUrl: String? = null // File paths or URLs
)

data class NotificationModel(
    val _id: String,
    val title: String,
    val message: String,
    val threadId: String?,
    val createdAt: String
)

data class NotificationResponse(
    val notifications: List<NotificationModel>
)
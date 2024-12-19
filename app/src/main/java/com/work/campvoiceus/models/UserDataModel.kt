package com.work.campvoiceus.models

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class RegisterRequest(val name: String, val email: String, val username: String, val password: String)
data class RegisterResponse(val email: String, val username: String, val token: String)

data class UserResponse(val name: String, val avatarUrl: String?, val username: String, val email: String)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

data class CreateThreadRequest(
    val title: String,
    val content: String
)

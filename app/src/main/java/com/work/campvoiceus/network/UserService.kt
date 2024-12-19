package com.work.campvoiceus.network


import com.work.campvoiceus.models.ApiResponse
import com.work.campvoiceus.models.LoginRequest
import com.work.campvoiceus.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {
    @POST("users/login")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("users/register")
    suspend fun registerUser(@Body registerRequest: Map<String, String>): LoginResponse

    @GET("users/{username}")
    suspend fun getUserByUsername(
        @Path("username") username: String,
        @Header("Authorization") token: String
    ): Map<String, Any>

    @PUT("user/{username}")
    suspend fun updateUserByUsername(
        @Path("username") username: String,
        @Header("Authorization") token: String,
        @Body updateData: Map<String, String>
    ): Map<String, Any>
}
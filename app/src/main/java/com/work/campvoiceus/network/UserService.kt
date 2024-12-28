package com.work.campvoiceus.network


import com.work.campvoiceus.models.LoginRequest
import com.work.campvoiceus.models.LoginResponse
import com.work.campvoiceus.models.NotificationResponse
import com.work.campvoiceus.models.RegisterRequest
import com.work.campvoiceus.models.RegisterResponse
import com.work.campvoiceus.models.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface UserService {
    @POST("users/login")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("users/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @GET("users/token")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<User>

    @PUT("users/profile/edit")
    @Multipart
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @PartMap profileData: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part avatar: MultipartBody.Part? // Optional avatar file
    ): Response<Unit>

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

    @POST("users/getuserbyid")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Body idMap: Map<String, String> // Key "id" maps to the user ID
    ): Response<User>

    @POST("users/savefcmtoken")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String,
        @Body fcmTokenData: Map<String, String>
    ): Response<Void>

    @GET("users/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<NotificationResponse>


}
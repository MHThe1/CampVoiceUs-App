package com.work.campvoiceus.network

import com.work.campvoiceus.models.ApiResponse
import com.work.campvoiceus.models.CreateThreadRequest
import com.work.campvoiceus.models.ThreadModel
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ThreadService {

    @POST("threads/createthread")
    @Multipart
    suspend fun createThread(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody
    ): Response<Unit>

    // Update this endpoint to match your backend's API
    @POST("threads/homethreads")
    suspend fun getThreads(
        @Header("Authorization") token: String
    ): Response<List<ThreadModel>>

    @GET("threads/user/{userId}")
    suspend fun getUserThreads(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<List<ThreadModel>>

    @POST("threads/upvote")
    suspend fun upvote(
        @Body data: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ThreadModel>

    @POST("threads/downvote")
    suspend fun downvote(
        @Body data: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ThreadModel>

}

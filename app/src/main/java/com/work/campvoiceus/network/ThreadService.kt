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


    @POST("threads")
    suspend fun createThread(
        @Header("Authorization") token: String,
        @Body thread: CreateThreadRequest
    ): Response<ApiResponse<ThreadModel>>

    @POST("threads/{threadId}/upvote")
    suspend fun upvoteThread(
        @Header("Authorization") token: String,
        @Path("threadId") threadId: String
    ): Response<Unit>

    @POST("threads/{threadId}/downvote")
    suspend fun downvoteThread(
        @Header("Authorization") token: String,
        @Path("threadId") threadId: String
    ): Response<Unit>

}

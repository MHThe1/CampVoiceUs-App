package com.work.campvoiceus.network

import com.work.campvoiceus.models.CommentResponse
import com.work.campvoiceus.models.ThreadModel
import com.work.campvoiceus.models.ThreadResponse
import com.work.campvoiceus.models.ThreadsByTagResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ThreadService {

    @POST("threads/createthread")
    @Multipart
    suspend fun createThread(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("tags") tags: RequestBody? = null,
        @Part file: MultipartBody.Part? = null
    ): Response<Void>

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

    @POST("threads/getthreadbyid")
    suspend fun getThreadById(
        @Body request: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ThreadResponse>

    @GET("threads/filterbytag/{tag}")
    suspend fun getThreadsByTag(
        @Path("tag") tag: String,
        @Header("Authorization") token: String
    ): Response<ThreadsByTagResponse>

    @POST("threads/comment")
    suspend fun addComment(
        @Body commentData: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ThreadModel>

    @POST("threads/upvotecomment")
    suspend fun upvoteComment(
        @Body voteData: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<CommentResponse>

    @POST("threads/downvotecomment")
    suspend fun downvoteComment(
        @Body voteData: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<CommentResponse>

    @POST("threads/filedownload")
    suspend fun getFileUrl(
        @Body requestBody: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<String>


}

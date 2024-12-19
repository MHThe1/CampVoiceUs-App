package com.work.campvoiceus.network

import com.work.campvoiceus.models.ApiResponse
import com.work.campvoiceus.models.CreateThreadRequest
import com.work.campvoiceus.models.ThreadModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Body

interface ThreadService {

    @GET("threads")
    suspend fun getThreads(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<ThreadModel>>>

    @POST("threads")
    suspend fun createThread(
        @Header("Authorization") token: String,
        @Body thread: CreateThreadRequest
    ): Response<ApiResponse<ThreadModel>>
}

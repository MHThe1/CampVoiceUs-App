package com.work.campvoiceus.network

import com.work.campvoiceus.models.ApiResponse
import com.work.campvoiceus.models.CreateThreadRequest
import com.work.campvoiceus.models.ThreadModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ThreadService {

    // Update this endpoint to match your backend's API
    @POST("threads/homethreads")
    suspend fun getThreads(
        @Header("Authorization") token: String
    ): Response<List<ThreadModel>>

    @POST("threads")
    suspend fun createThread(
        @Header("Authorization") token: String,
        @Body thread: CreateThreadRequest
    ): Response<ApiResponse<ThreadModel>>
}
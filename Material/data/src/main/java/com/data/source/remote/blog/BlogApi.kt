package com.data.source.remote.blog

import com.data.model.BlogObjectResponse
import retrofit2.http.GET

interface BlogApi {
    @GET("blogs")
    suspend fun get():List<BlogObjectResponse>
}
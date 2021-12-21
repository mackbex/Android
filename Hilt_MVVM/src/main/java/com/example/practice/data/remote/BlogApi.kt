package com.example.practice.data.remote

import retrofit2.http.GET

interface BlogApi {
    @GET("blogs")
    suspend fun get():List<BlogObjectResponse>
}
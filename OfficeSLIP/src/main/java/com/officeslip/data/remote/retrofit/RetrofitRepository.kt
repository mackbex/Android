package com.officeslip.data.remote.retrofit

import com.google.gson.JsonObject
import com.officeslip.data.model.LoginBody
import com.officeslip.data.model.User
import retrofit2.http.*

interface RetrofitRepository {

    @Headers("Accept: application/json")
    @POST("sign/auth/")
    suspend fun getUserInfo(@Body body:LoginBody): JsonObject?

}
package com.officeslip.data.remote.retrofit

import com.google.gson.JsonObject
import com.officeslip.data.model.LoginBody
import com.officeslip.data.model.User
import com.officeslip.di.module.RetrofitModule
import javax.inject.Inject

class RetrofitRepositoryImpl @Inject constructor(private val retrofit:RetrofitRepository) :RetrofitRepository {

    override suspend fun getUserInfo(body: LoginBody): JsonObject? {
        return retrofit.getUserInfo(body)
    }
}
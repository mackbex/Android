package com.officeslip.di.module

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.officeslip.LOGIN_URL
import com.officeslip.data.remote.retrofit.RetrofitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {
    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson) : Retrofit {

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClient = OkHttpClient.Builder()
                .addInterceptor(logging)

        return Retrofit.Builder()
                .baseUrl(LOGIN_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build()
    }

    @Provides
    fun provideRetrofitRepository(retrofit: Retrofit): RetrofitRepository = retrofit.create(RetrofitRepository::class.java)

    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

}
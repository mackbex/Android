package com.example.practice.di

import com.example.practice.data.local.BlogDao
import com.example.practice.data.local.CacheMapper
import com.example.practice.data.remote.BlogApi
import com.example.practice.data.remote.BlogMapper
import com.example.practice.repo.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun provideMainRepository(
        blogDao: BlogDao,
        blogApi: BlogApi,
        cacheMapper: CacheMapper,
        blogMapper: BlogMapper
    ): MainRepository {
        return MainRepository(blogDao, blogApi, cacheMapper, blogMapper)
    }
}
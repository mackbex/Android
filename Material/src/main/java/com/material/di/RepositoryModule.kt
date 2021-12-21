package com.material.di

import com.domain.repos.BlogRepository
import com.data.repos.impl.BlogRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Binds
    fun bindBlogRepository(impl: BlogRepositoryImpl): BlogRepository
}
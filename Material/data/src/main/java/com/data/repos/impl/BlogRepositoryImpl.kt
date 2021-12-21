package com.data.repos.impl

import com.data.model.mapFromEntityList
import com.data.util.DataState
import com.domain.model.Blog
import com.domain.repos.BlogRepository
import com.data.source.remote.blog.BlogApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import javax.inject.Inject

class BlogRepositoryImpl @Inject constructor(
    private val blogApi:BlogApi,
): BlogRepository {
    override fun getBlog(): Flow<DataState<List<Blog>>> = flow {
        emit(DataState.Loading)

        try{
            val networkBlogs = blogApi.get().mapFromEntityList()
            emit(DataState.Success(networkBlogs))
        }
        catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }
}
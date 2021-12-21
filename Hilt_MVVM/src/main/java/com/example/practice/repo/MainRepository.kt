package com.example.practice.repo

import com.example.practice.data.local.BlogDao
import com.example.practice.data.local.CacheMapper
import com.example.practice.data.remote.BlogApi
import com.example.practice.data.remote.BlogMapper
import com.example.practice.model.Blog
import com.example.practice.util.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class MainRepository
constructor(
    private val blogDao: BlogDao,
    private val blogApi: BlogApi,
    private val cacheMapper: CacheMapper,
    private val blogMapper: BlogMapper
){
    suspend fun getBlog():Flow<DataState<List<Blog>>> = flow {
        emit(DataState.Loading)
        kotlinx.coroutines.delay(1000)
        try{
            val networkBlogs = blogApi.get()
            val blogs = blogMapper.mapFromEntityList(networkBlogs)
            for (blog in blogs) {
                blogDao.insert(cacheMapper.mapToEntity(blog))
            }

            val cachedBlogs = blogDao.get()
            emit(DataState.Success(cacheMapper.mapFromEntityList(cachedBlogs)))

        }
        catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }
}
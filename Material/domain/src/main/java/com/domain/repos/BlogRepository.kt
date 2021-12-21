package com.domain.repos

import com.data.util.DataState
import com.domain.model.Blog
import kotlinx.coroutines.flow.Flow

interface BlogRepository {
    fun getBlog():Flow<DataState<List<Blog>>>
}


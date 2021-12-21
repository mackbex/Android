package com.example.practice.data.remote

import com.example.practice.model.Blog
import com.example.practice.util.EntityMapper
import javax.inject.Inject

class BlogMapper @Inject constructor() :EntityMapper<BlogObjectResponse, Blog>{

    override fun mapFromEntity(entity: BlogObjectResponse): Blog {
        return Blog(
            id = entity.id,
            title = entity.title,
            body = entity.body,
            image = entity.image,
            category = entity.category
        )
    }

    override fun mapToEntity(model: Blog): BlogObjectResponse {
        return BlogObjectResponse(
            id = model.id,
            title = model.title,
            body = model.body,
            image = model.image,
            category = model.category
        )
    }

    fun mapFromEntityList(entities:List<BlogObjectResponse>):List<Blog>{
        return entities.map { mapFromEntity(it) }
    }
}
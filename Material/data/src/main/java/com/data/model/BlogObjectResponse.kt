package com.data.model

import com.domain.model.Blog
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BlogObjectResponse(
    @SerializedName("pk")
    @Expose
    var id: Int,

    @SerializedName("title")
    @Expose
    var title: String,

    @SerializedName("body")
    @Expose
    var body: String,

    @SerializedName("category")
    @Expose
    var category: String,

    @SerializedName("image")
    @Expose
    var image: String
)


fun List<BlogObjectResponse>.mapFromEntity(entity: BlogObjectResponse): Blog {
    return Blog(
        id = entity.id,
        title = entity.title,
        body = entity.body,
        image = entity.image,
        category = entity.category
    )
}

fun List<BlogObjectResponse>.mapFromEntityList():List<Blog>{
    return this.map { mapFromEntity(it) }
}

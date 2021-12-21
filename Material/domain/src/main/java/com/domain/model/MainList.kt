package com.domain.model

data class MainList (
    val id:Int,
    val icon:Int,
    val title:String,
    val items:List<MaterialItem>? = null
)
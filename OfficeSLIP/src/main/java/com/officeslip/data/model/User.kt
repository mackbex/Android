package com.officeslip.data.model

data class User(
    val userId:String,
    val userNm:String,
    val partNo:String,
    val partNm:String,
    val corpNo:String,
    val corpNm:String,
    val LANG:String,
    var checked:Boolean = false
)
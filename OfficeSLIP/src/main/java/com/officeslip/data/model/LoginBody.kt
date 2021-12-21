package com.officeslip.data.model

import com.google.gson.annotations.SerializedName

data class LoginBody(

    @field:SerializedName("gw_id")
    val userId:String,
    @field:SerializedName("gw_pass")
    val userPw:String,
    @field:SerializedName("raw")
    val raw:String

)

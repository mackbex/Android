package com.officeslip.data.model

import android.graphics.Bitmap
import com.officeslip.ViewFlag
import java.io.Serializable

data class Slip(
//    val docIrn:String,
    val fileSize:Long = 0,
    var path:String = "",
    val width:Int = 0,
    val height:Int = 0,
    val title:String = "",
    var selected:Boolean = false,
    var quality:Int = 100,
    var docIrn:String = "",
    var thumbPath:String = "",
    var sdocNo:String = "",
    var slipFlag : ViewFlag = ViewFlag.Add,
    var docNo:Int = 0,
    var orgIrn:String = "",
    var latitude:Double = 0.0,
    var longitude:Double = 0.0,
    var slipInfo:SearchSlipResultItem? = null
) : Serializable
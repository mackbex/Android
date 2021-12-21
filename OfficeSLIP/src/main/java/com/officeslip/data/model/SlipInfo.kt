package com.officeslip.data.model

import android.graphics.Bitmap
import com.officeslip.ViewFlag
import java.io.Serializable
data class SlipInfo(
        var regUserId:String = "",
        var regUserNm:String = "",
        var regPartNo:String = "",
        var regPartNm:String = "",
        var regCorpNo:String = "",
        var regCorpNm:String = "",
        var slipKindNo:String,
        var slipKindNm:String,
        var sdocName:String,
        var jdocNo:String,
        var sdocNo:String,
        var sdocStep:String,
        var sdocSecu:String,
        var regTime:String,
        var sdocDevice:String



) : Serializable
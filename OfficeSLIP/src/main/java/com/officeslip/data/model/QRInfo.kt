package com.officeslip.data.model

import android.graphics.Bitmap
import com.officeslip.ViewFlag
import java.io.Serializable

data class QRInfo(
        var cabinet:String,
        var sdocName:String,
        var sdocKindNo:String,
        var sdocKindNM:String,
        var jdocNo:String,
        var partNo:String,
        var partNm:String,
        var regUserId:String,
        var reguserNm:String,
        var recvStatus:String,
        var sdocNo:String
) : Serializable
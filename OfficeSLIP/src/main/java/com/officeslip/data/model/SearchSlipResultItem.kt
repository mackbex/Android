package com.officeslip.data.model

import java.io.Serializable

data class SearchSlipResultItem(
    var regUserId:String,
    var regUserNm:String,
    var regPartNo:String,
    var regPartNm:String,
    var regCorpNo:String,
    var regCorpNm:String,
    var slipKindNo:String,
    var slipKindNm:String,
    var sdocName:String,
    var sdocNo:String,
    var docIrn:String,
    var sdocStep:String,
    var regTime:String,
    var folder:String,
    var slipCnt:String,
    var orgIrn:String?,
    var orgFile:String?,
    var checked:Boolean = false,
    var newDocIrn:String? = null,
    var newSdocNo:String? = null
): Serializable
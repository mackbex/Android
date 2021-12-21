package com.officeslip.data.model

data class Document(
    var regUserId:String,
    var regUserNm:String,
    var regPartNo:String,
    var regPartNm:String,
    var regCorpNo:String,
    var regCorpNm:String,
    var slipKindNo:String,
    var slipKindNm:String,
    var barcodeYn:Boolean = false,
    var managerId:String,
    var managerNm:String,
    var sdocName:String,
    var sdocNo:String,
    var jdocNo:String,
    var jdocIndex:String
)
package com.officeslip.data.model

enum class BookmarkType {
    RECT,
    PEN,
    CIRCLE,
    MEMO
}

enum class RectPosition {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM
}

data class Bookmark(
    var bookmarkType:BookmarkType = BookmarkType.RECT,
    var tag:String? = null,
    var leftPoint:Float = 0f,
    var topPoint:Float = 0f,
    var rightPoint:Float = 0f,
    var bottomPoint:Float = 0f,
    var alpha:Float = 1f,
    var lineColor:String? = null,
    var bgColor:String? = null,
    var text:String? = null,
    var italic:String = "0",
    var bold:String = "0",
    var weight:Float = 0f,
    var width:Float = 0f,
    var height:Float = 0f,
    var leftGap:Float = 0f,
    var topGap:Float = 0f,
    var backFlag:String = "0",
    var fontSize:Int = 0,
    var fontColor:String? = null
)
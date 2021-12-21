package com.officeslip.data.model

data class DownloadProgress(
    var progress:Int,
    var byte:ByteArray,
    var isCompleted:Boolean = false
)
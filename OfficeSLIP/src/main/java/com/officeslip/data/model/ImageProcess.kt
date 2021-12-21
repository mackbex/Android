package com.officeslip.data.model

import android.net.Uri
import com.officeslip.UploadMethod

data class ImageProcess(
    val uri:Uri?,
    val flag: UploadMethod,
    val localPath:String? = null
)
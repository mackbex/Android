package com.material.util.ext

import android.content.res.Resources

//region Extensions Utils
fun Int.dpToPx(): Int =
    (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.pxToDp(): Int =
    (this / Resources.getSystem().displayMetrics.density).toInt()

fun getScreenWidth() : Int = Resources.getSystem().displayMetrics.widthPixels

fun getScreenHeight() : Int = Resources.getSystem().displayMetrics.heightPixels
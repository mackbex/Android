package com.material.util.ext

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes resId:Int): View = LayoutInflater.from(context).inflate(resId, this, false)
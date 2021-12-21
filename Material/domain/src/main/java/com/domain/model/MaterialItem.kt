package com.domain.model

import android.view.View

data class MaterialItem(
    val title:String,
    val clickListener: View.OnClickListener
)
package com.officeslip.data.model

data class SlipKindItem(
    val name:String,
    val code:String,
    val barcode:Boolean,
    var checked:Boolean = false
)
//{
//    fun onClickListener(view: View) {
//        Toast.makeText(view.context, "Clicked: $name", Toast.LENGTH_SHORT).show()
//    }
//}
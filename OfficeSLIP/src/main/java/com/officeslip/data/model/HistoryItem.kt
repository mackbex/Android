package com.officeslip.data.model

data class HistoryItem(
    val folder:String,
    val sdocName:String,
    val title:String,
    var historyUserId:String,
    var historyUserNm:String,
    var regTime:String,
    var icon:String
)
//{
//    fun onClickListener(view: View) {
//        Toast.makeText(view.context, "Clicked: $name", Toast.LENGTH_SHORT).show()
//    }
//}
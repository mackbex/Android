package com.officeslip.data.model

import com.officeslip.*
import com.officeslip.util.Common
import java.io.Serializable
import kotlin.collections.HashMap


data class SearchSlipOption(
    var regUserId:String ,//= SysInfo.userInfo[userId].asString,
    var regUserNm:String ,//= SysInfo.userInfo[userNm].asString,
    var regPartNo:String ,//= SysInfo.userInfo[partNo].asString,
    var regPartNm:String ,//= SysInfo.userInfo[partNm].asString,
    var regCorpNo:String ,//= SysInfo.userInfo[corpNo].asString,
    var regCorpNm:String ,//= SysInfo.userInfo[corpNm].asString,
    var stepUsed: HashMap<String, Any> = HashMap<String, Any>().apply {
        put("VALUE","2,3,4,5,6")
        put("CHECKED", true)
    },

    var stepUnused: HashMap<String, Any> = HashMap<String, Any>().apply {
        put("VALUE","0,1")
        put("CHECKED", true)
    },
    var stepRemoved: HashMap<String, Any> = HashMap<String, Any>().apply {
        put("VALUE","9")
        put("CHECKED", false)
    },
    var secu:String = "",
    var fromDate:String = Common().getDateFromToday("yyyy-MM-dd",-1, DateUnit.Month),
    var toDate:String = Common().getDate("yyyy-MM-dd"),
    var slipKindNm:String = "- ALL -",
    var slipKindNo:String = "-1",
    var sdocName:String = "",
    var folderSlip:Boolean = true,
    var folderAttach:Boolean = true,
    var lang:String = SysInfo.LANG
):Serializable
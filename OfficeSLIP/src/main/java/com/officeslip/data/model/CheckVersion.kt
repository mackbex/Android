package com.officeslip.data.model

import com.officeslip.NetworkResFlag

data class CheckVersion(
        var flag:NetworkResFlag,
        var version:String? = null

)

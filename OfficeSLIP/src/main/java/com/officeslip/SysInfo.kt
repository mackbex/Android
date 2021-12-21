package com.officeslip

import com.google.gson.JsonObject
import com.officeslip.*

object SysInfo {

    object ServerMode {
        const val PRD = 1
        const val DEV = 0
    }

    object DataBase {
        const val ORACLE = 1
        const val MSSQL = 0
    }

    object Protocol {
        const val SOCKET = 1
        const val HTTP = 0
    }

    object UpdateMethod {
        const val SERVER = 1
        const val STORE = 0
    }

    val IP: String
        get() {
            return if (SERVER_MODE == ServerMode.PRD) {
                CONNECTION_PRD_IP
            } else {
                CONNECTION_DEV_IP
            }
        }

    val PORT: Int
        get() {
            return if (SERVER_MODE == ServerMode.PRD) {
                CONNECTION_PRD_PORT
            } else {
                CONNECTION_DEV_PORT
            }
        }

    val CHARSET: String
        get() {
            return SVR_ENCODING
        }

    val TIMEOUT: Int
        get() {
            return CONNECTION_TIMEOUT
        }

    lateinit var ROOT_PATH:String
    var IS_LOGGED = false
    var AUTO_LOGIN = true
    var DETECT_DOC = true
    var UPLOAD_SEMINAR_ORIGINAL = true
    var LANG = "KO"
    var IS_SCHEME_CALL = false

    var userInfo = JsonObject()
    var schemeUserInfo = JsonObject()
    var schemeDocTitle = ""
    var schemeJDocNo = ""


}
package com.officeslip

import android.Manifest
import android.content.res.Resources
import com.hadilq.liveevent.LiveEvent
import com.officeslip.SysInfo
import com.officeslip.util.log.Logger
import java.io.File
/**
 * Encrypt info
 */
const val APP_BUNDLE_ID = "OfficeSLIP"
const val APP_SQLCIPHER_KEY = "#WOONAMSOFT1998#%WOONAMSOFT1998%"
const val APP_ARIACIPHER_KEY = "#WOONAMSOFT1998#%WOONAMSOFT1998%"
const val USE_ENCRYPTION = true

/**
 * Local info
 */

const val UPLOAD_IMAGE_QUALITY = 100
val UPLOAD_PATH = SysInfo.ROOT_PATH + File.separator +"upload"
val UPLOAD_THUMB_PATH = UPLOAD_PATH + File.separator + "thumb"
val DOWNLOAD_PATH = SysInfo.ROOT_PATH + File.separator +"download"
const val BYTE_TRANSFER_FILE_DOCNO_START_IDX = 0 // OfficeSLIP 3.0 ver = 0,  else = 1
const val IMG_EXT = "JPG"
const val APK_NAME = "OfficeSLIP"

/**
 * Connection info
 */
const val SERVER_MODE = SysInfo.ServerMode.PRD
const val SVR_PROTOCOL = SysInfo.Protocol.SOCKET
const val CONNECTION_DB = SysInfo.DataBase.MSSQL
const val SVR_ENCODING = "UTF-8"
const val CONNECTION_PRD_IP =  "e-officeslip.jype.com"
const val CONNECTION_PRD_PORT = 9788
const val CONNECTION_DEV_IP = "e-officeslip.jype.com"
const val CONNECTION_DEV_PORT = 9788
const val LOGIN_URL = "http://i.jype.com/ngw/"
//const val CONNECTION_PRD_IP =  "e-officeslip.jype.com"
//const val CONNECTION_PRD_PORT = 9788
//const val CONNECTION_DEV_IP = "e-officeslip-test.jype.com"
//const val CONNECTION_DEV_PORT = 9888
const val CONNECTION_TIMEOUT =15000
const val AGENT_SERVERKEY = "JYP"
const val DEFAULT_LANGUAGE = "ko"
const val USE_SYSTEM_LANGUAGE = false
const val AGENT_EDMS = "W" //W = Agent, X = Xvarm
const val AGENT_RECEIPT = "SLIPDOC"
const val AGENT_FOLDER = "SLIP"
const val UPDATE_METHOD = SysInfo.UpdateMethod.SERVER
const val APP_UPDATE_URL_PRD = "https://e-officeslip.jype.com/UpdateFiles/M/"
const val APP_UPDATE_URL_DEV = "https://e-officeslip.jype.com/UpdateFiles/M/"

/**
 * Log info
 */
val LOG_LEVEL = Logger.LOG_LEVEL.TRACE
val LOG_CONSOLE = true
val LOG_PATH =  SysInfo.ROOT_PATH + File.separator + "logs"

/**
 * Search option
 */
val SEARCH_DATE_FORMAT = "yyyyMMdd"

/**
 * User column selector
 */

val loginId = "LOGIN_ID"
val userId = "USER_ID"
val userNm = "USER_NM"
val partNo = "PART_NO"
val partNm = "PART_NM"
val corpNo = "CORP_NO"
val corpNm = "CORP_NM"

/**
 * Slipkind selector
 */

val kindNm = "KIND_NM"
val kindNo = "KIND_NO"


/** inner const **/

const val RESULT_SELECT_SLIPKIND = 0
const val RESULT_SEARCH_USER = 1
const val RESULT_CAMERA = 2
const val RESULT_CREATE_SLIP_IMAGE = 3
const val RESULT_GALLERY = 4
const val RESULT_SEARCH_SLIP_OPTION = 5
const val RESULT_SEARCH_SLIP_IMAGE = 6

const val FRAG_ADD_SLIP = "ADD_SLIP"
const val FRAG_SEARCH_SLIP = "SEARCH_SLIP"
const val FRAG_QR = "QR"
const val FRAG_SETTING = "SETTING"
const val SLIP_INDEX = "INDEX"
const val VIEWER_FLAG = "VIEWER_FLAG"
const val SLIP_ITEM = "SLIP_ITEM"
const val SLIP_INFO = "SLIP_INFO"
const val BUNDLE_SDOC_NO = "BUNDLE_SDOC_NO"
const val BUNDLE_FOLDER = "BUNDLE_FOLDER"

const val THUMB_VIEWER_MAX_ZOOM = 5



const val CROP_CIRCLE_WIDTH = 15
const val UPLOAD_SLIP_SIZE_LIMIT =  500000 //500KB
const val UPLOAD_THUMB_SCALE_LIMIT =  500 //pixel
const val UPLOAD_SLIPDOC_DEFAULT_INDEX = 10

enum class NetworkResFlag {
    UPDATE_AVAILABLE,
    VERSION_NEWEST,
    CHECK_FAILED,
    NETWORK_DISABLED,
    FAILED_GET_USER_INFO,
    FAILED_GET_SDOC_KIND_LIST,
    SUCCESS,
    UNKNOWN_EXCEPTION
}

enum class ThumbMode {
    View,
    Edit
}

enum class UploadMethod {
    Camera,
    Gallery
}

enum class sizeUnit {
    KB,
    BYTE,
    MB
}

enum class ViewMode {
    View,
    Edit,
    Thumb
}

enum class ViewFlag {
    Edit,
    Search,
    Add
}


enum class ExtensionFlag {
    Copy,
    Move
}
enum class EditMode {
    Pinch,
    Crop,
    Memo,
    Circle,
    Rect,
    Pen,
    Rotate
}

enum class PageType {
    ADD_SLIP,
    SEARCH_SLIP,
    QR,
    SETTING,
    NULL
}

enum class DateUnit {
    Day,
    Week,
    Month,
    Year
}


enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}



const val SHAPE_MIN_SIZE = 6
/**
 * Pen default
 */
const val PEN_WEIGHT_DEFAULT = 25f
const val PEN_BACKGROUND_DEFAULT = "#FFFF8D"
const val PEN_ALPHA_DEFAULT = 200

/**
 * Rect default
 */
const val RECT_WEIGHT_DEFAULT = 3f
const val RECT_BACKGROUND_DEFAULT = "#BDBDBD"
const val RECT_ALPHA_DEFAULT = 200
const val RECT_LINE_DEFAULT = "#FF1744"
const val RECT_BG_ENABLE_DEFAULT = "1"

/**
 * Circle default
 */
const val CIRCLE_WEIGHT_DEFAULT = 3f
const val CIRCLE_BACKGROUND_DEFAULT = "#BDBDBD"
const val CIRCLE_ALPHA_DEFAULT = 200
const val CIRCLE_LINE_DEFAULT = "#FF1744"
const val CIRCLE_BG_ENABLE_DEFAULT = "1"

/**
 * Memo default
 */
const val MEMO_FONTSIZE_DEFAULT = 10
const val MEMO_BACKGROUND_DEFAULT = "#FFD600"
const val MEMO_FOREGROUND_DEFAULT = "#FF1744"
const val MEMO_ALPHA_DEFAULT = 0.5f
const val MEMO_LINE_DEFAULT = "#BDBDBD"
const val MEMO_WEIGHT_DEFAULT = 1f
const val MEMO_BOLD_DEFAULT = "0"
const val MEMO_ITALIC_DEFAULT = "0"
const val MEMO_BG_ENABLE_DEFAULT = "0"

/**
 * App permission. do not modify
 */
const val MULTIPLE_PERMISSIONS = 10


inline fun <T: Any> guardLet(vararg elements: T?, closure: () -> Nothing): List<T> {
    return if (elements.all { it != null }) {
        elements.filterNotNull()
    } else {
        closure()
    }
}

infix fun Any?.ifNull(block: () -> Unit) {
    if(this == null) block()
}

fun LiveEvent<*>.notifyObserver() {
    this.value = this.value
}

const val DETECT_RECTANGLE_LIMIT = 50f
val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val requiredPermissions = arrayOf(
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.INTERNET,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
//    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.CAMERA,
//    Manifest.permission.ACCESS_FINE_LOCATION,
//    Manifest.permission.ACCESS_MEDIA_LOCATION
)
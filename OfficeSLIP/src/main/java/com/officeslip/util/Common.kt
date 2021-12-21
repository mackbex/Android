package com.officeslip.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.widget.TextView
import androidx.exifinterface.media.ExifInterface
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.officeslip.*
import com.officeslip.data.model.Slip
import com.officeslip.util.log.Logger
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import java.io.*
import java.nio.IntBuffer
import java.text.SimpleDateFormat
import java.util.*
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.opengles.GL10


class Common {
    fun isBlank(str:String?): Boolean {

        if(str == null || str.replace(Regex("\\p{Z}"), "").isEmpty())
        {
            return true
        }

        return false
    }

    fun detectScheme(activity: Activity):Boolean {
        return Intent.ACTION_VIEW == activity.intent.action
    }


    fun getBarcode(userId:String):String {
        var strUUID = UUID.randomUUID().toString()
        return "$userId-${getDate("yyyyMMdd")}-${getDate("HHmmss")}-${strUUID.substring(strUUID.length - 5)}"
    }

    fun getDisplayMetrics(activity: Activity): DisplayMetrics {
        val outMetrics = DisplayMetrics()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = activity.display
            display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = activity.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(outMetrics)
        }
        return outMetrics
    }

    fun xmlToJson(str:String):JsonObject {
        return JsonParser.parseString(XmlToJson.Builder(str).build().toString()).asJsonObject
    }

    fun simpleAlert(activity: Activity?, title:String?, msg:String?, callBack: () -> Unit) {
        activity?.run {
            AlertDialog.Builder(this).run {
                if(!isBlank(title)) {
                    setTitle(title)
                }
                setMessage(msg)
                setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    callBack()
                }
            }.show()
        }
    }

    fun pixel2pt(pixel:Int, dpi:Int = 200):Int {

        return  ( pixel * 720 ) / dpi
    }


    fun pt2pixel( pt:Int, dpi:Int = 200):Float {
        return ( pt * dpi ) / 720f
    }

    fun getDate(strRegex:String, date:String? = null, inputRegex:String="yyyy-MM-dd"):String
    {
        return date?.let {
            val specificDate = SimpleDateFormat(inputRegex).parse(it)
            SimpleDateFormat(strRegex).format(specificDate)
        } ?: run {
            SimpleDateFormat(strRegex, Locale.KOREA).format(Date())
        }
    }

    fun getDateFromToday(strRegex:String, dif:Int, unit: DateUnit = DateUnit.Month):String
    {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        var calUnit:Int

        when(unit) {
            DateUnit.Day -> {
                calUnit = Calendar.DATE
            }
            DateUnit.Month -> {
                calUnit = Calendar.MONTH
            }
            DateUnit.Week -> {
                calUnit = Calendar.WEEK_OF_MONTH
            }
            DateUnit.Year -> {
                calUnit = Calendar.YEAR
            }
        }

        calendar.add(calUnit, dif)

        return SimpleDateFormat(strRegex).format(Date(calendar.timeInMillis))
    }

    fun simpleConfirm(activity: Activity, title:String?, msg:String?, positiveCallBack: () -> Unit, negativeCallback: () -> Unit) {
        AlertDialog.Builder(activity).run {
            if(!isBlank(title)) {
                setTitle(title)
            }
            setMessage(msg)
            setPositiveButton(activity.getString(R.string.confirm)) { _, _ ->
                positiveCallBack()
            }
            setNegativeButton(activity.getString(R.string.cancel)) { _, _ ->
                negativeCallback()
            }
        }.show()
    }

    fun removeFolder(context:Context, strFolderPath:String):Boolean {

        var dir = File(strFolderPath)
        if (dir.isDirectory)
        {
            for(file in dir.listFiles())
            {
                removeFolder(context, file.absolutePath)
            }
        }

        if(dir.delete())
        {
            val file = File(strFolderPath)
            MediaScannerConnection.scanFile(context, arrayOf(file.toString()), arrayOf(file.name), null)
//            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dir)))
        }
        return true
    }

    fun removeFile(context:Context, strFilePath:String?):Boolean {
        if(isBlank(strFilePath)) return true
        var bRes = false
        var file = File(strFilePath)
        bRes = file.delete()
        if(bRes)
        {
            MediaScannerConnection.scanFile(context, arrayOf(file.toString()), arrayOf(file.name), null)
        }

        Logger.WriteLog(this::javaClass.name, "removeFile", 9)
        return bRes
    }

    fun getCircleProgress(context: Context?, text:String? = null, callBack: () -> Unit): AlertDialog {
        return AlertDialog.Builder(context).apply {
            val view = LayoutInflater.from(context).inflate(R.layout.progress_circle, null)
            setView(view)
            setCancelable(false)
            if(!text.isNullOrBlank()) {
                view.findViewById<TextView>(R.id.view_tvProgressTitle).text = text
            }
            setNegativeButton(
                context?.getString(R.string.btn_cancel),
                DialogInterface.OnClickListener { dialog, which ->
//                    if(callBack != null) {
                        callBack()
//                    }
                    dialog.dismiss()
                })
        }.create()
    }

    fun getCurrentLocale(context:Context) : String {
        val locale: Locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            locale = context.resources.configuration.locales.get(0)
        }
        else
        {
            locale = context.resources.configuration.locale
        }

        return locale.language.substring(0, 2)
    }

    fun setLocale(context:Context, lang:String) {

        val locale: Locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            locale = context.resources.configuration.locales.get(0)
        }
        else
        {
            locale = context.resources.configuration.locale
        }

        if(locale.language.substring(0,2) != SysInfo.LANG)
        {
            val res = context.resources
            val dm = res.displayMetrics
            val conf = res.configuration
            conf.setLocale(Locale(lang.toLowerCase()))
            res.updateConfiguration(conf, dm)
            var activity = context as Activity
            //    activity.recreate()
        }
    }

    fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw weight and width of m_thumbData
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // weight and width larger than the requested weight and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    //copy File
    fun copyFile(strOldPath:String, strNewPath:String):Boolean {

        var bRes = false
        var fis:FileInputStream? = null
        var fos:FileOutputStream? = null

        try {

            val file = File(strNewPath)
            if(!file.exists())
            {
                file.createNewFile()
            }
            fis = FileInputStream(strOldPath)
            fos = FileOutputStream(strNewPath)

            var byteArr 	= ByteArray(1024)

            var nLength = 0

            while({ nLength = fis?.read(byteArr)!!; nLength}() > -1)
            {
                fos.write(byteArr, 0, nLength)
            }

            bRes = true
        }
        catch (e:Exception)
        {
            Logger.error("copyFile", e)
        }

        finally
        {
            fis?.close()
            fos?.close()
        }
        return bRes
    }

    fun getRealPathFromUri(uri:Uri?, context:Context):String? {
        var strResPath:String? = null

        uri?.run {
            var nColumnIndex = 0
            val objData     = arrayOf(MediaStore.Images.Media.DATA)
            context.contentResolver.query(uri, objData, null, null, null)?.use {
                try {
                    if (it.moveToFirst()) {
                        nColumnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    }
                    strResPath = it.getString(nColumnIndex)
                } catch (e: Exception) {
                    Logger.error("getRealPathFromUri", e)
                    strResPath = null
                } finally {
                    it.close()
                }
            }
        }
        return strResPath
    }


    fun decodeSampledBitmapFromResource(byte:ByteArray, width:Int? = null, height:Int? = null): Bitmap? {

        var bRes:Bitmap? = null
        try
        {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(byte, 0, byte.size, options)
            //  BitmapFactory.decodeResource(res, resId, options)

            val reqWidth = if(width == null) { options.outWidth } else run { width }
            val reqHeight = if(height == null) { options.outHeight } else run { height }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth!!, reqHeight!!)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false

            bRes = BitmapFactory.decodeByteArray(byte, 0, byte.size, options)
        }
        catch(e : Exception)
        {
            e.printStackTrace()
        }

        return  bRes
    }


    fun fileToByte(path:String):ByteArray? {
        val file = File(path)
        val size: Int = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bytes
    }

    fun copyExif(oldExif: ExifInterface, newPath:String) {

        val attrs = arrayListOf<String>(
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_SUBSEC_TIME,
            ExifInterface.TAG_WHITE_BALANCE
        )

        val newExif = ExifInterface(newPath)

        if (attrs.size > 0) {
            for (attr in attrs) {
                val value = oldExif.getAttribute(attr)
                if (value != null)
                    newExif.setAttribute(attr, value)
            }
            newExif.saveAttributes()
        }
    }


    //Save original m_thumbData.
    @Throws(Exception::class)
    fun saveOriginal(strImageName:String, strImgFile: File /*data:ByteArray*/, docIrn:String, stream:InputStream? = null): Slip {

        val path               = File(UPLOAD_PATH)

        if(!path.exists())
        {
            path.mkdirs()
        }
//        var file = File(path.absolutePath, fileName)

        var imgSize_KB:Long = 0


//        var myAttribute = ""

        val exif = stream?.use { stream ->
            ExifInterface(stream)
        }
            ?: ExifInterface(strImgFile.absolutePath)

//                .apply {
//            myAttribute += getAttribute(ExifInterface.TAG_DATETIME)
//            myAttribute += getAttribute(ExifInterface.TAG_FLASH)
//            myAttribute += exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
//            myAttribute += getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
//            myAttribute += exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
//            myAttribute += getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
//            myAttribute += getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
//            myAttribute += getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
//            myAttribute += getAttribute(ExifInterface.TAG_MAKE)
//            myAttribute += getAttribute(ExifInterface.TAG_MODEL)
//            myAttribute += getAttribute(ExifInterface.TAG_ORIENTATION)
//            myAttribute += getAttribute(ExifInterface.TAG_WHITE_BALANCE)
//
//        }


//        val slip: Slip? = null
        ExifUtil().rotateBitmap(strImgFile.absolutePath).run {
            val newFile = File(path.absolutePath, strImageName)
            FileOutputStream(newFile).use{
                compress(Bitmap.CompressFormat.JPEG, UPLOAD_IMAGE_QUALITY, it)
                imgSize_KB = strImgFile.length() / 1000
                it.flush()
            }

            copyExif(exif, newFile.absolutePath)

//            if(!isRecycled)
//            {
//                recycle()
//            }
            val gpsInfo = GPSConverter(exif)

//
//            var myAttribute = ""
//            val xx = ExifInterface(strImgFile.absolutePath).apply {
//                myAttribute += getAttribute(ExifInterface.TAG_DATETIME)
//            myAttribute += getAttribute(ExifInterface.TAG_FLASH)
//            myAttribute += getAttribute(ExifInterface.TAG_GPS_LATITUDE)
//            myAttribute += getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
//            myAttribute += getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
//            myAttribute += getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
//            myAttribute += getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
//            myAttribute += getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
//            myAttribute += getAttribute(ExifInterface.TAG_MAKE)
//            myAttribute += getAttribute(ExifInterface.TAG_MODEL)
//            myAttribute += getAttribute(ExifInterface.TAG_ORIENTATION)
//            myAttribute += getAttribute(ExifInterface.TAG_WHITE_BALANCE)
//
//        }


            return Slip(
                    imgSize_KB,
                    strImgFile.absolutePath,
                    width,
                    height,
                    "",
                    false,
                    100,
                    docIrn = docIrn,
                    latitude = gpsInfo.latitude?.let{ it.toString().toDouble()} ?: run { 0.0 },
                    longitude = gpsInfo.longitude?.let{ it.toString().toDouble()} ?: run { 0.0 }
                )

        }
    }

    /**
     * 특수문자 변환
     */
    fun convertSpecialChar(str:String?, reverse:Boolean = false):String
    {
        var strRes = str

        if (!isBlank(strRes)) {
            if(reverse) {strRes = strRes!!.replace("&ltbr&gt", "\\n")
                strRes = strRes.replace("〉", ">")
                strRes = strRes.replace("〈", "<")
                strRes = strRes.replace("’", "'")
                strRes = strRes.replace("！", "!")
                strRes = strRes.replace("％", "%")
                strRes = strRes.replace("〉", "&gt")
                strRes = strRes.replace("〈", "&lt")
                strRes = strRes.replace("［", "&amp")
                strRes = strRes.replace("］", "&ampquot")
                strRes = strRes.replace("＆", "&")
            }
            else {
                strRes = strRes!!.replace("\\n", "&ltbr&gt")
                strRes = strRes.replace(">", "〉")
                strRes = strRes.replace("<", "〈")
                strRes = strRes.replace("'", "’")
                strRes = strRes.replace("!", "！")
                strRes = strRes.replace("%", "％")
                strRes = strRes.replace("&gt", "〉")
                strRes = strRes.replace("&lt", "〈")
                strRes = strRes.replace("&amp", "［")
                strRes = strRes.replace("&ampquot", "］")
                strRes = strRes.replace("&", "＆")
                strRes = strRes.replace("--", "")
            }

        } else {
            strRes = ""
        }


        return strRes
    }

    fun saveThumb(slip:Slip):String? {
        var thumb:File? = null
        try {
            val path = File(UPLOAD_THUMB_PATH)
            thumb = File(path, "${slip.docIrn}.jpg")

            var nDefaultQuality = slip.quality / 4

            BitmapFactory.decodeFile(slip.path).run {

//            ExifUtil().rotateBitmap(slip.path).run {
                if (!path.exists()) {
                    path.mkdirs()
                }

                var scale = if(width > height) { width } else { height }
                var width = width
                var height = height

                while(scale > UPLOAD_THUMB_SCALE_LIMIT) {
                    width       =  (width * 0.9).toInt()
                    height      =  (height * 0.9).toInt()
                    scale     = if(width > height) { width } else { height }
                }


                val bitThumb = Bitmap.createScaledBitmap(this, width, height, true)

//                if (!isRecycled) {
//                    recycle()
//                }

                FileOutputStream(thumb).use {
                    bitThumb.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    it.flush()
                    if(!bitThumb.isRecycled) {
                        bitThumb.recycle()
                    }
                    if (!isRecycled) {
                        recycle()
                    }

                }
            }
        }
        catch(e:java.lang.Exception) {
            Logger.error("saveThumb : ", e)
            return null
        }

        slip.thumbPath = thumb.absolutePath
        return thumb.absolutePath
    }

    fun getBytesFromFile(file: File): ByteArray {

        lateinit var bytes:ByteArray
        FileInputStream(file).use {

//            if(file.length() > Long.MAX_VALUE)
//            {
//
//            }
            bytes = ByteArray(file.readBytes().size)

            var offset = 0
            var numRead = 0

            // while (it.read(bytes).let { tmp = it; it != -1 })
            //while( offset < bytes.size && (numRead = it.read(bytes, offset,bytes.size - offset) > -1))
            while(offset < bytes.size && { numRead = it.read(bytes, offset, bytes.size - offset); numRead }() > -1)
            {
                offset += numRead
            }

            if(offset < bytes.size)
            {
                throw IOException("Could not completely read file " + file.name)
            }
        }

        return bytes
    }
}
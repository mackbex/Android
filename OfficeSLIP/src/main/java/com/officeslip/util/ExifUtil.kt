package com.officeslip.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix


class ExifUtil {
    /**
     * @see http://sylvana.net/jpegcrop/exif_orientation.html
     */


    fun rotateBitmap(path: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(path)
        try {
            val nOrientation = getExifOrientation(path)
            // Common().getAppOrientation(activity)
            if (nOrientation == 1) {
                return bitmap
            }
//
//            val exifInterface = ExifInterface(path)
//            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,
//                    nOrientation.toString())
//            exifInterface.saveAttributes()


            val matrix = Matrix()
            when (nOrientation) {
                2 -> matrix.setScale(-1f, 1f)
                3 -> matrix.postRotate(180f)
                4 -> {
                    matrix.postRotate(180f)
                    matrix.postScale(-1f, 1f)
                }
                5 -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                6 -> matrix.postRotate(90f)
                7 -> {
                    matrix.postRotate(-90f)
                    matrix.postScale(-1f, 1f)
                }
                8 -> matrix.setRotate(-90f)
                else -> matrix.postRotate(0f)
            }

            try {
                val oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                //  bitmap.recycle()
                return oriented
            } catch (e: Exception) {
//                Logger.WriteException(this.javaClass.name, "rotateBitmap", e, 7)
                return bitmap
            }

        } catch (e: Exception) {
//            Logger.WriteException(this.javaClass.name, "rotateBitmap", e, 7)
        }

        return bitmap
    }


    private fun getExifOrientation(path:String):Int {
        var nOrientation = 1

        // if (Build.VERSION.SDK_INT >= 5) {
        val exifClass = Class.forName("android.media.ExifInterface")
        val exifConstructor = exifClass.getConstructor(String::class.java)
        val exifInstance = exifConstructor.newInstance(path)
        val getAttributeInt = exifClass.getMethod("getAttributeInt",  String::class.java , Int::class.java)
        val tagOrientationField = exifClass.getField("TAG_ORIENTATION")
        val tagOrientation = tagOrientationField.get(null) as String
        nOrientation = getAttributeInt.invoke(exifInstance, tagOrientation, 1) as Int
        // }

        return nOrientation
    }

}
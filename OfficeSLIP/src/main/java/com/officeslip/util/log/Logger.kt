package com.officeslip.util.log

import android.os.Build
import android.util.Log
import com.officeslip.LOG_CONSOLE
import com.officeslip.LOG_PATH
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class Logger {

    object LOG_LEVEL {
        val FATAL = 0
        val ERROR = 1
        val WARN = 2
        val INFO = 3
        val DEBUG = 4
        val TRACE = 5
    }

    companion object {

        /**
         * log 파일에 exception 기록
         * @param className
         * 					클래스 이름
         * @param e
         * 					Exception
         */


        fun fatal(str:String, e:Exception) {
            val name = object{}.javaClass.enclosingMethod?.name

            val swError             = StringWriter()
            e.printStackTrace(PrintWriter(swError))

            WriteLog(
                name,
                swError.toString(),
                LOG_LEVEL.FATAL
            )
        }

//        fun error(str:String, e:Exception) {
//            val name = object{}.javaClass.enclosingMethod?.name
//
//            val swError             = StringWriter()
//            e.printStackTrace(PrintWriter(swError))
//
//            WriteLog(
//                name,
//                swError.toString(),
//                LOG_LEVEL.ERROR
//            )
//        }

        fun error(str:String, e:Throwable?) {
            val name = object{}.javaClass.enclosingMethod?.name

            var errMsg = str
            e?.let {
                val swError             = StringWriter()
                e.printStackTrace(PrintWriter(swError))
                errMsg = "$errMsg - ${swError.toString()}"
            }

            WriteLog(
                name,
                errMsg,
                LOG_LEVEL.ERROR
            )
        }

        fun warn(str:String) {
            val name = object{}.javaClass.enclosingMethod?.name
            WriteLog(
                name,
                str,
                LOG_LEVEL.WARN
            )
        }

        fun info(str:String) {
            val name = object{}.javaClass.enclosingMethod?.name
            WriteLog(
                name,
                str,
                LOG_LEVEL.INFO
            )
        }

        fun debug(str:String) {
            val name = object{}.javaClass.enclosingMethod?.name
            WriteLog(
                name,
                str,
                LOG_LEVEL.DEBUG
            )
        }

        fun trace(str:String) {
            val name = object{}.javaClass.enclosingMethod?.name
            WriteLog(
                name,
                str,
                LOG_LEVEL.TRACE
            )
        }

        /**
         * Log 생성
         */

        fun WriteLog(methodName:String?, strLog:String, logLevel:Int):Boolean {
            var bRes = false


            val file = File(LOG_PATH)
            if(!file.exists())
            {
                bRes = file.mkdirs()
            }

            val date = Date()
            var dateFormat = SimpleDateFormat("yyyyMMdd")
            val strLogFile =  LOG_PATH + File.separator + dateFormat.format(date) + ".log"

            if(LOG_CONSOLE) {

                dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                Log.i(methodName, "${dateFormat.format(date).toString()} ($methodName :${Build.DEVICE}) : $strLog")
            }

            if(com.officeslip.LOG_LEVEL >= logLevel) {

                try {

                    BufferedWriter(FileWriter(strLogFile, true)).use {
                        it.write(dateFormat.format(date).toString() + "(" + methodName + ":" + Build.DEVICE + ") : ")
                        it.write(strLog)
                        it.write("\n")
                        it.flush()
                    }
                }
                catch (e:Exception)
                {
                    e.printStackTrace()
                    bRes = false
                }
            }
            return bRes
        }
    }
}
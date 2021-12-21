package com.officeslip.di.module

import android.app.Application
import com.officeslip.AGENT_SERVERKEY
import com.officeslip.SysInfo
import com.officeslip.util.AppLifecycleTracker
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class AppModule : Application()
{
    companion object {
        lateinit var instance: AppModule
            private set
    }
    //Add tracker to detect app went to background
    override fun onCreate() {
        super.onCreate()
        instance = this

        SysInfo.ROOT_PATH = filesDir.absolutePath + File.separator + AGENT_SERVERKEY
//        SysInfo.UPLOAD_PATH = SysInfo.ROOT_PATH + File.separator +"upload"
//        SysInfo.UPLOAD_THUMB_PATH = SysInfo.UPLOAD_PATH + File.separator + "thumb"

        var tracker = AppLifecycleTracker()
        registerActivityLifecycleCallbacks(tracker)
    }
}
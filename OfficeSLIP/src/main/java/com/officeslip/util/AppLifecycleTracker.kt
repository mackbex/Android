package com.officeslip.util


import android.app.Activity
import android.app.Application
import android.os.Bundle


class AppLifecycleTracker : Application.ActivityLifecycleCallbacks {

    private var nStarted = -1


    //Activity called from background
    override fun onActivityStarted(activity: Activity) {

//        CALL_ANOTHER_APPLICATION = false
        if(nStarted == 0)
        {
//            g_SysInfo = SysInfo(activity!!)
//            g_UserInfo = pj_UserInfo(activity!!)
            if(activity!!::class.java.simpleName != "SplashActivity") {


                //    LoginActivity.Login(activity).execute(g_UserInfo.strUserID)
                //  com.officeslip.Operation.CheckAppUpdate(activity!!).execute()
            }
        }

        nStarted++
    }

    //Activity went to background
    override fun onActivityStopped(activity: Activity) {
        nStarted--
        if(nStarted == 0)
        {
//            if(!CALL_ANOTHER_APPLICATION) g_SysInfo.bPINLogged = false
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        if(nStarted < 0)
        {
            nStarted ++
        }
    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

}
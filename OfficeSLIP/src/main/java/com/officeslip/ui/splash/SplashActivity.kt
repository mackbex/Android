package com.officeslip.ui.splash

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.officeslip.*
import com.officeslip.base.BaseActivity
import com.officeslip.databinding.ActivitySplashBinding
import com.officeslip.ui.login.LoginActivity
import com.officeslip.ui.main.MainActivity
import com.officeslip.util.Common
import com.officeslip.util.log.Logger
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.system.exitProcess

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
    private val m_C = Common()
    override val layoutResourceId: Int
        get() =  R.layout.activity_splash
        override val viewModel by viewModels<SplashViewModel>()

    init {
        System.loadLibrary("opencv_java4")
    }

    override fun initStartView() {
    }

    override fun initDataBinding() {

        viewModel.getVersion.observe(this, Observer {

            when(it.status) {
                Status.SUCCESS -> {

                    when(it.data?.flag) {
                        NetworkResFlag.VERSION_NEWEST -> {
                            viewModel.initAppInfo()
                        }
                        NetworkResFlag.UPDATE_AVAILABLE -> {
                            if(UPDATE_METHOD == SysInfo.UpdateMethod.SERVER) {
                                m_C.simpleAlert(this@SplashActivity
                                        ,null
                                        ,getString(R.string.new_version_detected_server)
                                ) {  updateApp(it.data.version) }
                            }
                            else {
                                m_C.simpleAlert(this@SplashActivity
                                        ,null
                                        ,getString(R.string.new_version_detected_store)
                                ) {  this@SplashActivity.finish() }
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    m_C.simpleAlert(this@SplashActivity
                        ,null
                        ,getString(R.string.failed_connect_agent)
                    ) { this@SplashActivity.finish() }
                }
            }
        })



        viewModel.updateUserInfo.observe(this,  {
            when(it.status) {
                Status.SUCCESS -> {
                    m_C.setLocale(this@SplashActivity, SysInfo.LANG)
                    Intent(this@SplashActivity, MainActivity::class.java).run {
                        startActivity(this)
                    }
                    this.finish()
                }
                Status.ERROR -> {
                    m_C.simpleAlert(
                        this@SplashActivity
                        , null
                        , it.message
                    ) {
                        Intent(this@SplashActivity, LoginActivity::class.java).run {
                            startActivity(this)
                        }
                        this.finish()
                    }
                }
            }
        })

        viewModel.initAppInfo.observe(this, Observer {

            when(it.status) {
                Status.SUCCESS -> {
                    if (SysInfo.IS_LOGGED) {
                        viewModel.updateUserInfo()
                    } else {
                        Intent(this@SplashActivity, LoginActivity::class.java).run {
                            startActivity(this)
                        }
                        this.finish()
                    }
                }

                Status.ERROR -> {

                    Logger.info("Failed to access sqlite.")
                    Intent(this@SplashActivity, LoginActivity::class.java).run {
                        startActivity(this)
                    }
                    this.finish()

//                    m_C.simpleAlert(this@SplashActivity
//                        ,null
//                        ,it.message
//                    ) { this@SplashActivity.finish() }
                }
            }
        })
    }

    override fun initAfterBinding() {
        if (checkPermissionsGranted()) {
            viewModel.getAppVersion()

            m_C.removeFolder(this@SplashActivity, UPLOAD_PATH)
            m_C.removeFolder(this@SplashActivity, DOWNLOAD_PATH)
        }
    }

    private fun checkPermissionsGranted(): Boolean {
        var bRes = false
        var nPermissionStatus: Int
        var listPermissionsNeeded: ArrayList<String> = ArrayList()

        requiredPermissions.forEach { p: String ->
            nPermissionStatus = ContextCompat.checkSelfPermission(this, p)
            if (nPermissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this@SplashActivity, listPermissionsNeeded.toTypedArray(), MULTIPLE_PERMISSIONS)
        } else {
            bRes = true
        }

        return bRes
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {

                var isAllGranted = true
                for (i in 0 until grantResults.size) {
                    if (grantResults[i] != 0) {
                        isAllGranted = false
                        Logger.warn( "Application permission error : ${permissions.get(i)} not granted.")
                        break
                    }
                }

                if (grantResults.isNotEmpty() && isAllGranted) {

                    viewModel.getAppVersion()

                } else {
                    exitProcess(0)
                }
            }
        }
    }



    private fun updateApp(version:String?) {
        val strDesination = "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath}/${APK_NAME}_${version}.apk"
        //  var uri = Uri.parse("file://" + strDesination)
        val uri =  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)  FileProvider.getUriForFile(this@SplashActivity, BuildConfig.APPLICATION_ID + ".Util.GenericFileProvider", File(strDesination))
        else Uri.parse("file://$strDesination")

        //Delete update file if exists
        val file = File(strDesination)
        if (file.exists()) file.delete()

        //get url of app on server

        //set downloadmanager
        val updateURL = if(SERVER_MODE == SysInfo.ServerMode.PRD) APP_UPDATE_URL_PRD else APP_UPDATE_URL_DEV
        val downloadRequest = DownloadManager.Request(Uri.parse("$updateURL//${APK_NAME}_${version}.apk"))
        downloadRequest.setTitle(getString(R.string.app_name))

        //set destination
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${APK_NAME}_${version}.apk")
        else downloadRequest.setDestinationUri(uri)

        // get download service and enqueue file
        val manager =  this@SplashActivity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(downloadRequest)

        //set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                val install = Intent(Intent.ACTION_VIEW)?.apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

//                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    setDataAndType(uri,
                            manager.getMimeTypeForDownloadedFile(downloadId))
                }
                startActivity(install)
                unregisterReceiver(this)
                finish()
            }
        }

        //register receiver for when .apk download is compete
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}
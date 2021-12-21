package com.officeslip.ui.setting

import android.app.Application
import android.content.Context


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.base.BaseViewModel
import com.officeslip.data.local.AppInfoRepository
import com.officeslip.data.model.*
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.util.Common
import com.officeslip.util.agent.PreparedStatement
import com.officeslip.util.log.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository,
    private val appInfo: AppInfoRepository
    ) : BaseViewModel() {

    private val m_C = Common()

    val version : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }

    val developer : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }


    val detectDoc : LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }

    val sendBugReport : LiveEvent<UploadResult> by lazy {
        LiveEvent<UploadResult>()
    }

    lateinit var jdocNoForBugReport:String
    lateinit var proc_sendBugReport: Job

    fun stopAgentExecution() {
        if(this::proc_sendBugReport.isInitialized && proc_sendBugReport.isActive) { proc_sendBugReport.cancel() }
    }

    suspend fun uploadLogFile(file:File):BugReport? {
        var res:BugReport? = null
        val docIrn = agent.Get_IRN()

        if(agent.uploadFile(
                file.absolutePath,
                docIrn,
                "MOBILE_REPORT_X",
                BYTE_TRANSFER_FILE_DOCNO_START_IDX
        )) {
            res = BugReport(docIrn, file.length(), file.name)
        }
        return res
    }

    suspend fun insertLogFile(fileInfo:BugReport):Boolean {

        var res = false
        PreparedStatement(INSERT_BUG_REPORT).apply {
            setString(0, fileInfo.docIrn)
            setString(1, m_C.getDate("yyyyMMdd"))
            setString(2, AGENT_FOLDER)
            setString(3, jdocNoForBugReport)
            setString(4, fileInfo.fileName.substring(0, fileInfo.fileName.lastIndexOf(".")))
            setString(5, fileInfo.fileName)
            setString(6, "androidUser")
            setString(7, m_C.getDate("yyyyMMddhhmmsss"))
            setString(8, "${fileInfo.fileSize}")

        }.getQuery()?.let {query ->
            val resCnt = agent.setData(query)["Query"].asJsonObject["Cnt"].asInt

            if (resCnt <= 0) {
                Logger.error("Failed to insert report table.", null)
                return false
            }

        } ?: run {
            Logger.error("Failed to insert report table.",null)
            res = false
        }

        return res
    }

    fun sendBugReport() {

        jdocNoForBugReport = agent.Get_IRN()

        File(LOG_PATH).listFiles()?.let { list ->
            proc_sendBugReport = viewModelScope.launch(Dispatchers.IO) {
                if(list.filter{ false }.isNotEmpty()) {
                    sendBugReport.postValue(UploadResult(false, application.getString(R.string.failed_uplaod_bug_report)))
                }
                else {

                    try {
                        var res = true
                        list.forEach loop@{ logFile ->
                            val reportInfo = uploadLogFile(logFile)
                            if (reportInfo != null) {
                                if(!insertLogFile(reportInfo)) {
                                    res = false
                                    return@loop
                                }
                            } else {
                                res = false
                                return@loop
                            }
                        }

                        if(res) {
                            sendBugReport.postValue(UploadResult(true, ""))
                        }
                    }
                    catch (e: InterruptedException) {
                        Logger.error("User cancelled.", e)
                        sendBugReport.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
                    }
                    catch (e: CancellationException) {
                        Logger.error("User cancelled.", e)
                        sendBugReport.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
                    }
                    catch(e:Exception) {
                        Logger.error("Failed to insert report table.",null)
                        sendBugReport.postValue(UploadResult(false, application.getString(R.string.failed_uplaod_bug_report)))
                    }

//                    UploadResult(true, jdocNoForBugReport)
                }
            }
        } ?: run {

            sendBugReport.postValue(UploadResult(false, application.getString(R.string.no_bug_report)))
        }
    }


    fun setDetectDoc(flag:Boolean) {
        SysInfo.DETECT_DOC = flag

        viewModelScope.launch(Dispatchers.IO) {
            appInfo?.let {
                var value = if(flag){ "1" } else { "0" }
                it.upsert(AppInfo("DETECT_DOC", value))
            }
        }
    }

}
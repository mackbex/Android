package com.officeslip.ui.splash

import android.app.Application
import android.content.Context
import android.net.Uri


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.util.agent.PreparedStatement
import com.officeslip.base.BaseViewModel
import com.officeslip.data.local.AppInfoRepository
import com.officeslip.data.model.AppInfo
import com.officeslip.SysInfo
import com.officeslip.SysInfo.userInfo
//import com.officeslip.data.remote.agent.AgentService
import com.officeslip.data.model.AgentResponse
import com.officeslip.data.model.CheckVersion
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.util.Common
import com.officeslip.util.log.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository,
    private val appInfo: AppInfoRepository
    ) : BaseViewModel() {

    private val TAG = "SplashViewModel"

    private val m_C = Common()

    private val _appInitAppInfo = MutableLiveData<AgentResponse<Any>>()
    val initAppInfo:LiveData<AgentResponse<Any>>
        get() = _appInitAppInfo

    private val _appVersion = LiveEvent<AgentResponse<CheckVersion>>()
    val getVersion:LiveData<AgentResponse<CheckVersion>>
        get() = _appVersion

    private val _updateUserInfo = LiveEvent<AgentResponse<NetworkResFlag>>()
    val updateUserInfo:LiveData<AgentResponse<NetworkResFlag>>
        get() = _updateUserInfo

    val schemeProc: LiveEvent<AgentResponse<Boolean>> by lazy {
        LiveEvent<AgentResponse<Boolean>>()
    }

    fun updateUserInfo() {
        val query = PreparedStatement(GET_USER_INFO).apply {

            val (userId, corpNo) = guardLet(userInfo[userId]?.asString, userInfo[corpNo]?.asString) {
                _updateUserInfo.postValue(AgentResponse.error(application.getString(R.string.failed_update_user_info),null))
                return
            }
            setString(0,userId)
            setString(1,corpNo)
            setString(2, SysInfo.LANG)

        }.getQuery()

        _updateUserInfo.postValue(AgentResponse.loading(null))

        viewModelScope.launch(Dispatchers.IO) {
            try {
                agent.getData(query!!)["Row"]?.let { row ->

                    row.asJsonObject.entrySet().forEach { map ->
                        val key = map.key
                        val value = map.value.asString

                        appInfo.upsert(AppInfo(key, value))
                    }

                    userInfo = row.asJsonObject

                    val lang = if (USE_SYSTEM_LANGUAGE) {
                        m_C.getCurrentLocale(application)
                    } else {
                        DEFAULT_LANGUAGE
                    }
                    appInfo.upsert(AppInfo("LANG", lang))
                    _updateUserInfo.postValue(AgentResponse.success(null))

                } ?: run {
                    Logger.error("Update user info : Received data is null.", null)
                    _updateUserInfo.postValue(AgentResponse.error(application.getString(R.string.failed_update_user_info), null))
                }
            }
            catch (e:Exception) {
                Logger.error("Failed to update user Info.", e)
                _updateUserInfo.postValue(AgentResponse.error(application.getString(R.string.failed_update_user_info),null))
            }
        }
    }



    fun initAppInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            appInfo.let {
                val isLogged            = it.getItem("IS_LOGGED")?.VALUE ?: run { "0" }
                val autoLogin           = it.getItem("AUTO_LOGIN")?.VALUE ?: run { if(SysInfo.DETECT_DOC) { "1" } else {"0"} }
                val detectDoc           = it.getItem("DETECT_DOC")?.VALUE ?: run { if(SysInfo.DETECT_DOC) { "1" } else {"0"} }

//                    SysInfo.AUTO_LOGIN      = autoLogin == "1"

                userInfo.addProperty(loginId, it.getItem(loginId)?.VALUE ?: run { "" })
                userInfo.addProperty(userId, it.getItem(userId)?.VALUE ?: run { "" })
                userInfo.addProperty(corpNo, it.getItem(corpNo)?.VALUE ?: run { "" })

                if(isLogged == "1" && SysInfo.AUTO_LOGIN) {
                    SysInfo.IS_LOGGED = true
                }
                else {
                    SysInfo.IS_LOGGED = false
                    it.upsert(AppInfo("IS_LOGGED", "0"))
                }

                SysInfo.DETECT_DOC = detectDoc == "1"

                SysInfo.LANG = it.getItem("LANG")?.VALUE ?: run {
                    val lang = if(USE_SYSTEM_LANGUAGE) { m_C.getCurrentLocale(application) } else { DEFAULT_LANGUAGE }
                    it.upsert(AppInfo("LANG", lang))
                    lang
                }
            }
            _appInitAppInfo.postValue(AgentResponse.success(null))
        }
    }

    fun getAppVersion() {
//
        val query = PreparedStatement(GET_VERSION_INFO)
                .apply {
                    setString(0, "AN_VERSION")
                }.getQuery()
//
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var res = NetworkResFlag.NETWORK_DISABLED
                var serverVersion = "1.0.0"

                agent.getData(query!!)["Row"]?.let { row ->
                     try {
                        serverVersion = row.asJsonObject["ENV_VALUE"].asString
                    } catch (e: Exception) {
                        serverVersion = "1.0.0"
                    }
                    val localVersion = BuildConfig.VERSION_NAME.replace(".", "").toInt()

                    if (serverVersion.replace(".", "").toInt() > localVersion) {
                        res = NetworkResFlag.UPDATE_AVAILABLE
                    } else {
                        res = NetworkResFlag.VERSION_NEWEST
                    }
                } ?: run {
                    res = NetworkResFlag.VERSION_NEWEST
                }

                _appVersion.postValue(AgentResponse.success(CheckVersion(res, serverVersion)))

            } catch (e: java.lang.Exception) {
                Logger.error("Failed to check version Info.", e)
                _appVersion.postValue(AgentResponse.error("Failed to get data.", CheckVersion(NetworkResFlag.CHECK_FAILED, null)))
            }
        }
    }
}
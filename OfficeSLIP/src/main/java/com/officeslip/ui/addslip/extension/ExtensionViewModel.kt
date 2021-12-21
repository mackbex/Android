package com.officeslip.ui.addslip.extension

import android.app.Application
import android.content.Context


import androidx.lifecycle.*
import com.google.gson.JsonArray
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.base.BaseViewModel
import com.officeslip.util.agent.PreparedStatement
import com.officeslip.data.model.AgentResponse
import com.officeslip.data.model.UploadResult
import com.officeslip.data.model.User
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.util.Common
import com.officeslip.util.log.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExtensionViewModel @Inject constructor(
        private val application: Application,
        private val savedStateHandle: SavedStateHandle,
        val agent: AgentRepository
) : BaseViewModel() {

    private val m_C = Common()

    val keyword : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }

    private val _moveSlip = LiveEvent<AgentResponse<Boolean>>()
    val moveSlip:LiveData<AgentResponse<Boolean>>
        get() = _moveSlip


    private val _copySlip = LiveEvent<AgentResponse<Boolean>>()
    val copySlip:LiveData<AgentResponse<Boolean>>
        get() = _copySlip

    private val _userList = LiveEvent<AgentResponse<List<User>>>()
    val userList:LiveData<AgentResponse<List<User>>>
        get() = _userList


//    val filteredUserInfo = Transformations.switchMap(keyword) { it ->
//        getFilteredList(it)
//    }

    private lateinit var proc_getUserList: Job
    private lateinit var proc_moveSlip: Job
    private lateinit var proc_copySlip: Job

    fun stopAgentExecution() {
        if(this::proc_getUserList.isInitialized && proc_getUserList.isActive)     { proc_getUserList.cancel() }
        if(this::proc_moveSlip.isInitialized && proc_moveSlip.isActive)           { proc_moveSlip.cancel() }
        if(this::proc_copySlip.isInitialized && proc_copySlip.isActive)           { proc_copySlip.cancel() }
    }

    fun moveSlip(sdocNo:String, user:User) {
        PreparedStatement(MOVE_SLIP)
                .apply {
                    setString(0, sdocNo)
                    setString(1,"SDOC_NO")
                    setString(2,user.corpNo)
                    setString(3,user.userId)
                    setString(4, SysInfo.userInfo[corpNo].asString)
                    setString(5, SysInfo.userInfo[userId].asString)
//            setString(3, SysInfo.LANG)
//            setString(4, "")
                }.getQuery()?.let { query ->
                    _moveSlip.postValue(AgentResponse.loading(null))

                    proc_moveSlip = viewModelScope.launch(Dispatchers.IO) {

                        try {
                            val resCnt = agent.setData(query)["Query"].asJsonObject["Cnt"].asInt

                            if (resCnt <= 0) {
                                Logger.error("Failed to copy slip.", null)
                                _moveSlip.postValue(AgentResponse.error(application.getString(R.string.failed_move_slip)))
                            } else {
                                _moveSlip.postValue(AgentResponse.success(true))
                            }
                        }
                        catch(e:InterruptedException) {
                            Logger.error("User cancelled.", e)
                            _moveSlip.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                        }
                        catch (e: CancellationException) {
                            Logger.error("User cancelled.", e)
                            _moveSlip.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                        }
                        catch (e:java.lang.Exception) {
                            Logger.error("Failed to copy slip.", e)
                            _moveSlip.postValue(AgentResponse.error(application.getString(R.string.failed_move_slip)))
                        }
                    }
                }
    }

    fun copySlip(sdocNo:String, user:User) {
        PreparedStatement(COPY_SLIP)
                .apply {
                    setString(0, sdocNo)
                    setString(1, agent.Get_IRN("S"))
                    setString(2, user.corpNo)
                    setString(3, user.userId)
                    setString(4, "")
//            setString(3, SysInfo.LANG)
//            setString(4, "")
                }.getQuery()?.let { query ->
                    _copySlip.postValue(AgentResponse.loading(null))

                    proc_copySlip = viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val resCnt = agent.setData(query)["Query"].asJsonObject["Cnt"].asInt

                            if (resCnt <= 0) {
                                Logger.error("Failed to copy slip.", null)
                                _copySlip.postValue(AgentResponse.error(application.getString(R.string.failed_copy_slip)))
                            } else {
                                _copySlip.postValue(AgentResponse.success(true))
                            }
                        }
                        catch(e:InterruptedException) {
                            Logger.error("User cancelled.", e)
                            _copySlip.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                        }
                        catch (e: CancellationException) {
                            Logger.error("User cancelled.", e)
                            _copySlip.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                        }
                        catch (e:java.lang.Exception) {
                            Logger.error("Failed to copy slip.", e)
                            _copySlip.postValue(AgentResponse.error(application.getString(R.string.failed_copy_slip)))
                        }
                    }
                }
    }


    fun getUserList(curUserId:String, viewFlag: ViewFlag = ViewFlag.Add) {
        PreparedStatement(GET_USER_LIST)
                .apply {
                    setString(0, curUserId)
                    setString(1, SysInfo.LANG)
                    setString(2, "")
                    setString(3, "")
//            setString(3, SysInfo.LANG)
//            setString(4, "")
                }.getQuery()?.let { query ->

                    _userList.postValue(AgentResponse.loading(null))
                    proc_getUserList = viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val arUserList = mutableListOf<User>()
                            agent.getData(query)["Row"]?.let { row ->

                            if(row.isJsonObject) { JsonArray().apply { add(row) } } else { row.asJsonArray }.forEach { el ->
                                el.asJsonObject.let { item ->
                                        arUserList.add(
                                            User(
                                                    item[userId].asString,
                                                    item[userNm].asString,
                                                    item[partNo].asString,
                                                    item[partNm].asString,
                                                    item[corpNo].asString,
                                                    item[corpNm].asString,
                                                    "",
                                                    curUserId?.let{id -> id == item[userId].asString}
                                            )
                                     )
                                    }
                                }
                            }
                            _userList.postValue(AgentResponse.success(arUserList) as? AgentResponse<List<User>>)
                        }
                        catch(e:InterruptedException) {
                            Logger.error("User cancelled.", e)
                            _userList.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                        }
                        catch (e: CancellationException) {
                            Logger.error("User cancelled.", e)
                            _userList.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                        }
                        catch (e: Exception) {
                            Logger.error("Failed to get user list.", e)
                            _userList.postValue(AgentResponse.error(application.getString(R.string.failed_get_user_info)))
                        }
                    }
                } ?: run {
            Logger.error("Failed to get user list.", null)
            _userList.postValue(AgentResponse.error(application.getString(R.string.failed_get_user_info)))
        }
    }


    fun resetCheckState() {
        _userList.value?.data?.forEach {
            it.checked = false
        }
    }
}
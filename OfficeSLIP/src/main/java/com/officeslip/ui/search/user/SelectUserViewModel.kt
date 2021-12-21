package com.officeslip.ui.search.user

import android.app.Application
import android.content.Context


import androidx.lifecycle.*
import com.google.gson.JsonArray
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.base.BaseViewModel
import com.officeslip.util.agent.PreparedStatement
import com.officeslip.data.model.AgentResponse
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
class SelectUserViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    val agent: AgentRepository
    ) : BaseViewModel() {

    private val m_C = Common()

    val keyword : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }

    private val _userInfo = LiveEvent<AgentResponse<List<User>>>()
    val userInfo:LiveData<AgentResponse<List<User>>>
        get() = _userInfo

//    val filteredUserInfo = Transformations.switchMap(keyword) { it ->
//        getFilteredList(it)
//    }

    private lateinit var proc_getUserList: Job

    fun stopAgentExecution() {
        proc_getUserList.cancel()
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

            _userInfo.postValue(AgentResponse.loading(null))
             proc_getUserList = viewModelScope.launch(Dispatchers.IO) {
                try {
                    val arUserList = mutableListOf<User>()
                    agent.getData(query)["Row"]?.let { row ->


//                        if(viewFlag == ViewFlag.Search) {
//                            arUserList.add(
//                                User(
//                                    "- ALL -",
//                                    "-1",
//                                        curUserId?.let { code -> code == "-1" } ?: kotlin.run { false }
//                                )
//                            )
//                        }
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
                    _userInfo.postValue(AgentResponse.success(arUserList) as? AgentResponse<List<User>>)
                }
                catch(e:InterruptedException) {
                    Logger.error("User cancelled.", e)
                    _userInfo.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                }
                catch (e: CancellationException) {
                    Logger.error("User cancelled.", e)
                    _userInfo.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                }
                catch (e: Exception) {
                    Logger.error("Failed to get user list.", e)
                    _userInfo.postValue(AgentResponse.error(application.getString(R.string.failed_get_user_info)))
                }
            }
        } ?: run {
            Logger.error("Failed to get user list.", null)
            _userInfo.postValue(AgentResponse.error(application.getString(R.string.failed_get_user_info)))
        }
    }

//    private fun getFilteredList(str:String):LiveData<AgentResponse<List<User>>> {
//        val liveData = MutableLiveData<AgentResponse<List<User>>>()
//        liveData.run {
//            value = AgentResponse.success(userInfo.value?.data?.let {
//                it.filter { item ->
//                    item.userId.contains(str, true) || item.userNm.contains(str, true)
//                }
//            } ?: run {
//                userInfo.value?.data
//            })
//        }
//        return liveData
//
//    }

    fun resetCheckState() {
        _userInfo.value?.data?.forEach {
            it.checked = false
        }
    }
}
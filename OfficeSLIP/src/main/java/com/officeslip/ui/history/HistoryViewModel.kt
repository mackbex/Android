package com.officeslip.ui.history

import android.app.Application
import android.content.Context


import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.base.BaseViewModel
import com.officeslip.data.model.*
import com.officeslip.util.agent.PreparedStatement
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
class HistoryViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    val agent: AgentRepository
    ) : BaseViewModel() {

    private val m_C = Common()



    private val _historyList = LiveEvent<AgentResponse<List<HistoryItem>>>()
    val historyList:LiveData<AgentResponse<List<HistoryItem>>>
        get() = _historyList


    private lateinit var proc_getHistory: Job


    fun stopAgentExecution() {
        proc_getHistory.cancel()
    }


    fun getProperty(sdocNo:String) {
        val query = PreparedStatement(GET_HISTORY)
                .apply {
                    setString(0, sdocNo)
                    setString(1, "SDOC_NO")
                    setString(2, SysInfo.LANG)
                }.getQuery()?.let { query ->

                    _historyList.postValue(AgentResponse.loading(null))
                    proc_getHistory = viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val arHistoryList = mutableListOf<HistoryItem>()

                            agent.getData(query)["Row"]?.let { row ->
                                if(row.isJsonArray) {
                                    row.asJsonArray.forEach { el ->
                                        el.asJsonObject.run {
                                            arHistoryList.add(HistoryItem(
                                                    folder = this["FOLDER"].asString,
                                                    sdocName = this["SDOC_NAME"].asString,
                                                    title = this["HistoryNM"].asString,
                                                    historyUserId = this["HistoryUser"].asString,
                                                    historyUserNm = this["HistoryUserNM"].asString,
                                                    regTime = this["REG_TIME"].asString,
                                                    icon = this["ICON"].asString
                                            ))
                                        }
                                    }
                                }
                                else {
                                    row.asJsonObject.run {
                                        arHistoryList.add(HistoryItem(
                                                folder = this["FOLDER"].asString,
                                                sdocName = this["SDOC_NAME"].asString,
                                                title = this["HistoryNM"].asString,
                                                historyUserId = this["HistoryUser"].asString,
                                                historyUserNm = this["HistoryUserNM"].asString,
                                                regTime = this["REG_TIME"].asString,
                                                icon = this["ICON"].asString
                                        ))
                                    }
                                }

                            }

                            _historyList.postValue(AgentResponse.success(arHistoryList))
                        }
                        catch(e:InterruptedException) {
                            Logger.error("User cancelled.", e)
                            _historyList.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                        }
                        catch (e: CancellationException) {
                            Logger.error("User cancelled.", e)
                            _historyList.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                        }
                        catch (e: Exception) {
                            Logger.error("Failed to get property list.", e)
                            _historyList.postValue(AgentResponse.error(application.getString(R.string.failed_get_slipdoc_info)))
                        }
                    }
                }
    }
}
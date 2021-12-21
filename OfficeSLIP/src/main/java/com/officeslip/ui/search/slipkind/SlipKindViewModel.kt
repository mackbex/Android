package com.officeslip.ui.search.slipkind

import android.app.Application
import android.content.Context


import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.base.BaseViewModel
import com.officeslip.util.agent.PreparedStatement
import com.officeslip.data.model.SlipKindItem
import com.officeslip.data.model.AgentResponse
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
class SlipKindViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    val agent: AgentRepository
    ) : BaseViewModel() {

    private val m_C = Common()

    val keyword : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }

    private val _slipKind = LiveEvent<AgentResponse<List<SlipKindItem>>>()
    val slipKind:LiveData<AgentResponse<List<SlipKindItem>>>
        get() = _slipKind

    val filteredSlipKind = Transformations.switchMap(keyword) {it ->
        getFilteredList(it)
    }


    private lateinit var proc_getSlipKind: Job

    fun stopAgentExecution() {
        proc_getSlipKind.cancel()
    }


    fun getSlipKindList(kindLevel:String, parentNo:String, curSlipKindNo:String?, viewFlag: ViewFlag = ViewFlag.Add) {
        val query = PreparedStatement(GET_SLIPKIND_LIST)
            .apply {
            setString(0, SysInfo.userInfo[corpNo].asString)
            setString(1, kindLevel)
            setString(2, parentNo)
            setString(3, SysInfo.LANG)
            setString(4, "")
        }.getQuery()?.let { query ->

            _slipKind.postValue(AgentResponse.loading(null))
            proc_getSlipKind = viewModelScope.launch(Dispatchers.IO) {
                try {
                    val arKindList = mutableListOf<SlipKindItem>()
                    agent.getData(query)["Row"]?.let { row ->

                        if (viewFlag == ViewFlag.Search) {
                            arKindList.add(
                                    SlipKindItem(
                                            "- ALL -",
                                            "-1",
                                            curSlipKindNo?.let { code -> code == "-1" }
                                                    ?: kotlin.run { false }
                                    )
                            )
                        }
                        row.asJsonArray.forEach { el ->
                            el.asJsonObject.let { item ->

                                arKindList.add(
                                        SlipKindItem(
                                                item[kindNm].asString,
                                                item[kindNo].asString,
                                                (item["KIND_BARCODE"].asString == "Y"),
                                                curSlipKindNo?.let { code -> code == item[kindNo].asString }
                                                        ?: kotlin.run { false }
                                        )
                                )
                            }
                        }
                    }
                    _slipKind.postValue(AgentResponse.success(arKindList) as? AgentResponse<List<SlipKindItem>>)
                }
                catch(e:InterruptedException) {
                    Logger.error("User cancelled.", e)
                    _slipKind.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                }
                catch (e: CancellationException) {
                    Logger.error("User cancelled.", e)
                    _slipKind.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                }
                catch (e: Exception) {
                    Logger.error("Failed to get kind list.", e)
                    _slipKind.postValue(AgentResponse.error(application.getString(R.string.failed_get_slipkind)))
                }
            }
        }
    }

    private fun getFilteredList(str:String):LiveData<AgentResponse<List<SlipKindItem>>> {
        val liveData = MutableLiveData<AgentResponse<List<SlipKindItem>>>()
        liveData.run {
            value = AgentResponse.success(slipKind.value?.data?.let {
                it.filter { item ->
                    item.name.contains(str, true) || item.code.contains(str, true)
                }
            } ?: run {
                slipKind.value?.data
            })
        }
        return liveData

    }

    fun resetCheckState() {
        _slipKind.value?.data?.forEach {
            it.checked = false
        }
    }
}
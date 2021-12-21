package com.officeslip.ui.property

import android.app.Application

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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PropertyViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    val agent: AgentRepository
    ) : BaseViewModel() {

    private val m_C = Common()

    private val _slipInfo = LiveEvent<AgentResponse<SlipInfo>>()
    val slipInfo:LiveData<AgentResponse<SlipInfo>>
        get() = _slipInfo



    private lateinit var proc_getProperty: Job


    fun stopAgentExecution() {
        proc_getProperty.cancel()
    }


    fun getProperty(sdocNo:String, folder:String) {
        val query = PreparedStatement(GET_PROPERTY)
                .apply {
                    setString(0, sdocNo)
                    setString(1, folder)
                    setString(2, SysInfo.LANG)
                }.getQuery()?.let { query ->

                    _slipInfo.postValue(AgentResponse.loading(null))
                    proc_getProperty = viewModelScope.launch(Dispatchers.IO) {
                        try {

                            agent.getData(query)["Row"]?.let { row ->
                                row.asJsonObject.run {

                                    val slipInfo = SlipInfo(
                                        regTime = this["CABINET"].asString,
                                        regUserNm = this["REG_USERNM"].asString,
                                        regUserId = this["REG_USER"].asString,
                                        regPartNm = this["PART_NM"].asString,
                                        regPartNo = this["PART_NO"].asString,
                                        regCorpNm = this["CORP_NM"].asString,
                                        regCorpNo = this["CORP_NO"].asString,
                                        slipKindNm = this["SDOC_KINDNM"].asString,
                                        slipKindNo = this["SDOC_KIND"].asString,
                                        sdocStep = this["SDOC_STEP"].asString,
                                        sdocName = this["SDOC_NAME"].asString,
                                        sdocDevice = this["SDOC_DEVICE"].asString,
                                        sdocSecu = this["SDOC_SECU"].asString,
                                        sdocNo = this["SDOC_NO"].asString,
                                        jdocNo = this["JDOC_NO"].asString
                                    )

                                    _slipInfo.postValue(AgentResponse.success(slipInfo))
                                }
                            } ?: run {
                                Logger.error("Failed to get property list.", null)
                                _slipInfo.postValue(AgentResponse.error(application.getString(R.string.failed_get_slipdoc_info)))
                            }
                        }
                        catch(e:InterruptedException) {
                            Logger.error("User cancelled.", e)
                            _slipInfo.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                        }
                        catch (e: CancellationException) {
                            Logger.error("User cancelled.", e)
                            _slipInfo.postValue(AgentResponse.error(application.getString(R.string.user_cancelled)))
                        }
                        catch (e: Exception) {
                            Logger.error("Failed to get property list.", e)
                            _slipInfo.postValue(AgentResponse.error(application.getString(R.string.failed_get_slipdoc_info)))
                        }
                    }
                }
    }
}
package com.officeslip.ui.qr

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class QRReceiveViewModel @Inject constructor(
        private val application: Application,
        private val savedStateHandle: SavedStateHandle,
    val agent: AgentRepository
    ) : BaseViewModel() {

    private val m_C = Common()

    val qrInfo : LiveEvent<QRInfo> by lazy {
        LiveEvent<QRInfo>()
    }

    private val _getQRInfo = LiveEvent<AgentResponse<QRInfo>>()
    val getQRInfo:LiveData<AgentResponse<QRInfo>>
        get() = _getQRInfo

    private val _getSlipItem = LiveEvent<AgentResponse<ArrayList<Slip>>>()
    val getSlipItem:LiveData<AgentResponse<ArrayList<Slip>>>
        get() = _getSlipItem

    private val _changeStat = LiveEvent<AgentResponse<Boolean>>()
    val changeQRRecvStat:LiveData<AgentResponse<Boolean>>
        get() = _changeStat


    private lateinit var proc_getQRInfo: Job
    private lateinit var proc_changeQRRecvStat: Job


    fun stopAgentExecution() {
        if(this::proc_getQRInfo.isInitialized && proc_getQRInfo.isActive) { proc_getQRInfo.cancel() }
    }

    fun getToday():String {
        return m_C.getDate("yyyy-MM-dd")
    }

    fun changeRecvStatus(barcode:String, status:String) {

        PreparedStatement(CHANGE_QR_RECV_STATUS)
                .apply {

                    val stat = if(status.equals("Y", true)) { "N" } else { "Y" }

                    setString(0, barcode)
                    setString(1, SysInfo.userInfo[userId].asString)
                    setString(2, stat)
                }.getQuery()?.let { query ->

                    _changeStat.postValue(AgentResponse.loading())

                    proc_changeQRRecvStat = viewModelScope.launch(Dispatchers.IO) {
                        val resCnt = agent.setData(query)["Query"].asJsonObject["Cnt"].asInt

                        if (resCnt <= 0) {
                            Logger.error("Failed to change stat. barcode : $barcode", null)
                            _changeStat.postValue(AgentResponse.error(application.getString(R.string.failed_change_qr_recv_stat)))
                        }
                        else {
                            _changeStat.postValue(AgentResponse.success(true))
                        }
                    }
                } ?: run {
            _changeStat.postValue(AgentResponse.error(application.getString(R.string.failed_change_qr_recv_stat), null))
        }


    }

    fun getQRInfo(barcode:String) {
        PreparedStatement(GET_QR_INFO)
                .apply {
                    //3:39 PM 김한결: ProcImg_SearchRecv ‘SEARCH’, ‘\(g_SysInfo.corpID)’, ‘\(self.barCodeStr)’, ‘’, ‘’, ‘’, ‘\(g_SysLang)’
                    setString(0, "SEARCH")
                    setString(1, SysInfo.userInfo[corpNo].asString)
                    setString(2, barcode)
                    setString(3, "")
                    setString(4, "")
                    setString(5, "")
                    setString(6, SysInfo.LANG)
                }.getQuery()?.let { query ->

                    _getQRInfo.postValue(AgentResponse.loading())

                    proc_getQRInfo = viewModelScope.launch(Dispatchers.IO) {
                        agent.getData(query)["Row"]?.let { row ->

                            lateinit var info:QRInfo
                                //<ListData><Row><CABINET>2021-05-10</CABINET><SDOC_NO>S202105107A399132720027</SDOC_NO><CORP_NO>1000</CORP_NO><CORP_NM>(주)제이와이피엔터테인먼트 </CORP_NM><PART_NO>101010</PART_NO><PART_NM>개발</PART_NM><REG_USER>woonam</REG_USER><REG_USERNM>우남</REG_USERNM><SDOC_KIND>2000</SDOC_KIND><SDOC_KINDNM>의상내역서</SDOC_KINDNM><SDOC_STEP>0</SDOC_STEP><SDOC_FLAG>1</SDOC_FLAG><SDOC_URL>0</SDOC_URL><SDOC_AFTER>0</SDOC_AFTER><SDOC_FOLLOW>0</SDOC_FOLLOW><SDOC_COPY>0</SDOC_COPY><SDOC_COPYNM></SDOC_COPYNM><SDOC_SECU>1</SDOC_SECU><SDOC_SYSTEM>0</SDOC_SYSTEM><JDOC_INDEX></JDOC_INDEX><SDOC_NAME>IOS test</SDOC_NAME><JDOC_NO></JDOC_NO><SLIP_IRN>202105104E307132720027</SLIP_IRN><DOC_IRN>202105104E307132720027</DOC_IRN><SLIP_NO>0</SLIP_NO><SLIP_TITLE></SLIP_TITLE><SLIP_RECT>0,0,15436,10252</SLIP_RECT><SLIP_ROTATE>0</SLIP_ROTATE><DOC_NO>0</DOC_NO><KIND_COLOR>200,200,200</KIND_COLOR><KIND_FLAG>N</KIND_FLAG><SDOCNO_CNT>1</SDOCNO_CNT><SDOCNO_INDEX>1</SDOCNO_INDEX><RECV_STATUS>N</RECV_STATUS><ORG_FILE>0</ORG_FILE><ORG_FILENM>0</ORG_FILENM></Row></ListData>
                            if(row.isJsonArray) {

                                val item = row.asJsonArray[0].asJsonObject
                                info = QRInfo(
                                    item["CABINET"].asString,
                                    item["SDOC_NAME"].asString,
                                    item["SDOC_KIND"].asString,
                                    item["SDOC_KINDNM"].asString,
                                    item["JDOC_NO"].asString,
                                    item["PART_NO"].asString,
                                    item["PART_NM"].asString,
                                    item["REG_USER"].asString,
                                    item["REG_USERNM"].asString,
                                    item["RECV_STATUS"].asString,
                                    item["SDOC_NO"].asString
                                )
                            }

                            else {
                                info = QRInfo(
                                    row.asJsonObject["CABINET"].asString,
                                    row.asJsonObject["SDOC_NAME"].asString,
                                    row.asJsonObject["SDOC_KIND"].asString,
                                    row.asJsonObject["SDOC_KINDNM"].asString,
                                    row.asJsonObject["JDOC_NO"].asString,
                                    row.asJsonObject["PART_NO"].asString,
                                    row.asJsonObject["PART_NM"].asString,
                                    row.asJsonObject["REG_USER"].asString,
                                    row.asJsonObject["REG_USERNM"].asString,
                                    row.asJsonObject["RECV_STATUS"].asString,
                                    row.asJsonObject["SDOC_NO"].asString
                                )

                            }
                            _getQRInfo.postValue(AgentResponse.success(info))
                        }
                    }
                } ?: run {
                    _getQRInfo.postValue(AgentResponse.error(application.getString(R.string.failed_get_qr_info), null))
        }
    }


    fun getSlipItem(sdocNo:String) {

        PreparedStatement(GET_SLIP_DATA)
                .apply {
                    setString(0, sdocNo) //SDOC_NO
                    setString(1, "SDOC_NO") //SDOC_NO for Index
                    setString(2, SysInfo.LANG) //SDOC_NO for Index
                }.getQuery()?.let { query ->

                    _getSlipItem.postValue(AgentResponse.loading(null))
                    viewModelScope.launch(Dispatchers.IO) {
                        agent.getData(query)["Row"]?.let { row ->
                            val arSlipList = ArrayList<Slip>()
                            if (row.isJsonArray) {
                                row.asJsonArray.forEach { el ->
                                    el.asJsonObject.run {
                                        arSlipList.add(Slip(
                                                docIrn = this["DOC_IRN"].asString,
                                                sdocNo = this["SDOC_NO"].asString,
                                                slipFlag = ViewFlag.Search,
                                                docNo = this["DOC_NO"].asInt
                                        ))
                                    }
                                }
                            }
                            else {
                                row.asJsonObject.run {
                                    arSlipList.add(Slip(
                                            docIrn = this["DOC_IRN"].asString,
                                            sdocNo = this["SDOC_NO"].asString,
                                            slipFlag = ViewFlag.Search,
                                            docNo = this["DOC_NO"].asInt
                                    ))
                                }
                            }

                            _getSlipItem.postValue(AgentResponse.success(arSlipList))
                        } ?: run {
                            _getSlipItem.postValue(AgentResponse.error(application.getString(R.string.failed_get_search_slip)))
                        }
                    }

                } ?: run {
            _getSlipItem.postValue(AgentResponse.error(application.getString(R.string.failed_get_search_slip)))
        }
    }



    suspend fun downloadThumb(slip:Slip): Bitmap? {
        var bRes:Bitmap? = null
        try {
            agent.downloadFile(slip.docIrn, "${slip.docNo}", "IMG_SLIP_M")?.let { proc ->
                if(proc.isCompleted) {
                    BitmapFactory.decodeByteArray(proc.byte, 0, proc.byte.size).run {
                        ByteArrayOutputStream().use {
                            compress(Bitmap.CompressFormat.JPEG, 100, it)
//                                if(!isRecycled) {
//                                    recycle()
//                                }
                            bRes = m_C.decodeSampledBitmapFromResource(it.toByteArray())
                        }
                    }
                }
                else {
                    Logger.error("Failed to download thumb.", null)
                }
            } ?: run {
                Logger.error("Failed to download thumb.", null)
            }
        }
        catch(e:InterruptedException) {
            Logger.error("User cancelled.", e)
            bRes = null
        }
        catch(e:Exception) {
            Logger.error("downloadThumbz - exception occured.", e)
            bRes = null
        }
        return bRes
    }
}
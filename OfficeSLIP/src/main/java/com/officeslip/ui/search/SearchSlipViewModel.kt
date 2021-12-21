package com.officeslip.ui.search

import android.app.Application
import android.content.Context
import android.os.Environment


import androidx.lifecycle.*
import com.google.gson.JsonObject
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.base.BaseViewModel
import com.officeslip.data.model.*
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.util.Common
import com.officeslip.util.agent.PreparedStatement
import com.officeslip.util.log.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SearchSlipViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    val agent: AgentRepository
    ) : BaseViewModel() {

    val keyword : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }

    val filteredSearchResult = Transformations.switchMap(keyword) { it ->
        getFilteredList(it)
    }

    private val _searchResult = LiveEvent<AgentResponse<List<SearchSlipResultItem>>>()
    val searchResult: LiveData<AgentResponse<List<SearchSlipResultItem>>>
        get() = _searchResult

    private val _removeSlip = LiveEvent<AgentResponse<Int>>()
    val removeSlip: LiveData<AgentResponse<Int>>
        get() = _removeSlip

    private val _getSlipItem = LiveEvent<AgentResponse<ArrayList<Slip>>>()
    val getSlipItem:LiveData<AgentResponse<ArrayList<Slip>>>
        get() = _getSlipItem
//    private val _downAttach = LiveEvent<Resource<File>>()
//    val downAttach: LiveData<Resource<File>>
//        get() = _downAttach

    lateinit var proc_getSlipList: Job
    lateinit var proc_downloadAttach:Job
    lateinit var proc_removeSlip:Job
    lateinit var proc_getSlipImageData:Job

    private val m_C = Common()





    fun stopAgentExecution() {
        if(this::proc_getSlipList.isInitialized && proc_getSlipList.isActive)           { proc_getSlipList.cancel() }
        if(this::proc_downloadAttach.isInitialized && proc_downloadAttach.isActive)     { proc_downloadAttach.cancel() }
        if(this::proc_removeSlip.isInitialized && proc_removeSlip.isActive)             { proc_removeSlip.cancel() }
        if(this::proc_getSlipImageData.isInitialized && proc_getSlipImageData.isActive) { proc_getSlipImageData.cancel() }
    }

    fun getSlipList(searchSlipOption:SearchSlipOption) {

        val query = PreparedStatement(SEARCH_SLIP_LIST)
            .apply {

//                val step = searchSlipOption.stepUnused

                var step = ""
                var folder = ""

                searchSlipOption.run {
                    if(stepUsed["CHECKED"] as Boolean) {
                        if(m_C.isBlank(step)) { step = stepUsed["VALUE"].toString() } else { step += ",${stepUsed["VALUE"].toString()}" }
                    }
                    if(stepUnused["CHECKED"] as Boolean) {
                        if(m_C.isBlank(step)) { step = stepUnused["VALUE"].toString() } else { step += ",${stepUnused["VALUE"].toString()}" }
                    }
                    if(stepRemoved["CHECKED"] as Boolean) {
                        if(m_C.isBlank(step)) { step = stepRemoved["VALUE"].toString() } else { step += ",${stepRemoved["VALUE"].toString()}" }
                    }

                    if(folderSlip && folderAttach) {
                        folder = ""
                    }
                    else {
                        if(folderSlip) {
                            folder = "SLIPDOC"
                        }
                        else {
                            folder = "ADDFILE"
                        }
                    }

                    if(searchSlipOption.slipKindNo == "-1") {
                        searchSlipOption.slipKindNo = ""
                    }
                }


                setString(0, SysInfo.userInfo[corpNo].asString) //SEARCH_CORP
                setString(1, SysInfo.userInfo[partNo].asString) //SEARCH_PART
                setString(2, SysInfo.userInfo[userId].asString) //SEARCH_USER
                setString(3, "($step)")//STEP
                setString(4, searchSlipOption.secu)//SECU
                setString(5, searchSlipOption.fromDate)//FROM_DATE
                setString(6, searchSlipOption.toDate)//TO_DATE
                setString(7, searchSlipOption.slipKindNo)//SLIP_KIND
                setString(8, searchSlipOption.sdocName)//SDOC_NAME
                setString(9, folder)//FOLDER
                setString(10, searchSlipOption.lang)//LANG
                setString(11, SysInfo.userInfo[corpNo].asString)
                setString(12, SysInfo.userInfo[userId].asString)

//
//                setString(0, "%${searchSlipOption.sdocName}%")//SDOC_NAME
//                setString(1, m_C.getDate(SEARCH_DATE_FORMAT, searchSlipOption.fromDate))//FROM_DATE
//                setString(2, m_C.getDate(SEARCH_DATE_FORMAT, searchSlipOption.toDate))//TO_DATE
//                setString(3, SysInfo.userInfo[userId].asString) //SEARCH_USER
//                setString(4, SysInfo.userInfo[partNo].asString) //SEARCH_PART
//                setString(5, SysInfo.userInfo[corpNo].asString) //SEARCH_CORP

            }.getQuery()?.let { query ->
                _searchResult.postValue(AgentResponse.loading(null))

                proc_getSlipList = viewModelScope.launch(Dispatchers.IO) {
                    val arSlipList = mutableListOf<SearchSlipResultItem>()

                    agent.getData(query)["Row"]?.let { row ->
                      if(row.isJsonArray) {
                        row.asJsonArray.forEach { el ->
                            el.asJsonObject.run {
                                arSlipList.add(SearchSlipResultItem(
                                    if(this["ORG_USER"].isJsonNull || m_C.isBlank(this["ORG_USER"].asString)) { this["REG_USER"].asString }  else { this["ORG_USER"].asString } ,
                                    if(this["ORG_USERNM"].isJsonNull || m_C.isBlank(this["ORG_USERNM"].asString)) { this["REG_USERNM"].asString }  else { this["ORG_USERNM"].asString } ,
                                    this["PART_NO"].asString,
                                    this["PART_NM"].asString,
                                    this["CORP_NO"].asString,
                                    this["CORP_NM"].asString,
                                    this["SDOC_KIND"].asString,
                                    this["SDOC_KINDNM"].asString,
                                    this["SDOC_NAME"].asString,
                                    this["SDOC_NO"].asString,
                                    "",
                                    this["SDOC_STEP"].asString,
                                    this["CABINET"].asString,
                                    this["FOLDER"].asString,
                                    if(this["FOLDER"].asString == "SLIPDOC") { this["SLIP_CNT"].asString } else { "1" },
                                    this["ORG_IRN"]?.asString,
                                    this["ORG_FILE"]?.asString
                                ))
                            }
                        }
                    }
                    else {
                        row.asJsonObject.run {
                            arSlipList.add(SearchSlipResultItem(
                                if(this["ORG_USER"].isJsonNull || m_C.isBlank(this["ORG_USER"].asString)) { this["REG_USER"].asString }  else { this["ORG_USER"].asString } ,
                                if(this["ORG_USERNM"].isJsonNull || m_C.isBlank(this["ORG_USERNM"].asString)) { this["REG_USERNM"].asString }  else { this["ORG_USERNM"].asString } ,
                                this["PART_NO"].asString,
                                this["PART_NM"].asString,
                                this["CORP_NO"].asString,
                                this["CORP_NM"].asString,
                                this["SDOC_KIND"].asString,
                                this["SDOC_KINDNM"].asString,
                                this["SDOC_NAME"].asString,
                                this["SDOC_NO"].asString,
                                "",
                                this["SDOC_STEP"].asString,
                                this["CABINET"].asString,
                                this["FOLDER"].asString,
                                if(this["FOLDER"].asString == "SLIPDOC") { this["SLIP_CNT"].asString } else { "1" },
                                this["ORG_IRN"]?.asString,
                                this["ORG_FILE"]?.asString
                            ))
                        }
                    }
                }
                _searchResult.postValue(AgentResponse.success(arSlipList))
            }
        } ?: run {
            _searchResult.postValue(AgentResponse.error(application.getString(R.string.failed_get_search_slip)))
        }
    }


    fun getSlipItem(item:SearchSlipResultItem) {

        PreparedStatement(GET_SLIP_DATA)
        .apply {
            setString(0, item.sdocNo) //SDOC_NO
            setString(1, "SDOC_NO") //SDOC_NO for Index
            setString(2, SysInfo.LANG) //SDOC_NO for Index
        }.getQuery()?.let { query ->

                _getSlipItem.postValue(AgentResponse.loading(null))
                proc_getSlipImageData = viewModelScope.launch(Dispatchers.IO) {
                    agent.getData(query)["Row"]?.let { row ->
                        val arSlipList = ArrayList<Slip>()
                        if (row.isJsonArray) {
                            row.asJsonArray.forEach { el ->
                                el.asJsonObject.run {
                                    arSlipList.add(Slip(
                                        docIrn = this["DOC_IRN"].asString,
                                        sdocNo = this["SDOC_NO"].asString,
                                        slipFlag = ViewFlag.Search,
                                        docNo = this["DOC_NO"].asInt,
                                        slipInfo = item
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
                                    docNo = this["DOC_NO"].asInt,
                                    slipInfo = item
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

    private fun getFilteredList(str:String):LiveData<AgentResponse<List<SearchSlipResultItem>>> {
        val liveData = MutableLiveData<AgentResponse<List<SearchSlipResultItem>>>()
        liveData.run {
            value = AgentResponse.success(searchResult.value?.data?.let {
                it.filter { item ->
                    item.sdocName.contains(str, true)
                }
            } ?: run {
                searchResult.value?.data
            })
        }
        return liveData

    }
//
//    fun downloadAttach(item:SearchSlipResultItem, position:Int, progData: LiveEvent<AgentResponse<DownloadProgress>>) {
//
//        //_downAttach.postValue(Resource.loading())
//
//        val (orgFile, orgIrn) = guardLet(item.orgFile, item.orgIrn) {
//            progData.postValue(AgentResponse.error(context.getString(R.string.failed_download_file),null))
//            return
//        }
//
//        val fileNames = orgFile.split(".")
//
//        val fileName = fileNames[0]
//        val fileExt = fileNames[1]
//        //Download directory
////        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.apply {
////            if (!exists()) {
////                mkdirs()
////            }
////        }
//
//        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.apply {
//            if (!exists()) {
//                mkdirs()
//            }
//        }
//
//        proc_downloadAttach = agent.downloadFile(orgIrn, "0", "IMG_ORGFILE_X")
//            .debounce(1, TimeUnit.SECONDS)
//            .subscribeOn(Schedulers.io())
//            .subscribe ({ proc ->
//                    if(proc.isCompleted) {
//                        BufferedOutputStream(FileOutputStream(File(path, "$fileName.$fileExt"))).use {
//                            it.write(proc.byte)
//                            it.flush()
//                        }
//                    }
//                    else {
//                        progData.postValue(AgentResponse.loading(proc))
//                    }
//                }, {
//                    Logger.error("Failed to download addfile.", it)
//                progData.postValue(AgentResponse.error(context.getString(R.string.failed_download_file),null))
//                },
//                {
//                    progData.postValue(AgentResponse.success(null))
//                }
//            )
//    }
//
    fun removeSlip(item:SearchSlipResultItem, position:Int) {

        lateinit var query:PreparedStatement

        when(item.folder.toUpperCase())
        {
            "SLIPDOC" -> {
                query = PreparedStatement(REMOVE_SLIP)
            }
            else -> {
                query = PreparedStatement(REMOVE_ATTACH)
            }
        }

        query.apply {
            setString(0, item.sdocNo)
            setString(1, "SDOC_NO")
            setString(2, SysInfo.userInfo[corpNo].asString)
            setString(3, SysInfo.userInfo[userId].asString)
        }.getQuery()?.let { query ->
            _removeSlip.postValue(AgentResponse.loading())

            proc_removeSlip = viewModelScope.launch(Dispatchers.IO) {
                val resCnt = agent.setData(query)["Query"].asJsonObject["Cnt"].asInt

                if (resCnt <= 0) {
                    Logger.error("Failed to remove slip. sdocNo : ${item.sdocNo}", null)
                    _removeSlip.postValue(AgentResponse.error(application.getString(R.string.failed_remove_slip)))
                }
                else {
                    _removeSlip.postValue(AgentResponse.success(position))
                }
            }
        } ?: run {
            _removeSlip.postValue(AgentResponse.error(application.getString(R.string.failed_remove_slip)))
        }


    }
}
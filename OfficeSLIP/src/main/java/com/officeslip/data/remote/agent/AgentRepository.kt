package com.officeslip.data.remote.agent

import com.google.gson.JsonObject
import com.officeslip.data.model.DownloadProgress
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface AgentRepository {

    fun getSingleData(query:String): Single<JsonObject>
    suspend fun getData(query:String): JsonObject
    fun setSingleData(query:String): Single<JsonObject>
    suspend fun setData(query:String): JsonObject

//    fun getFlowableData(flag:String, query:String) :Flowable<JsonObject>

    fun uploadSingleFile(path:String, docIrn:String, docTable:String, docNo:Int?): Single<Boolean>
    suspend fun uploadFile(path:String, docIrn:String, docTable:String, docNo:Int?): Boolean
    fun uploadFileStream(path:String, docIrn:String, docTable:String, docNo:Int?): Observable<String>
    fun uploadFile(byteFile: ByteArray, fileName:String, docIrn:String, docTable:String, docNo:Int?): Single<Boolean>
    fun downloadSingleFile(docIrn:String, docNo:String, docTable:String): Observable<DownloadProgress>
    suspend fun downloadFile(docIrn:String, docNo:String, docTable:String):DownloadProgress?

    fun Get_IRN(flag:String = ""):String
    fun stopCurrentExecution()
}
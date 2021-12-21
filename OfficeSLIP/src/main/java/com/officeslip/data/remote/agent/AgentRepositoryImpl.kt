package com.officeslip.data.remote.agent

import com.google.gson.JsonObject
import com.officeslip.data.model.DownloadProgress
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class AgentRepositoryImpl @Inject constructor(private val agent: AgentDao) :
    AgentRepository {

    override fun getSingleData( query:String): Single<JsonObject> {
      return agent.getSingleData(query)
    }

    override suspend fun getData(query: String): JsonObject {
        return agent.getData(query)
    }

    override fun setSingleData( query:String): Single<JsonObject> {
        return agent.setSingleData(query)
    }

    override suspend fun setData(query: String): JsonObject {
        return agent.setData(query)
    }

//    override fun getFlowableData(flag: String, query: String): Flowable<JsonObject> {
//        return agent.getFlowableData(flag, query)
//    }

    override fun stopCurrentExecution() {
        agent.stopCurrentExecution()
    }

    override fun uploadSingleFile(path:String, docIrn:String, docTable:String, docNo:Int?): Single<Boolean> {
        return agent.uploadSingleFile(path, docIrn, docTable, docNo)
    }


    override suspend fun uploadFile(path:String, docIrn:String, docTable:String, docNo:Int?): Boolean {
        return agent.uploadFile(path, docIrn, docTable, docNo)
    }

    override fun uploadFile(byteFile: ByteArray, fileName:String, docIrn:String, docTable:String, docNo:Int?): Single<Boolean> {
        return agent.uploadFile(byteFile, fileName, docIrn, docTable, docNo)
    }

    override fun uploadFileStream(path:String, docIrn:String, docTable:String, docNo:Int?): Observable<String> {
        return agent.uploadFileStream(path, docIrn, docTable, docNo)
    }

    override fun downloadSingleFile(docIrn:String, docNo:String, docTable:String): Observable<DownloadProgress> {
        return agent.downloadSingleFile(docIrn, docNo, docTable)
    }


    override suspend fun downloadFile(docIrn:String, docNo:String, docTable:String): DownloadProgress? {
        return agent.downloadFile(docIrn, docNo, docTable)
    }

    override fun Get_IRN(flag:String): String {
        return agent.Get_IRN(flag)
    }
}


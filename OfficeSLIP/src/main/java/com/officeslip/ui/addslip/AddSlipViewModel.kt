package com.officeslip.ui.addslip

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.base.BaseViewModel
import com.officeslip.data.model.*
//import com.officeslip.data.remote.agent.AgentService
import com.officeslip.data.model.CollectionItem
import com.officeslip.data.model.AgentResponse
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.util.Common
import com.officeslip.util.agent.PreparedStatement
import com.officeslip.util.log.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.NullPointerException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class AddSlipViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository
) : BaseViewModel() {

    private val m_C = Common()

    val documentInfo: LiveEvent<Document> by lazy {
        LiveEvent<Document>()
    }
    private var slipList = ArrayList<Slip>()
    val slipCollectionItem : LiveEvent<CollectionItem<*>> by lazy {
        LiveEvent<CollectionItem<*>>()
    }

    val imageProcess: LiveEvent<AgentResponse<ImageProcess>> by lazy {
        LiveEvent<AgentResponse<ImageProcess>>()
    }

    val slipItem : LiveEvent<AgentResponse<Slip>> by lazy {
        LiveEvent<AgentResponse<Slip>>()
    }


    val checkBarcode : LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }

    val splitSlip : LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }

    val uploadProcess : LiveEvent<UploadProcess> by lazy {
        LiveEvent<UploadProcess>()
    }

    val uploadResult : LiveEvent<UploadResult> by lazy {
        LiveEvent<UploadResult>()
    }

    private lateinit var proc_uploadSlip:Job

    fun prepareCamera() {

        Single.fromCallable<ImageProcess> {
            application.packageManager?.let { manager ->
                if (manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    val path = File(UPLOAD_PATH).apply {
                        if (!exists()) {
                            mkdirs()
                        }
                    }

                    val imageFile = File.createTempFile(
                        "tmp",
                        ".jpg",
                        path
                    )

                    val imgUri = FileProvider.getUriForFile(
                        application,
                        BuildConfig.APPLICATION_ID + ".Util.GenericFileProvider",
                        File(imageFile.toString())
                    )

                    return@fromCallable ImageProcess(imgUri, UploadMethod.Camera, imageFile.absolutePath)
                } else {
                    throw Throwable()
                }
            } ?: run {
                throw Throwable()
            }

        }.subscribe({
            imageProcess.postValue(AgentResponse.success(it))
        }, {
            imageProcess.postValue(AgentResponse.error(application.getString(R.string.error_add_image)))
        })
    }

    fun prepareGallery() {

        File(UPLOAD_PATH).apply {
            if (!exists()) {
                mkdirs()
            }
        }

        Single.create<ImageProcess> { emitter ->
            application.packageManager?.let { manager ->

                emitter.onSuccess(ImageProcess(null, UploadMethod.Gallery))

            } ?: run {
                emitter.onError(NullPointerException())
            }

        }.subscribe({
            imageProcess.postValue(AgentResponse.success(it))
        }, {
            imageProcess.postValue(AgentResponse.error(application.getString(R.string.error_add_image)))
        })
    }

    fun convertToSlipImage(oldFile:File, stream:InputStream? = null) {

        Single.fromCallable {
            var docIrn = agent.Get_IRN()
            val imgName = "${docIrn}.JPG"
            val renamedFile = File(UPLOAD_PATH, imgName)
            if(oldFile.renameTo(renamedFile)) {

                val slip = m_C.saveOriginal(imgName, renamedFile, docIrn, stream)
                m_C.saveThumb(slip)
                return@fromCallable slip
//                m_C.saveThumb(m_C.saveOriginal(imgName, renamedFile, docIrn))
            }
            else {
                throw Throwable()
            }
        }
            .subscribeOn(Schedulers.io())
            .subscribe({

                slipItem.postValue(AgentResponse.success(it))
            },{
                slipItem.postValue(AgentResponse.error(application.getString(R.string.error_add_image)))
            })
    }

    fun addSlipItem(slip:Slip) {
        slipList.add(slip)
//        slipInfoPosts.notifyObserver()
    }
    fun setSlipItem(slip:List<Slip>) {
        slipList = slip as ArrayList<Slip>
    }

    fun clearSlipItem() {
        slipList.clear()
    }

//    fun removeSlipItem(slip:Slip) {
//        slipList.remove(slip)
////        slipInfoPosts.notifyObserver()
//    }

    fun getSlipList():ArrayList<Slip> {
        return slipList
    }




    suspend fun insertSlipDoc(size:Int):Boolean {

        try {
            val docInfo = documentInfo.value!!
            PreparedStatement(INSERT_SLIPDOC).apply {
                setString(0, docInfo.sdocNo) //SDOC_NO
                setString(1, docInfo.regCorpNo) //CORP_NO
                setString(2, docInfo.regPartNo) //PART_NO
                setString(3, docInfo.regUserId) //REG_USER
                setString(4, m_C.getDate("yyyyMM")) //SDOC_MONTH
                setString(5, docInfo.slipKindNo) //SDOC_KIND
                setString(6, m_C.convertSpecialChar(docInfo.sdocName)) //SDOC_NAME
                setString(7, "$size") //SLIP_CNT
                setString(8, docInfo.managerId) //ORG_USER
            }.getQuery()?.let { query ->

                val resCnt = agent.setData(query)["Query"].asJsonObject["Cnt"].asInt

                if (resCnt <= 0) {
                    Logger.error("Failed to upload slipdoc", null)
                    uploadResult.postValue(UploadResult(false, application.getString(R.string.upload_failed_upload_slipdoc)))
                    return false
                }
            }
        }
        catch(e:InterruptedException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e: CancellationException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch(e:Exception) {
            Logger.error("Failed to upload slipdoc.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.upload_failed_upload_slipdoc)))
            return false
        }
        return true
    }



    suspend fun uploadSlip(slip:Slip):Boolean {
        try {
//            slipList.forEach { slip ->
                reduceImageSize(slip.path)?.let {
                    slip.quality = it

                    val docIrn = agent.Get_IRN()
                    val oldSlip = File(slip.path)
                    val imgName = "${docIrn}.${IMG_EXT}"
                    val renamedFile = File(UPLOAD_PATH, imgName)
                    if (oldSlip.renameTo(renamedFile)) {
                        slip.docIrn = docIrn
                        slip.path = renamedFile.absolutePath
                    } else {
                        Logger.error("Failed upload slip.", null)
                        uploadResult.postValue(UploadResult(false, application.getString(R.string.upload_failed_upload_slip)))
                        return false
                    }
                    if (!agent.uploadFile(
                                    slip.path,
                                    slip.docIrn,
                                    "IMG_SLIP_X",
                                    BYTE_TRANSFER_FILE_DOCNO_START_IDX
                            )) {
                        Logger.error("Failed to upload slip.", null)
                        uploadResult.postValue(UploadResult(false, application.getString(R.string.upload_failed_upload_slip)))
                        return false
                    }
                }
//            }
        }
        catch (e: InterruptedException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e: CancellationException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e:Exception) {
            Logger.error("Failed upload slip.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.upload_failed_upload_slip)))
            return false
        }

        return true
    }

    suspend fun insertSlip(slip:Slip, index:Int):Boolean {
        try {
//            slipList.forEachIndexed { index, slip  ->
                PreparedStatement(INSERT_SLIP)
                .apply {
                    val rect = "0,0,${m_C.pixel2pt(slip.width)},${m_C.pixel2pt(slip.height)}"
                    val slipIrn = agent.Get_IRN("")
                    setString(0, slipIrn)
                    setString(1, slip.docIrn)
                    setString(2, documentInfo.value!!.sdocNo)
                    setString(3, "$index")
                    setString(4, slip.fileSize.toString())
                    setString(5, "Page ${index+1}")
                    setString(6, "1")
                    setString(7, "1")
                    setString(8, rect)
                    setString(9, "0")
                }.getQuery()?.let { query ->
                    val resCnt = agent.setData(query)["Query"].asJsonObject["Cnt"].asInt

                    if (resCnt <= 0) {
                        Logger.error("Failed to insert slip table.", null)
                        uploadResult.postValue(UploadResult(false, application.getString(R.string.upload_failed_upload_thumb)))
                        return false
                    }
                }
//            }
        }
        catch (e: InterruptedException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e: CancellationException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e:Exception) {
            Logger.error("Failed to insert slip table.", e)
            return false
        }
        return true
    }

    suspend fun uploadThumb(slip:Slip):Boolean {

        try {
//            slipList.forEach { slip ->
                if (!agent.uploadFile(
                                slip.thumbPath,
                                slip.docIrn,
                                "IMG_SLIP_M",
                                BYTE_TRANSFER_FILE_DOCNO_START_IDX
                        )) {
                    Logger.error("Failed to upload thumb.", null)
                    uploadResult.postValue(UploadResult(false, application.getString(R.string.upload_failed_upload_thumb)))
                    return false
                }
//            }
        }
        catch (e: InterruptedException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e: CancellationException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e:Exception) {
            Logger.error("Failed to upload thumb.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.upload_failed_upload_thumb)))
            return false
        }
        return true
    }

    suspend fun verifyThumbCnt(arDocIrn:ArrayList<String>):Boolean {
        try {
        PreparedStatement(CHECK_THUMB_CNT_BY_DOCIRN)
            .apply {
                setArray(0, arDocIrn)//slipList.map { it.docIrn } as ArrayList<String>)
            }.getQuery()?.let { query ->
                agent.getData(query)["Row"]?.let { row ->
                    row.asJsonObject.let { item ->
                        if (arDocIrn.size != item["CNT"].asInt) {
                            Logger.error("Uploaded thumb not matched.", null)
                            uploadResult.postValue(UploadResult(false, application.getString(R.string.upload_failed_upload_thumb)))
                            return false
                        }
                    }
                }
            }
        }
        catch (e: InterruptedException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e: CancellationException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e:Exception) {
            Logger.error("Failed to upload thumb.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.upload_failed_upload_thumb)))
            return false
        }
        return true
    }




    suspend fun insertBarcode():Boolean {
        try {
             PreparedStatement(INSERT_BARCODE_SLIP)
                    .apply {
                        val barcode = m_C.getBarcode(documentInfo.value!!.regUserId)
                        setString(0, documentInfo.value!!.sdocNo)
                        setString(1, barcode)

                    }.getQuery()?.let { query ->
                        val resCnt = agent.setData(query)["Query"].asJsonObject["Cnt"].asInt

                        if (resCnt <= 0) {
                            Logger.error("Failed to call barcode proc.", null)
                            uploadResult.postValue(UploadResult(false, application.getString(R.string.failed_create_barcode)))
                            return false
                        }
                        else {
                            PreparedStatement(ADD_HISTORY)
                                .apply {
                                    setString(0, documentInfo.value!!.sdocNo)
                                    setString(1, "")
                                    setString(2, "")
                                    setString(3, "S16")
                                    setString(4, SysInfo.userInfo[corpNo]?.asString ?: run { "" })
                                    setString(5, SysInfo.userInfo[userId]?.asString ?: run { "" })
                                }.getQuery()?.let { historyQuery ->
                                    val historyResCnt =
                                        agent.setData(historyQuery)["Query"].asJsonObject["Cnt"].asInt

//                                    if (historyResCnt <= 0) {
//                                        Logger.error("Failed to call barcode proc.", null)
//                                        uploadResult.postValue(
//                                            UploadResult(
//                                                false,
//                                                application.getString(R.string.failed_create_barcode)
//                                            )
//                                        )
//                                        return false
//                                    }
                                }
                        }
                    }

        }
        catch (e: InterruptedException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e: CancellationException) {
            Logger.error("User cancelled.", e)
            uploadResult.postValue(UploadResult(false, application.getString(R.string.user_cancelled)))
            return false
        }
        catch (e:Exception) {
            Logger.error("Failed to call barcode proc.", e)
            return false
        }
        return true
    }

    fun submit() {

        documentInfo.value?.sdocNo = agent.Get_IRN("S")
        documentInfo.value?.sdocName.apply {
            m_C.convertSpecialChar(this)
        }

        uploadProcess.postValue(
            UploadProcess(
                application.getString(R.string.progress_upload_original),
                1
            )
        )

        proc_uploadSlip = viewModelScope.launch(Dispatchers.IO) {


            val isSplit = splitSlip.value ?: run { false }

            if(isSplit) {
                uploadSplitSlip()
            }
            else {
                uploadBatchSlip()
            }
        }
    }

    suspend fun uploadSplitSlip() {

        var progress = false
        slipList.forEach{ slip ->
            if(uploadSlip(slip) && proc_uploadSlip.isActive) {
                if(uploadThumb(slip) && proc_uploadSlip.isActive) {
                    if (verifyThumbCnt(arrayListOf(slip.docIrn)) && proc_uploadSlip.isActive) {
                        documentInfo.value!!.sdocNo = agent.Get_IRN("S")
                        if (insertSlip(slip, BYTE_TRANSFER_FILE_DOCNO_START_IDX) && proc_uploadSlip.isActive) {
                            if (insertSlipDoc(1) && proc_uploadSlip.isActive) {
                                if (documentInfo.value?.barcodeYn ?: run { false }) {
                                    if (insertBarcode() && proc_uploadSlip.isActive) {
                                        progress = true
//                                        uploadResult.postValue(UploadResult(true, ""))
                                    }
                                } else {
                                    progress = true
                                }
                            }
                        }
                    }
                }
            }
        }

        uploadResult.postValue(UploadResult(progress, ""))
    }

    suspend fun uploadBatchSlip() {
        var progress = true
        slipList.forEach loop@{
            progress = uploadSlip(it)
            if(!progress || !proc_uploadSlip.isActive) {
                progress = false
                return@loop
            }
        }

        if(progress) {
            slipList.forEach loop@{
                progress = uploadThumb(it)
                if(!progress || !proc_uploadSlip.isActive) {
                    progress = false
                    return@loop
                }
            }

            if(progress) {
                val thumbDocIrn = slipList.map { it.docIrn } as ArrayList<String>
                progress = verifyThumbCnt(thumbDocIrn)
            }
        }

        if(progress) {
            documentInfo.value!!.sdocNo = agent.Get_IRN("S")

            slipList.forEachIndexed loop@{ index, slip ->
                progress = insertSlip(slip, index)
                if(!progress || !proc_uploadSlip.isActive) {
                    progress = false
                    return@loop
                }
            }
        }

        if(progress) {
            progress = insertSlipDoc(slipList.size)
        }

        if(progress && proc_uploadSlip.isActive && documentInfo.value?.barcodeYn ?: run { false }) {
            if(insertBarcode()) {
              uploadResult.postValue(UploadResult(progress, ""))
            }
        }
        else {
            uploadResult.postValue(UploadResult(progress, ""))
        }


    }

    fun stopAgentExecution() {
        proc_uploadSlip.cancel()
    }

    private fun reduceImageSize(path :String):Int?
    {
        val file = File(path)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)//ExifUtil().rotateBitmap(file.absolutePath)
        var imageSize_KB = file.length() // 1024
        var nDefaultQuality = 100

        while (imageSize_KB > UPLOAD_SLIP_SIZE_LIMIT)
        {
            nDefaultQuality -= 5
            FileOutputStream(file).use{
                bitmap.compress(Bitmap.CompressFormat.JPEG, nDefaultQuality, it)
                imageSize_KB = file.length() // 1000
                it.flush()
            }
        }
        return nDefaultQuality
    }


    fun resetDocumentInfo() {

        val userInfo = if(SysInfo.IS_SCHEME_CALL) SysInfo.schemeUserInfo else SysInfo.userInfo
        documentInfo.postValue(Document(
            userInfo[userId].asString ,
            userInfo[userNm].asString,
            userInfo[partNo].asString,
            userInfo[partNm].asString,
            userInfo[corpNo].asString,
            userInfo[corpNm].asString,
            "",
                "",
                false,
            userInfo[userId].asString,
            userInfo[userNm].asString,
            "",
            "",
            if(SysInfo.IS_SCHEME_CALL)  SysInfo.schemeJDocNo else "",
            ""
        ))
    }
//    private fun uploadSlip(slip:Slip):Boolean {
//
//
//
//
//    }
}
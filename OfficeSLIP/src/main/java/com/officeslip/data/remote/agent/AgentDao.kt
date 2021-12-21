package com.officeslip.data.remote.agent

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.officeslip.*
import com.officeslip.data.model.DownloadProgress
import com.officeslip.util.Common
import com.officeslip.util.cipher.ARIACipher
import com.officeslip.util.log.Logger
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jdom2.input.SAXBuilder
import org.xml.sax.InputSource
import java.io.*
import java.lang.NullPointerException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.text.DecimalFormat
import java.util.*

class AgentDao {

    object AgentFlag {
        const val SELECT = "S"
        const val UPDATE = "Q"
    }

    private val m_C = Common()
    private lateinit var socket:Socket

    fun stopCurrentExecution() {

        Logger.debug("Closing socket connection..")
        if(::socket.isInitialized && socket.isConnected) {
            socket.close()
        }
    }

    fun Get_IRN(flag:String = ""):String
    {

        var strUUID = UUID.randomUUID().toString()
        val sbRes = StringBuffer().apply {
            append(flag)
            append(m_C.getDate("yyyyMMdd"))
            append(strUUID.substring(strUUID.length - 5, strUUID.length).toUpperCase(Locale.getDefault()))
            append(m_C.getDate("HHmmssSSS"))
        }

        return sbRes.toString()
    }

    fun getFlowableData(flag: String, form: String) : Flowable<JsonObject> {

       return Flowable.create<JsonObject> ({ emitter ->

        },BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
    }


    suspend fun setData(form:String): JsonObject {
        var res: String? = null
        try {
            Logger.info("Flag : ${AgentFlag.UPDATE}, Form : $form")

            socket = Socket()
            socket.use {
                withContext(Dispatchers.IO) {
                    it.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
                    if (it.isConnected) {
                        it.getOutputStream()?.use { stream ->
                            stream.write(
                                convertToAgentForm(AgentFlag.UPDATE, form).toByteArray(
                                    charset(
                                        SysInfo.CHARSET
                                    )
                                )
                            )
                            stream.flush()
                            Scanner(
                                InputStreamReader(
                                    it.getInputStream(),
                                    SysInfo.CHARSET
                                )
                            ).use { scanner ->
                                scanner.useDelimiter("\\A")
                                res = if (scanner.hasNext()) scanner.next() else ""
                                res = (res as String).substring(14, (res as String).length)

                                it.close()
                            }
                        }
                    }
                }
            }

            Logger.info("Result : $res")

            // Thread.sleep(3000)
            res?.let { value ->
                if (m_C.isBlank(value)) {
                    throw NullPointerException()
                } else {
                    try {
                        m_C.xmlToJson(value).run {
                            if (size() <= 0) {
                                throw JsonParseException(value)
                            } else {
                                return this["Return"].asJsonObject
                            }
                        }
                    } catch (e: Exception) {
                        throw NullPointerException()
                    }
                }
            } ?: run {
                throw NullPointerException()
            }
        }
        catch (e:Exception) {
            Logger.error("setData - exception", e)
            throw e
        }
    }

    fun setSingleData(form:String): Single<JsonObject> {

        return Single.fromCallable {
            try {


                var res: String? = null

                Logger.info("Flag : ${AgentFlag.UPDATE}, Form : $form")

                socket = Socket()
                socket.use {
                    it.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
                    if (it.isConnected) {
                        it.getOutputStream()?.use { stream ->
                            stream.write(
                                convertToAgentForm(AgentFlag.UPDATE, form).toByteArray(
                                    charset(
                                        SysInfo.CHARSET
                                    )
                                )
                            )
                            stream.flush()
                            Scanner(
                                InputStreamReader(
                                    it.getInputStream(),
                                    SysInfo.CHARSET
                                )
                            ).use { scanner ->
                                scanner.useDelimiter("\\A")
                                res = if (scanner.hasNext()) scanner.next() else ""
                                res = (res as String).substring(14, (res as String).length)

                                it.close()
                            }
                        }
                    }
                }

                Logger.info("Result : $res")

                // Thread.sleep(3000)
                res?.let { value ->
                    if (m_C.isBlank(value)) {
                        throw NullPointerException()
                    } else {
                        try {
                            m_C.xmlToJson(value).run {
                                if (size() <= 0) {
                                    throw JsonParseException(value)
                                } else {
                                    this["Return"].asJsonObject
                                }
                            }
                        } catch (e: Exception) {
                            throw NullPointerException()
                        }
                    }
                } ?: run {
                    throw NullPointerException()
                }
            }
            catch (e:Exception) {
                throw NullPointerException()
            }
        }.subscribeOn(Schedulers.io())
    }

    suspend fun getData(form:String) : JsonObject {
        var res: String? = null
        try {
            Logger.info("Flag : ${AgentFlag.SELECT}, Form : $form")

            socket = Socket()
            socket.use {
                withContext(Dispatchers.IO) {
                    it.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
                    if (it.isConnected) {
                        it.getOutputStream()?.use { stream ->
                            stream.write(
                                convertToAgentForm(AgentFlag.SELECT, form).toByteArray(
                                    charset(
                                        SysInfo.CHARSET
                                    )
                                )
                            )
                            stream.flush()
                            Scanner(
                                InputStreamReader(
                                    it.getInputStream(),
                                    SysInfo.CHARSET
                                )
                            ).use { scanner ->
                                scanner.useDelimiter("\\A")
                                res = if (scanner.hasNext()) scanner.next() else ""
                                res = (res as String).substring(14, (res as String).length)

                                it.close()
                            }
                        }
                    }
                }
            }

            Logger.info("Result : $res")

            // Thread.sleep(3000)
            res?.let { value ->
                if (m_C.isBlank(value)) {
                    throw NullPointerException()
                } else {
                    try {
                        m_C.xmlToJson(value).run {
                            if (size() <= 0) {
                                throw JsonParseException(value)
                            } else {
                                return this["ListData"].let { el ->
                                    if (el.isJsonPrimitive || el.isJsonNull) {
                                        JsonObject()
                                    } else {
                                        el.asJsonObject
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        throw NullPointerException()
                    }
                }
              } ?: run {
                throw NullPointerException()
            }
        }
        catch (e:Exception) {
            Logger.error("getData exception - ", e)
            throw e
        }
    }

    fun getSingleData(form:String): Single<JsonObject> {

        return Single.fromCallable {
            var res: String? = null

            try {
                Logger.info("Flag : ${AgentFlag.SELECT}, Form : $form")

                socket = Socket()
                socket.use {
                    it.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
                    if (it.isConnected) {
                        it.getOutputStream()?.use { stream ->
                            stream.write(
                                convertToAgentForm(AgentFlag.SELECT, form).toByteArray(
                                    charset(
                                        SysInfo.CHARSET
                                    )
                                )
                            )
                            stream.flush()
                            Scanner(
                                InputStreamReader(
                                    it.getInputStream(),
                                    SysInfo.CHARSET
                                )
                            ).use { scanner ->
                                scanner.useDelimiter("\\A")
                                res = if (scanner.hasNext()) scanner.next() else ""
                                res = (res as String).substring(14, (res as String).length)

                                it.close()
                            }
                        }
                    }
                }

                Logger.info("Result : $res")

                // Thread.sleep(3000)
                res?.let { value ->
                    if (m_C.isBlank(value)) {
                        throw NullPointerException()
                    } else {
                        try {
                            m_C.xmlToJson(value).run {
                                if (size() <= 0) {
                                    throw JsonParseException(value)
                                } else {
                                    return@fromCallable this["ListData"].let { el ->
                                        if (el.isJsonPrimitive || el.isJsonNull) {
                                            JsonObject()
                                        } else {
                                            el.asJsonObject
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            throw NullPointerException()
                        }
                    }
                } ?: run {
                    throw NullPointerException()
                }
            }
            catch (e:Exception) {
                throw NullPointerException()
            }
        }.subscribeOn(Schedulers.io())
    }


    fun uploadSingleFile(path:String, docIrn:String, docTable:String, docNo:Int? = 0): Single<Boolean> {

        return Single.fromCallable {

            try {
            var nFileIdx                   = 0
            lateinit var fileName:String
            lateinit var byteFile: ByteArray

            nFileIdx = path.lastIndexOf("\\")
            if(nFileIdx <= 0)
            {
                nFileIdx		=	path.lastIndexOf("/")
            }
            fileName = path.substring(nFileIdx + 1)
            val uploadFile = File(path)
            try {
                byteFile = m_C.getBytesFromFile(uploadFile)
            }
            catch (e:Exception) {
                Logger.error("file upload - get file bytes", e)
                throw Throwable(e)
            }
//
            if(USE_ENCRYPTION)
            {
                try {
                    byteFile = ARIACipher().ARIA_Encode(byteFile)
                }
                catch(e :Exception)
                {
                    Logger.error("file upload - encryption", e)
                    throw Throwable()
                }
            }
//
            Logger.debug(
                "FileName : ${fileName}\n" +
                "FilePath : ${path}\n" +
                "FileIndex : ${nFileIdx}\n" +
                "DocTable : ${docTable}\n"
            )
            socket = Socket()

            socket.use { socket ->
                socket.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
                if (socket.isConnected) {
//                    //Write to socket
                    BufferedOutputStream(socket.getOutputStream()).use { buf ->

                        buf.run {
                            write(stream("create", docIrn, docTable, byteFile.size, fileName, docNo))
                            write(byteFile)
                            flush()
                        }
//
//                        //Receive output res from socket
                        BufferedInputStream(socket.getInputStream()).use {

                            val byteRes = ByteArray(2)
                            it.read(byteRes)
                            val strRes = String(byteRes)

                            if (strRes[0] != 'W' || strRes[1] != 'T') {
                                throw Throwable("Failed upload item.")
                            }
                            else {
                                return@fromCallable true
                            }
                        }
                    }
                }
                else {
                    throw SocketException()
                }
            }
            }
            catch (e:InterruptedException) {
                throw InterruptedException()
            }
            catch (e:Exception) {
                throw SocketException()
            }
        }.subscribeOn(Schedulers.io())
    }

    suspend fun uploadFile(path:String, docIrn:String, docTable:String, docNo:Int? = 0): Boolean {

            try {
                var nFileIdx                   = 0
                lateinit var fileName:String
                lateinit var byteFile: ByteArray

                nFileIdx = path.lastIndexOf("\\")
                if(nFileIdx <= 0)
                {
                    nFileIdx		=	path.lastIndexOf("/")
                }
                fileName = path.substring(nFileIdx + 1)
                val uploadFile = File(path)
                try {
                    byteFile = m_C.getBytesFromFile(uploadFile)
                }
                catch (e:Exception) {
                    Logger.error("file upload - get file bytes", e)
                    return false
                }
//
                if(USE_ENCRYPTION)
                {
                    try {
                        byteFile = ARIACipher().ARIA_Encode(byteFile)
                    }
                    catch(e :Exception)
                    {
                        Logger.error("file upload - encryption", e)
                        return false
                    }
                }
//
                Logger.debug(
                        "FileName : ${fileName}\n" +
                                "FilePath : ${path}\n" +
                                "FileIndex : ${nFileIdx}\n" +
                                "DocTable : ${docTable}\n"
                )
                socket = Socket()

                socket.use { socket ->
                    socket.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
                    if (socket.isConnected) {
//                    //Write to socket
                        BufferedOutputStream(socket.getOutputStream()).use { buf ->

                            buf.run {
                                write(stream("create", docIrn, docTable, byteFile.size, fileName, docNo))
                                write(byteFile)
                                flush()
                            }
//
//                        //Receive output res from socket
                            BufferedInputStream(socket.getInputStream()).use {

                                val byteRes = ByteArray(2)
                                it.read(byteRes)
                                val strRes = String(byteRes)

                                if (strRes[0] != 'W' || strRes[1] != 'T') {
                                    Logger.error("Failed upload item.", null)
                                    return false
                                }
                                else {
                                    return true
                                }
                            }
                        }
                    }
                    else {
                        Logger.error("Failed connect socket.", null)
                        return false
                    }
                }
            }
            catch (e:Exception) {
                Logger.error("UploadFile - exception.", e)
                throw e
            }
    }

    fun uploadFile(byteFile: ByteArray, fileName:String, docIrn:String, docTable:String, docNo:Int? = 0): Single<Boolean> {

        return Single.fromCallable {

            try {
                var file = byteFile
//                var nFileIdx                   = 0
//                lateinit var fileName:String
//                lateinit var byteFile: ByteArray



//                nFileIdx = path.lastIndexOf("\\")
//                if(nFileIdx <= 0)
//                {
//                    nFileIdx		=	path.lastIndexOf("/")
//                }
//                fileName = path.substring(nFileIdx + 1)
//                val uploadFile = File(path)
//                try {
//                    byteFile = m_C.getBytesFromFile(uploadFile)
//                }
//                catch (e:Exception) {
//                    Logger.error("file upload - get file bytes", e)
//                    throw Throwable(e)
//                }
//
                if(USE_ENCRYPTION)
                {
                    try {
                        file = ARIACipher().ARIA_Encode(file)
                    }
                    catch(e :Exception)
                    {
                        Logger.error("file upload - encryption", e)
                        throw Throwable()
                    }
                }
//
                Logger.debug(
                        "FileName : ${fileName}\n" +
//                                "FilePath : ${path}\n" +
//                                "FileIndex : ${nFileIdx}\n" +
                                "DocTable : ${docTable}\n"
                )
                socket = Socket()

                socket.use { socket ->
                    socket.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
                    if (socket.isConnected) {
//                    //Write to socket
                        BufferedOutputStream(socket.getOutputStream()).use { buf ->

                            buf.run {
                                write(stream("create", docIrn, docTable, file.size, fileName, docNo))
                                write(file)
                                flush()
                            }
//
//                        //Receive output res from socket
                            BufferedInputStream(socket.getInputStream()).use {

                                val byteRes = ByteArray(2)
                                it.read(byteRes)
                                val strRes = String(byteRes)

                                if (strRes[0] != 'W' || strRes[1] != 'T') {
                                    throw Throwable("Failed upload item.")
                                }
                                else {
                                    return@fromCallable true
                                }
                            }
                        }
                    }
                    else {
                        throw SocketException()
                    }
                }
            }
            catch (e:InterruptedException) {
                throw InterruptedException()
            }
            catch (e:Exception) {
                throw SocketException()
            }
        }.subscribeOn(Schedulers.io())
    }



    fun uploadFileStream(path:String, docIrn:String, docTable:String, docNo:Int? = 0): Observable<String> {

        return Observable.create<String>() { emitter ->

            try {
                var nFileIdx                   = 0
                lateinit var fileName:String
                lateinit var byteFile: ByteArray

                nFileIdx = path.lastIndexOf("\\")
                if(nFileIdx <= 0)
                {
                    nFileIdx		=	path.lastIndexOf("/")
                }
                fileName = path.substring(nFileIdx + 1)
                val uploadFile = File(path)
                try {
                    byteFile = m_C.getBytesFromFile(uploadFile)
                }
                catch (e:Exception) {
                    Logger.error("file upload - get file bytes", e)
                    throw Throwable(e)
                }
//
                if(USE_ENCRYPTION)
                {
                    try {
                        byteFile = ARIACipher().ARIA_Encode(byteFile)
                    }
                    catch(e :Exception)
                    {
                        Logger.error("file upload - encryption", e)
                        throw Throwable()
                    }
                }
//
                Logger.debug(
                        "FileName : ${fileName}\n" +
                                "FilePath : ${path}\n" +
                                "FileIndex : ${nFileIdx}\n" +
                                "DocTable : ${docTable}\n"
                )
                socket = Socket()

                socket.use { socket ->
                    socket.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
                    if (socket.isConnected) {
//                    //Write to socket
                        BufferedOutputStream(socket.getOutputStream()).use { buf ->

                            buf.run {
                                write(stream("create", docIrn, docTable, byteFile.size, fileName, docNo))
//                                write(byteFile)

                                var byteArr 	= ByteArray(4096)

                                var bis = ByteArrayInputStream(byteFile)
                                var nLength = 0
                                var progress = 0

                                while({ nLength = bis?.read(byteArr)!!; nLength}() > -1)
                                {
                                    buf.write(byteArr, 0, nLength)
                                    progress += nLength
                                    emitter.onNext("${progress} / ${byteFile.size}")
                                }
                                flush()
                            }
//
//                        //Receive output res from socket
                            BufferedInputStream(socket.getInputStream()).use {

                                val byteRes = ByteArray(2)
                                it.read(byteRes)
                                val strRes = String(byteRes)

                                if (strRes[0] != 'W' || strRes[1] != 'T') {
                                    throw Throwable("Failed upload item.")
                                }
                                else {
                                    return@create emitter.onComplete()
                                }
                            }
                        }
                    }
                    else {
                        throw SocketException()
                    }
                }
            }
            catch (e:InterruptedException) {
                throw InterruptedException()
            }
            catch (e:Exception) {
                throw SocketException()
            }
        }.subscribeOn(Schedulers.io())
    }

    fun uploadFileStream(byteFile: ByteArray, fileName:String, docIrn:String, docTable:String, docNo:Int? = 0): Observable<String> {

        return Observable.create<String>() { emitter ->

            try {
                var file = byteFile
//                var nFileIdx                   = 0
//                lateinit var fileName:String
//                lateinit var byteFile: ByteArray



//                nFileIdx = path.lastIndexOf("\\")
//                if(nFileIdx <= 0)
//                {
//                    nFileIdx		=	path.lastIndexOf("/")
//                }
//                fileName = path.substring(nFileIdx + 1)
//                val uploadFile = File(path)
//                try {
//                    byteFile = m_C.getBytesFromFile(uploadFile)
//                }
//                catch (e:Exception) {
//                    Logger.error("file upload - get file bytes", e)
//                    throw Throwable(e)
//                }
//
                if(USE_ENCRYPTION)
                {
                    try {
                        file = ARIACipher().ARIA_Encode(file)
                    }
                    catch(e :Exception)
                    {
                        Logger.error("file upload - encryption", e)
                        throw Throwable()
                    }
                }
//
                Logger.debug(
                        "FileName : ${fileName}\n" +
//                                "FilePath : ${path}\n" +
//                                "FileIndex : ${nFileIdx}\n" +
                                "DocTable : ${docTable}\n"
                )
                socket = Socket()

                socket.use { socket ->
                    socket.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
                    if (socket.isConnected) {
//                    //Write to socket
                        BufferedOutputStream(socket.getOutputStream()).use { buf ->

                            buf.run {
                                write(stream("create", docIrn, docTable, file.size, fileName, docNo))

                                var byteArr 	= ByteArray(4096)

                                var bis = ByteArrayInputStream(file)
                                var nLength = 0

                                while({ nLength = bis?.read(byteArr)!!; nLength}() > -1)
                                {
                                    buf.write(byteArr, 0, nLength)
                                    emitter.onNext("${byteArr.size} / ${file.size}")
                                }

                                flush()
                            }
//
//                        //Receive output res from socket
                            BufferedInputStream(socket.getInputStream()).use {

                                val byteRes = ByteArray(2)
                                it.read(byteRes)
                                val strRes = String(byteRes)

                                if (strRes[0] != 'W' || strRes[1] != 'T') {
                                    throw Throwable("Failed upload item.")
                                }
                                else {
                                    return@create emitter.onComplete()
//                                    return@fromCallable true
                                }

                            }
                        }
                    }
                    else {
                        throw SocketException()
                    }
                }
            }
            catch (e:InterruptedException) {
                throw InterruptedException()
            }
            catch (e:Exception) {
                throw SocketException()
            }
        }.subscribeOn(Schedulers.io())
    }




    private fun convertToAgentForm(flag: String = "S", query: String): String {

        return StringBuffer().apply {
            append(flag)
            for (i in 0 until 32) {
                append(" ")
            }

            append(DecimalFormat("000000000000").format(getQueryLength(query)))
            append(query)
        }.toString()
    }

    /**
     * get query length
     */

    private fun getQueryLength(strQuery: String): Int {
        var nRes = 0
        try {
            nRes = strQuery.toByteArray(charset(SysInfo.CHARSET)).size
        } catch (e: Exception) {
            //      Logger.WriteException(this@SocketManager.javaClass.name,"getQueryLength", e, 7)
        }
        return nRes
    }

    private fun stream(strOperation:String, strDocIRN:String, strTable:String, nFileSize:Int = 0, strFileName:String? = null, docNo:Int? = BYTE_TRANSFER_FILE_DOCNO_START_IDX):ByteArray
    {
        var sbParams: StringBuffer = StringBuffer().apply {
            append("op=")
            append(strOperation)
            append(";table=")
            append(strTable)
            append(";doc_no=")
            append(docNo)
            append(";doc_irn=")
            append(strDocIRN)
            if(!m_C.isBlank(strFileName))
            {
                append(";filename=")
                append(strFileName)
            }
            if(nFileSize > 0)
            {
                append(";filesize=")
                append(nFileSize)
                append(";&&")
            }
        }

        Logger.debug("stream : ${sbParams.toString()}")

        var sbBuffer: StringBuffer = StringBuffer().apply {
            append(AGENT_EDMS)
            append(getRegKeyAppendingBlank())
            append(DecimalFormat("000000000000").format(sbParams.length))
            append(sbParams)
        }

        Log.i("OfficeSLIP - UploadInfo",sbBuffer.toString())

        return sbBuffer.toString().toByteArray()
    }

    suspend fun downloadFile(docIrn:String, docNo:String, docTable:String): DownloadProgress?
    {
        lateinit var progress:DownloadProgress
        Log.i("Download - ", "DocIRN : $docIrn")
        Log.i("Download - ", "DocNo : $docNo")
        Log.i("Download - ", "DocTable : $docTable")

        var downStream = stream("download", docIrn, docTable, docNo = docNo.toInt())

        socket = Socket()

        socket.use { socket ->
            socket.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
            if (socket.isConnected) {
                //Write to socket
                BufferedOutputStream(socket.getOutputStream()).use { buf ->

                    buf.run {
                        write(downStream)
                        flush()
                    }

                    //Receive output res from socket
                    BufferedInputStream(socket.getInputStream()).use {

                        val byteRes = ByteArray(2)
                        it.read(byteRes)
                        val strRes = String(byteRes)

                        if (strRes[0] != 'W' || strRes[1] != 'T') {
                            throw Throwable("Failed download item.")
                        } else {
                            //Receive BufferLength
                            lateinit var resByte: ByteArray
                            var byteRes = ByteArray(12)
                            it.read(byteRes)

                            //Receive ByteArray
                            var byteLength = String(byteRes).toInt()
                            byteRes = ByteArray(byteLength)
                            it.read(byteRes)

                            var content = String(byteRes)

                            progress = DownloadProgress(0, byteRes)

                            StringReader(content)?.run {
                                SAXBuilder().build(InputSource(this))?.run {
                                    var elRoot = rootElement

                                    var lFileSize = elRoot.getAttribute("FileSize").longValue
                                    var strFileName =
                                            elRoot.getChild("Row").getChildText("FILENAME")

                                    Log.i("Download - FileName : ", lFileSize.toString())
                                    Log.i("Download - FileSize : ", strFileName)

                                    resByte = ByteArray(lFileSize.toInt())
                                    var bTemp = ByteArray(2)
                                    var nReadLength = 0
                                    var nPos = 0

                                    while ({
                                                nReadLength = it.read(bTemp)
                                                progress.progress = nPos * 100 / lFileSize.toInt()
                                                ; nReadLength
                                            }() != -1) {
                                        System.arraycopy(bTemp, 0, resByte, nPos, nReadLength)
                                        nPos += nReadLength
                                    }
                                }
                            }

                            if (USE_ENCRYPTION) {
                                try {
                                    resByte = ARIACipher().ARIA_Decode(resByte)
                                } catch (e: Exception) {
                                    Logger.error("file download - decryption", e)
                                    throw Throwable()
                                }
                            }

                            progress.isCompleted = true
                            progress.byte = resByte


                        }
                    }
                }
            } else {
                Logger.error("downloadFile - failed to connect socket.", null)
                return null
            }
        }

        return progress
    }

    fun downloadSingleFile(docIrn:String, docNo:String, docTable:String): Observable<DownloadProgress>
    {


        return Observable.create<DownloadProgress> { emit ->
            var bRes: ByteArray? = null
            Log.i("Download - ", "DocIRN : $docIrn")
            Log.i("Download - ", "DocNo : $docNo")
            Log.i("Download - ", "DocTable : $docTable")

            var downStream = stream("download", docIrn, docTable, docNo = docNo.toInt())

            socket = Socket()

            socket.use { socket ->
                socket.connect(InetSocketAddress(SysInfo.IP, SysInfo.PORT), SysInfo.TIMEOUT)
                if (socket.isConnected) {
                    //Write to socket
                    BufferedOutputStream(socket.getOutputStream()).use { buf ->

                        buf.run {
                            write(downStream)
                            flush()
                        }

                        //Receive output res from socket
                        BufferedInputStream(socket.getInputStream()).use {

                            val byteRes = ByteArray(2)
                            it.read(byteRes)
                            val strRes = String(byteRes)

                            if (strRes[0] != 'W' || strRes[1] != 'T') {
                                throw Throwable("Failed download item.")
                            } else {
                                //Receive BufferLength
                                lateinit var resByte: ByteArray
                                var byteRes = ByteArray(12)
                                it.read(byteRes)

                                //Receive ByteArray
                                var byteLength = String(byteRes).toInt()
                                byteRes = ByteArray(byteLength)
                                it.read(byteRes)

                                var content = String(byteRes)

                                val progress = DownloadProgress(0, byteRes)

                                StringReader(content)?.run {
                                    SAXBuilder().build(InputSource(this))?.run {
                                        var elRoot = rootElement

                                        var lFileSize = elRoot.getAttribute("FileSize").longValue
                                        var strFileName =
                                            elRoot.getChild("Row").getChildText("FILENAME")

                                        Log.i("Download - FileName : ", lFileSize.toString())
                                        Log.i("Download - FileSize : ", strFileName)

                                        resByte = ByteArray(lFileSize.toInt())
                                        var bTemp = ByteArray(2)
                                        var nReadLength = 0
                                        var nPos = 0

                                        while ({
                                                nReadLength = it.read(bTemp)
                                                progress.progress = nPos * 100 / lFileSize.toInt()
                                                emit.onNext(progress)
                                                ; nReadLength
                                            }() != -1) {
                                                System.arraycopy(bTemp, 0, resByte, nPos, nReadLength)
                                                nPos += nReadLength
                                        }
                                    }
                                }

                                if(USE_ENCRYPTION)
                                {
                                    try {
                                        resByte = ARIACipher().ARIA_Decode(resByte)
                                    }
                                    catch(e :Exception)
                                    {
                                        Logger.error("file download - decryption", e)
                                        throw Throwable()
                                    }
                                }

                                progress.isCompleted = true
                                progress.byte = resByte

                                emit.onNext(progress)
                                emit.onComplete()
                            }
                        }
                    }
                } else {
                    throw SocketException()
                }
            }
        }.subscribeOn(Schedulers.io())


//        return bRes
    }

    private fun getRegKeyAppendingBlank():String? {
        val sbKey = StringBuffer().apply {
            append(AGENT_SERVERKEY)
            append("                                ".toCharArray(), 0, 32 -  AGENT_SERVERKEY.length)
        }
        return sbKey.toString()
    }

}
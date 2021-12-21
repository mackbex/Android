package com.officeslip.ui.viewer

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View


import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.hadilq.liveevent.LiveEvent
import com.officeslip.ViewFlag
import com.officeslip.ViewMode
import com.officeslip.base.BaseViewModel
import com.officeslip.data.model.Slip
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.util.Common
import com.officeslip.util.log.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class SharedSlipViewerViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository
) : BaseViewModel() {
    val title : LiveEvent<String> by lazy {
        LiveEvent<String>()
    }

    val listSlip : LiveEvent<MutableList<Slip>> by lazy {
        LiveEvent<MutableList<Slip>>()
    }
//    val listSlipImage :LiveEvent<HashMap<String, Bitmap>> by lazy {
//        LiveEvent<HashMap<String, Bitmap>>()
//    }

    private val _mapSlipImage = LiveEvent<HashMap<String, Bitmap>>().apply {
        value = HashMap<String,Bitmap>()
    }
    val mapSlipImage: LiveData<HashMap<String, Bitmap>>
        get() = _mapSlipImage


    val curIdx : LiveEvent<Int> by lazy {
        LiveEvent<Int>()
    }
    val moveIdx : LiveEvent<Int> by lazy {
        LiveEvent<Int>()
    }
//
//    val curSlip : LiveEvent<Slip> by lazy {
//        LiveEvent<Slip>()
//    }

    val viewMode : LiveEvent<ViewMode> by lazy {
        LiveEvent<ViewMode>()
    }

    val viewFlag : LiveEvent<ViewFlag> by lazy {
        LiveEvent<ViewFlag>()
    }

    private val m_C = Common()


    suspend fun downloadOriginal(slip:Slip, imageView:View): Bitmap? {
      var bRes:Bitmap? = null
        try {
            agent.downloadFile(slip.docIrn, "${slip.docNo}", "IMG_SLIP_X")?.let { proc ->
                if(proc.isCompleted) {
                    BitmapFactory.decodeByteArray(proc.byte, 0, proc.byte.size).run {
                        ByteArrayOutputStream().use {
                            compress(Bitmap.CompressFormat.JPEG, 100, it)
//                                if(!isRecycled) {
//                                    recycle()
//                                }
                            bRes = m_C.decodeSampledBitmapFromResource(it.toByteArray(),imageView.width, imageView.height)
                        }
                    }
                }
                else {
                    Logger.error("Failed to download original.", null)
                }
            } ?: run {
                Logger.error("Failed to download original.", null)
            }
        }
        catch(e:InterruptedException) {
            Logger.error("User cancelled.", e)
            bRes = null
        }
        catch(e:Exception) {
            Logger.error("downloadOriginal - exception occured.", e)
            bRes = null
        }
        return bRes
    }


    suspend fun setImageOnView(item: Slip):Bitmap?{

        return Common().run {
            fileToByte(item.path)?.let {
                decodeSampledBitmapFromResource(it)
            } ?: run {
                null
            }
        }
    }
}
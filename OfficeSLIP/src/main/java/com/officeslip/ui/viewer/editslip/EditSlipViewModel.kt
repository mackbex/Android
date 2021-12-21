package com.officeslip.ui.viewer.editslip

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF


import androidx.lifecycle.SavedStateHandle
import com.hadilq.liveevent.LiveEvent
import com.officeslip.EditMode
import com.officeslip.R
import com.officeslip.SysInfo
import com.officeslip.base.BaseViewModel
import com.officeslip.data.model.AgentResponse
import com.officeslip.data.model.Slip
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.util.Common
import com.officeslip.util.Crop
import com.officeslip.util.opencv.RectFinder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class EditSlipViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository
) : BaseViewModel(){

    private val m_C = Common()
    val crop = Crop()

    val curMode : LiveEvent<EditMode> by lazy {
        LiveEvent<EditMode>()
    }



    val curSlip : LiveEvent<Slip> by lazy {
        LiveEvent<Slip>()
    }

    val detectDoc:LiveEvent<AgentResponse<List<PointF>>> by lazy {
        LiveEvent<AgentResponse<List<PointF>>>()
    }
//
    fun detectRect(slip: Slip) {

            Single.create<Bitmap> { emitter ->
                m_C.run {
                    fileToByte(slip.path)?.let {
                        decodeSampledBitmapFromResource(it)?.let { bitmap ->
                            emitter.onSuccess(bitmap)
                        } ?: run {
                            emitter.onError(Throwable())
                        }
                    } ?: run {
                        emitter.onError(Throwable())
                    }
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe({bitmap ->

                val mat = crop.bitmapToMat(bitmap)
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }

                val points = ArrayList<PointF>()
                if(SysInfo.DETECT_DOC) {
                    val rectFinder = RectFinder(0.2, 0.98)
                    val rectangle = rectFinder.findRectangle(mat)

                    if (rectangle != null) {

                        //                        val wRatio = bitmap.width.toFloat() / destBitmap.width.toFloat()
                        //                        val hRatio = bitmap.height.toFloat() / destBitmap.height.toFloat()

                        crop.sortPoints(rectangle.toList()).forEach { point ->
                            if (point != null) {
                                points.add(PointF(point.x.toFloat(), point.y.toFloat()))
                            }
                        }

                        mat.release()
                        rectangle.release()

                        detectDoc.postValue(AgentResponse.success(points))
                    } else {
                        mat.release()
                        detectDoc.postValue(AgentResponse.error(application.getString(R.string.failed_detect_document)))
                    }
                }
                else {
                    points.add(PointF(0.0f, 0.0f))
                    points.add(PointF(0.0f, 0.0f))
                    points.add(PointF(0.0f, 0.0f))
                    points.add(PointF(0.0f, 0.0f))
                    detectDoc.postValue(AgentResponse.success(points))
                }
            },{
                detectDoc.postValue(AgentResponse.error(application.getString(R.string.failed_load_image)))
        })
    }
}

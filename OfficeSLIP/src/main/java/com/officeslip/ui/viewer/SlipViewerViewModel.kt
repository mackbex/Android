package com.officeslip.ui.viewer

import android.app.Application
import android.content.Context


import androidx.lifecycle.SavedStateHandle
import com.officeslip.base.BaseViewModel
import com.officeslip.data.local.AppInfoRepository
import com.officeslip.data.remote.agent.AgentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

@HiltViewModel
class SlipViewerViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository,
    private val appInfo: AppInfoRepository
    ) : BaseViewModel(){

//    private val m_C = Common()
//    private val crop = Crop()
//
//    val title : LiveEvent<String> by lazy {
//        LiveEvent<String>()
//    }
//
//    val listSlip : LiveEvent<MutableList<Slip>> by lazy {
//        LiveEvent<MutableList<Slip>>()
//    }
//
//    val curIdx : LiveEvent<Int> by lazy {
//        LiveEvent<Int>()
//    }
////
////    val curSlip : LiveEvent<Slip> by lazy {
////        LiveEvent<Slip>()
////    }
//
//    val viewMode : LiveEvent<ViewMode> by lazy {
//        LiveEvent<ViewMode>()
//    }
//
//    val viewFlag : LiveEvent<ViewFlag> by lazy {
//        LiveEvent<ViewFlag>()
//    }
//
//    val editMode : LiveEvent<EditMode> by lazy {
//        LiveEvent<EditMode>()
//    }
//
//    val detectDoc:LiveEvent<Resource<List<PointF>>> by lazy {
//        LiveEvent<Resource<List<PointF>>>()
//    }
//
//    fun detectRect(slip: Slip) {
//
//        val res :ArrayList<Crop>
//        Single.create<Bitmap> { emitter ->
//            m_C.run {
//                fileToByte(slip.path)?.let {
//                    decodeSampledBitmapFromResource(it)?.let { bitmap ->
//                        emitter.onSuccess(bitmap)
//                    } ?: run {
//                        emitter.onError(Throwable())
//                    }
//                } ?: run {
//                    emitter.onError(Throwable())
//                }
//            }
//        }
//        .subscribeOn(Schedulers.io())
//        .subscribe({bitmap ->
//
//            val mat = crop.bitmapToMat(bitmap)
//            if (!bitmap.isRecycled) {
//                bitmap.recycle()
//            }
//
//            val rectFinder = RectFinder(0.2, 0.98)
//            val rectangle = rectFinder.findRectangle(mat)
//
//            if (rectangle != null) {
//
////                        val wRatio = bitmap.width.toFloat() / destBitmap.width.toFloat()
////                        val hRatio = bitmap.height.toFloat() / destBitmap.height.toFloat()
//
//                val points = ArrayList<PointF>()
//                crop.sortPoints(rectangle.toList()).forEach { point ->
//                    if (point != null) {
//                        points.add(PointF(point.x.toFloat(), point.y.toFloat()))
//                    }
//                }
//
//                mat.release()
//                rectangle.release()
//
//                detectDoc.postValue(Resource.success(points))
//            }
//            else {
//                detectDoc.postValue(Resource.error(context.getString(R.string.failed_detect_document)))
//            }
//        },{
//            detectDoc.postValue(Resource.error(context.getString(R.string.failed_load_image)))
//        })
//    }
//
////        Single.create<ArrayList<PointF>?> { emitter ->
////            m_C.run {
////                fileToByte(slip.path)?.let {
////                    decodeSampledBitmapFromResource(it)?.let { bitmap ->
////                        val mat = crop.bitmapToMat(bitmap)
////                        if (!bitmap.isRecycled) {
////                            bitmap.recycle()
////                        }
////
////                        val rectFinder = RectFinder(0.2, 0.98)
////                        val rectangle = rectFinder.findRectangle(mat)
////
////                        if (rectangle != null) {
////
//////                        val wRatio = bitmap.width.toFloat() / destBitmap.width.toFloat()
//////                        val hRatio = bitmap.height.toFloat() / destBitmap.height.toFloat()
////
////                            val points = ArrayList<PointF>()
////                            crop.sortPoints(rectangle.toList()).forEach { point ->
////                                if (point != null) {
////                                    points.add(PointF(point.x.toFloat(), point.y.toFloat()))
////                                }
////                            }
////
////                            mat.release()
////                            rectangle.release()
////
////                            emitter.onSuccess(points)
////                        }
////                        else {
////                            emitter.onSuccess(null)
////                        }
////                    } ?: run { emitter.onError() }
////                }
////        }
////            .subscribeOn(Schedulers.io())
////            .observeOn(AndroidSchedulers.mainThread())
////            .subscribe({
////                layout.findViewById<TouchImageView>(R.id.image)?.apply{
////                    setImageBitmap(it)
////                    setZoom(1f) //Important
////                }
////                prog.visibility = View.GONE
////            },{
////                prog.visibility = View.GONE
////            })
//
//
////        var res:ArrayList<PointF>? = null
//
//
////        }
////    }
}

package com.officeslip.di.binding

import android.graphics.Bitmap
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.officeslip.R
import com.officeslip.ViewFlag
import com.officeslip.ViewMode
import com.officeslip.base.BaseRecyclerView
import com.officeslip.base.subclass.DelegateImageView
import com.officeslip.base.subclass.TouchImageView
import com.officeslip.data.model.Slip
import com.officeslip.data.model.AgentResponse
import com.officeslip.ui.qr.QRReceiveActivity
import com.officeslip.ui.qr.QRReceiveViewModel
import com.officeslip.ui.viewer.SharedSlipViewerViewModel
import com.officeslip.ui.viewer.thumb.ThumbViewerFragment
import com.officeslip.util.Common
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

@BindingAdapter("init")
fun init(view: RecyclerView, res: AgentResponse<List<*>>?) {
    res?.let {
        it.data?.run {
            (view.adapter as? BaseRecyclerView.Adapter<Any, *>)?.run {
                replaceAll(it.data as ArrayList)
                notifyDataSetChanged()
            }
        } ?: run {
            (view.adapter as? BaseRecyclerView.Adapter<Any, *>)?.run {
                clearItem()
                notifyDataSetChanged()
            }
        }
    } ?: run {
        (view.adapter as? BaseRecyclerView.Adapter<Any, *>)?.run {
            clearItem()
            notifyDataSetChanged()
        }
    }
}


@BindingAdapter("slip", "sharedVm")
fun slip(layout: ConstraintLayout, item: Slip, sharedVm:SharedSlipViewerViewModel) {

    val prog = layout.findViewById<ProgressBar>(R.id.prog_image)
    val imageView = layout.findViewById<TouchImageView>(R.id.image)
    val ratio = 1

    prog.visibility = View.VISIBLE


    sharedVm.viewModelScope.launch(Dispatchers.IO) {
        val bitmap: Bitmap? = if (item.slipFlag == ViewFlag.Search) {
//        var imageBitmap:Bitmap? = null
            val imageKey = "${item.docIrn}_${item.docNo}"

            sharedVm.mapSlipImage.value?.let {
                it[imageKey]
            } ?: run {
                sharedVm.downloadOriginal(item, imageView)
            }
        } else {
            sharedVm.setImageOnView(item)
        }

        bitmap?.let { imageBitmap ->
            var image: Bitmap?

            imageBitmap.apply {
                val stream = ByteArrayOutputStream()
                compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()

                image = Common().decodeSampledBitmapFromResource(byteArray, (imageView.width * ratio).toInt(), (imageView.height * ratio).toInt())
            }
            imageBitmap.recycle()

            image?.apply {
                if (item.slipFlag == ViewFlag.Search) {
                    val imageKey = "${item.docIrn}_${item.docNo}"
                    val mapSlipImage = sharedVm.mapSlipImage.value ?: run { throw Throwable() }
                    mapSlipImage[imageKey] = this
                }
                launch(Dispatchers.Main) {
                    showSlipItem(ViewMode.View, layout, this@apply, item)
                }
            } ?: run {
                launch(Dispatchers.Main) {
                    sharedVm.showSnackbar("Failed to load image.")
                }
            }


        } ?: run {
            launch(Dispatchers.Main) {
                sharedVm.showSnackbar("Failed to load image.")
            }
        }
    }
}


@BindingAdapter("thumb", "viewModel", "position","activity")
fun thumb(layout: ConstraintLayout, item: Slip, viewModel: QRReceiveViewModel, position:Int, activity:QRReceiveActivity) {

    layout.findViewById<TouchImageView>(R.id.image)?.apply {
        setTouchEnable(false)
    }

    val imageView = layout.findViewById<TouchImageView>(R.id.image)
    val prog = layout.findViewById<ProgressBar>(R.id.prog_image)
    prog.visibility = View.VISIBLE

    layout.findViewById<TextView>(R.id.tv_title).text = layout.context.getString(R.string.slip)
//
//    val imageKey = "${item.docIrn}_${item.docNo}"
//    val mapImage = sharedVm.mapSlipImage.value ?: run {
//        sharedVm.showSnackbar("Failed to load image.")
//        return
//    }
//
//
//    mapImage[imageKey]?.let {
//        showSlipItem(ViewMode.Thumb, layout = layout, image = it)
//    } ?: run {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            viewModel.downloadThumb(item)?.let {
                val image = it
                launch(Dispatchers.Main) {
                    showSlipItem(ViewMode.Thumb, layout = layout, image = image)
//                    activity.binding.layoutMainCell.rcThumb.adapter?.notifyItemChanged(position)
                }
//            } ?: run {
//                launch(Dispatchers.Main) {
//                    sharedVm.showSnackbar("Failed to load image.")
//                }
//            }
        }
    }
}


@BindingAdapter("thumb", "sharedVm", "position", "fragment")
fun thumb(layout: ConstraintLayout, item: Slip, sharedVm: SharedSlipViewerViewModel, position:Int, fragment: ThumbViewerFragment) {

    layout.findViewById<TouchImageView>(R.id.image)?.apply {
        setTouchEnable(false)
    }

    val imageView = layout.findViewById<TouchImageView>(R.id.image)
    val prog = layout.findViewById<ProgressBar>(R.id.prog_image)
    prog.visibility = View.VISIBLE

    layout.findViewById<TextView>(R.id.tv_title).text = ""

    val imageKey = "${item.docIrn}_${item.docNo}"
    val mapImage = sharedVm.mapSlipImage.value ?: run {
        sharedVm.showSnackbar("Failed to load image.")
        return
    }


    mapImage[imageKey]?.let {
        showSlipItem(ViewMode.Thumb, layout = layout, image = it)
    } ?: run {
        sharedVm.viewModelScope.launch(Dispatchers.IO) {
            sharedVm.downloadOriginal(item, imageView)?.let {
                mapImage[imageKey] = it
//                        .apply {
//                    val stream = ByteArrayOutputStream()
//                    compress(Bitmap.CompressFormat.JPEG, 100, stream)
//                    val byteArray = stream.toByteArray()
//                    Common().decodeSampledBitmapFromResource(byteArray, imageView.width, imageView.height)
//                }

                fragment.binding.rcThumb.adapter?.notifyItemChanged(position)
            } ?: run {
                launch(Dispatchers.Main) {
                    sharedVm.showSnackbar("Failed to load image.")
                }
            }
        }
    }
}



@BindingAdapter("thumb")
fun thumb(layout: ConstraintLayout, item: Slip) {

    val imageView = layout.findViewById<TouchImageView>(R.id.image)
    val prog = layout.findViewById<ProgressBar>(R.id.prog_image)
    prog.visibility = View.VISIBLE

    Single.create<Bitmap> { emitter ->
        Common().fileToByte(item.thumbPath)?.let {
            Common().decodeSampledBitmapFromResource(it, imageView.width, imageView.height)?.let { bitmap ->
                emitter.onSuccess(bitmap)
            } ?: run {
                emitter.onError(Throwable())
            }
        } ?: run {
            emitter.onError(Throwable())
        }

    }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            showSlipItem(ViewMode.Thumb, layout = layout, image = it)
        }, { })
}

private fun showSlipItem(flag: ViewMode, layout: ConstraintLayout, image:Bitmap, delegateItem:Slip? = null)  {
    layout.findViewById<TouchImageView>(R.id.image)?.apply {
        setImageBitmap(image)
        setZoom(1f) //Important

        when(flag) {
            ViewMode.Thumb -> {
                setTouchEnable(false)
            }
            else -> {
                delegateItem?.run {
                    setDelegate(DelegateImageView(this@apply, delegateItem, null))
                }
            }
        }

        layout.findViewById<ProgressBar>(R.id.prog_image).apply {
            visibility = View.GONE
        }
    }
}

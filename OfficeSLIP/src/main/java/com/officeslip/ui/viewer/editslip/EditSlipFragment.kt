package com.officeslip.ui.viewer.editslip

import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.*
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.officeslip.*
import com.officeslip.base.BaseFragment
import com.officeslip.base.subclass.TouchImageView
import com.officeslip.databinding.FragmentEditSlipBinding
import com.officeslip.ui.viewer.SharedSlipViewerViewModel
import com.officeslip.ui.viewer.original.SlipViewerFragment
import com.officeslip.ui.viewer.SlipViewerViewModel
import com.officeslip.ui.main.OnBackPressedListener
import com.officeslip.util.Common
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

@AndroidEntryPoint
class EditSlipFragment : BaseFragment<FragmentEditSlipBinding, EditSlipViewModel>(), OnBackPressedListener{

    override val layoutResourceId: Int
        get() =  R.layout.fragment_edit_slip
    override val viewModel by viewModels<EditSlipViewModel>()

    private val m_C = Common()
    private lateinit var progress: AlertDialog



    override fun initStartView() {
        activity?.let {
            binding.fragment = this@EditSlipFragment
            binding.parentViewModel = viewModels<SlipViewerViewModel>({requireActivity()}).value
            binding.sharedViewModel = activityViewModels<SharedSlipViewerViewModel>().value
//            binding.sharedViewModel = getSharedViewModel()
            context?.run {

                val progressComment = if(SysInfo.DETECT_DOC) {
                    getString(R.string.detecting_document)
                }
                else {
                    getString(R.string.in_progress)
                }
                progress = m_C.getCircleProgress(this, progressComment) {
                    run{
                        closeEdit()
                    }
                }

                viewModel.curMode.postValue(EditMode.Crop)

                progress.show()
            }
        }
    }

    override fun initDataBinding() {
        binding.sharedViewModel?.apply {
            curIdx.observe(viewLifecycleOwner, Observer { idx ->
                listSlip?.value?.let { list ->

                    list[idx].let { slip ->
                        viewModel.curSlip.postValue(slip)

                        if(m_C.isBlank(slip.title)) {
                            title.postValue(getString(R.string.slip))
                        }
                        else {
                            title.postValue(slip.title)
                        }

                        initImageView(slip.path)
                    }
                }
            })
        }

        viewModel.curSlip.observe(viewLifecycleOwner, Observer {
//            if(SysInfo.DETECT_DOC) {
                viewModel.detectRect(it)
//            }
//            else {
//                viewModel.detectDoc.postValue(AgentResponse.error(getString(R.string.failed_detect_document)))
//            }
        })

        viewModel.detectDoc.observe(viewLifecycleOwner, Observer {
            drawCropView(it.data)
            progress.dismiss()
        })
    }

    override fun initAfterBinding() {

//        binding.parentViewModel.
    }

    override fun onBackPressed() {

//        var res = false
//        activity?.let {
//            m_C.simpleConfirm(it, null, getString(R.string.confirm_cancel_work), {
                closeEdit()
//            }, { })
//        }
    }

    fun closeEdit() {
        activity?.run{
//            setResult(Activity.RESULT_OK, intent)
//            finish()

            /**
             * back to viewer
             */
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_viewer, SlipViewerFragment(), ViewMode.View.name)
                .commit()


//            binding.parentViewModel?.apply {
//                viewModel.curSlip.postValue(viewModel.curSlip.value)
//            }
        }
    }

    fun confirmEdit() {
        when(viewModel.curMode.value) {

            EditMode.Crop -> {
                transformImage()
            }

        }
//        arguments?.let {bundle ->
//            viewModel.listSlip.postValue(bundle[SLIP_LIST] as List<Slip>)
//        } ?: run {
//            m_C.simpleAlert(activity, null, getString(R.string.failed_load_image)) {
//                activity?.finish()
//            }
//        }
    }

    private fun transformImage() {

        var imgProg:AlertDialog? = null
        activity?.let {
            imgProg = m_C.getCircleProgress(it) {
                run {
                    closeEdit()
                }
            }
            imgProg?.show()
        }

        viewModel.curSlip.value?.let {slip ->
            getBitmap(slip.path)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({bitmap ->
                    binding.image.apply {
                        try {
                            viewModel.crop.transformImage(bitmap, slip.path).let { bitmap ->

//                                Bitmap.createScaledBitmap(
//                                    bitmap,
//                                    bitmap.width,
//                                    bitmap.height,
//                                    true
//                                )

//                                setImageBitmap(bitmap)
//                                invalidate()

                                m_C.saveThumb(slip)
                                closeEdit()
                            }
                        }
                        catch (e:Exception) {
                            throw e
                        }
                        imgProg?.dismiss()
                    }
                },{
                    m_C.simpleAlert(activity, null, getString(R.string.failed_load_image)) {}
                    imgProg?.dismiss()
                })
        } ?: run {
            m_C.simpleAlert(activity, null, getString(R.string.failed_load_image)) {}
            imgProg?.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.sharedViewModel?.apply {
            listSlip.postValue(listSlip.value) //.notifyObserver()
            curIdx.postValue(curIdx.value)
        }
    }

    fun selectMode(mode:EditMode) {

        viewModel.curMode.postValue(mode)

        when(mode) {
            EditMode.Crop -> {
                selectCrop()
            }
            EditMode.Pinch -> {
//                viewModel.curSlip.value?.run {
                selectPinch()
//                }
            }
        }

    }

    private fun selectPinch() {
        binding.image.setDelegate(null)
        binding.image.invalidate()
//        initImageView()
//        viewModel.curSlip.value?.run {
//            initImageView(this.path)
//        }
    }

    private fun selectCrop() {
        viewModel.curSlip.value?.run {
            viewModel.detectRect(this)
        }
    }

    fun drawCropView(listPoints:List<PointF>?) {

        if(viewModel.curMode.value != EditMode.Crop) return

        val imageView = binding.image

        val leftUpperCirclePaint = Paint().apply {
            color = ContextCompat.getColor(imageView.context, R.color.docDetectCircle)
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 4f
        }

        val leftBottomCirclePaint = Paint().apply {
            color = ContextCompat.getColor(imageView.context, R.color.docDetectCircle)
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 4f
        }

        val rightUpperCirclePaint = Paint().apply {
            color = ContextCompat.getColor(imageView.context, R.color.docDetectCircle)
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 4f
        }

        val rightBottomCirclePaint = Paint().apply {
            color = ContextCompat.getColor(imageView.context, R.color.docDetectCircle)
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 4f
        }

        val bgPaint = Paint().apply {
            color = ContextCompat.getColor(imageView.context, R.color.backgroundOverlay)
        }

        val linePaint = Paint().apply {
            color = Color.RED
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        val circleBorderPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        fun setDefaultCircleColor() {
            leftUpperCirclePaint.color = ContextCompat.getColor(imageView.context, R.color.docDetectCircle)
            leftBottomCirclePaint.color = ContextCompat.getColor(imageView.context, R.color.docDetectCircle)
            rightUpperCirclePaint.color = ContextCompat.getColor(imageView.context, R.color.docDetectCircle)
            rightBottomCirclePaint.color = ContextCompat.getColor(imageView.context, R.color.docDetectCircle)
        }

        val crop = viewModel.crop
        crop.setPoints(listPoints, imageView)

        val linePath = Path()
        val bgPath = Path()

        imageView.setDelegate(object : TouchImageView.ImageViewDelegate{

            var leftUpper = PointF(0f,0f)
            var rightUpper = PointF(0f,0f)
            var leftBottom = PointF(0f,0f)
            var rightBottom = PointF(0f,0f)
            var verticalSpace = 0f
            var horizontalSpace = 0f

            override fun onDraw(canvas: Canvas?) {

                var zoomedWRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth
                var zoomedHRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight

                //Space betwwen imageview and device layout
                verticalSpace = (imageView.viewHeight - imageView.imageHeight) / 2f
                if(verticalSpace < 0f) verticalSpace = 0f
                horizontalSpace = (imageView.viewWidth - imageView.imageWidth) / 2f
                if(horizontalSpace < 0f) horizontalSpace = 0f

                var mMargin = crop.getImageMargin(imageView)
                val nLeftMargin = if(mMargin["Left"] == null) 0 else mMargin["Left"]!!
                val nTopMargin = if(mMargin["Top"] == null) 0 else mMargin["Top"]!!

                /**
                 *  Calculate Detected relative position
                 */
                listPoints?.let {
                    if (it.size != 4) {
                        crop.setDefaultSelection(imageView)
                    }
                } ?: run {
                    crop.setDefaultSelection(imageView)
                }

                leftUpper.apply {
                    x = (crop.leftUpper.x * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
                    y = (crop.leftUpper.y * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio)
                }
                rightUpper.apply {
                    x = (crop.rightUpper.x * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
                    y = (crop.rightUpper.y * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio)
                }
                rightBottom.apply {
                    x = (crop.rightBottom.x * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
                    y = (crop.rightBottom.y * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio)
                }
                leftBottom.apply {
                    x = (crop.leftBottom.x * zoomedWRatio) + horizontalSpace - (imageView.currentRect.left * zoomedWRatio)
                    y = (crop.leftBottom.y * zoomedHRatio) + verticalSpace - (imageView.currentRect.top * zoomedHRatio)
                }

                linePath.apply {
                    reset()
                    fillType = Path.FillType.EVEN_ODD
                    moveTo(leftUpper.x, leftUpper.y)
                    lineTo(rightUpper.x, rightUpper.y)
                    lineTo(rightBottom.x, rightBottom.y)
                    lineTo(leftBottom.x, leftBottom.y)
                    close()
                }

                //Draw black alphaed bg
                bgPath.apply {
                    reset()
                    fillType = Path.FillType.EVEN_ODD

                    var conf = imageView.resources.configuration

                    when(conf.orientation)
                    {
                        Configuration.ORIENTATION_PORTRAIT -> {
                            addRect(imageView.paddingLeft.toFloat(), nTopMargin.toFloat(), (imageView.width - imageView.paddingRight).toFloat(), (imageView.height - nTopMargin).toFloat(), Path.Direction.CW)
                        }
                        else -> {
                            addRect(nLeftMargin.toFloat(), imageView.paddingTop.toFloat(), (imageView.width - nLeftMargin).toFloat(), (imageView.height - nTopMargin).toFloat(), Path.Direction.CW)
                        }
                    }

                    addPath(linePath)
                }

                canvas?.apply {
                    drawPath(bgPath, bgPaint)
                    drawPath(linePath, linePaint)

                    drawCircle(leftUpper.x, leftUpper.y, (CROP_CIRCLE_WIDTH-3).px.toFloat(), leftUpperCirclePaint)
                    drawCircle(rightUpper.x, rightUpper.y, (CROP_CIRCLE_WIDTH-3).px.toFloat(), rightUpperCirclePaint)
                    drawCircle(rightBottom.x, rightBottom.y, (CROP_CIRCLE_WIDTH-3).px.toFloat(), rightBottomCirclePaint)
                    drawCircle(leftBottom.x, leftBottom.y, (CROP_CIRCLE_WIDTH-3).px.toFloat(), leftBottomCirclePaint)

                    drawCircle(leftUpper.x, leftUpper.y, (CROP_CIRCLE_WIDTH).px.toFloat(), circleBorderPaint)
                    drawCircle(rightUpper.x, rightUpper.y, (CROP_CIRCLE_WIDTH).px.toFloat(), circleBorderPaint)
                    drawCircle(rightBottom.x, rightBottom.y, (CROP_CIRCLE_WIDTH).px.toFloat(), circleBorderPaint)
                    drawCircle(leftBottom.x, leftBottom.y, (CROP_CIRCLE_WIDTH).px.toFloat(), circleBorderPaint)
                }
            }

            override fun onTouchEvent(e: MotionEvent?) {
                when(e?.actionMasked)
                {
                    MotionEvent.ACTION_MOVE -> {

//                        val isConvex = true

                        var wRatio = imageView.imageWidth / imageView.drawable.intrinsicWidth//.toFloat()
                        var hRatio = imageView.imageHeight / imageView.drawable.intrinsicHeight//.toFloat()

                        var zoomedWRatio = imageView.drawable.intrinsicWidth.toFloat() / imageView.imageWidth
                        var zoomedHRatio = imageView.drawable.intrinsicHeight.toFloat() / imageView.imageHeight

                        var eX = (e.x   - horizontalSpace + (imageView.currentRect.left * wRatio)) * zoomedWRatio
                        var eY = (e.y   - verticalSpace + (imageView.currentRect.top * hRatio)) * zoomedHRatio

                        crop.lastTouchedPoint?.set(eX, eY)
                    }

                    MotionEvent.ACTION_UP ->{
                        setDefaultCircleColor()
                        imageView.setTouchEnable(true)
                    }

                    MotionEvent.ACTION_DOWN -> {

                        var p = CROP_CIRCLE_WIDTH.px * 2
                        var pCur:PointF? = null

                        if(e.x < leftUpper.x + p && e.x > leftUpper.x - p
                            && e.y < leftUpper.y + p
                            && e.y > leftUpper.y - p)
                        {
                            imageView.setTouchEnable(false)

                            crop.lastTouchedPoint = crop.leftUpper
                            leftUpperCirclePaint.color = ContextCompat.getColor(imageView.context, R.color.colorAccent)
                        }
                        else if(e.x < rightUpper.x + p
                            && e.x > rightUpper.x - p
                            && e.y < rightUpper.y + p
                            && e.y > rightUpper.y - p) {
                            imageView.setTouchEnable(false)

//                            mUpperRightPoint!!.x = upperRightPosX
//                            mUpperRightPoint!!.y = upperRightPosY

                            crop.lastTouchedPoint = crop.rightUpper
                            rightUpperCirclePaint.color = ContextCompat.getColor(imageView.context, R.color.colorAccent)
                        }
                        else if (e.x < rightBottom.x + p
                            && e.x > rightBottom.x - p
                            && e.y < rightBottom.y + p
                            && e.y > rightBottom.y - p) {
                            imageView.setTouchEnable(false)

//                            mLowerRightPoint!!.x = lowerRightPosX
//                            mLowerRightPoint!!.y = lowerRightPosY

                            crop.lastTouchedPoint = crop.rightBottom
                            rightBottomCirclePaint.color = ContextCompat.getColor(imageView.context, R.color.colorAccent)
                        }
                        else if (e.x < leftBottom.x + p
                            && e.x > leftBottom.x - p
                            && e.y < leftBottom.y + p
                            && e.y > leftBottom.y - p) {
                            imageView.setTouchEnable(false)

//                            mLowerLeftPoint!!.x = lowerLeftPosX
//                            mLowerLeftPoint!!.y = lowerLeftPosY

                            crop.lastTouchedPoint = crop.leftBottom
                            leftBottomCirclePaint.color = ContextCompat.getColor(imageView.context, R.color.colorAccent)
                        }
                        else
                        {
                            crop.lastTouchedPoint = null
                        }
                    }
                }

                imageView.invalidate()
            }

            override fun onSizeChanged(w: Int, h: Int, oldw: Int, ildh: Int) {
                listPoints?.let {
                    if (it.size != 4) {
                        crop.setDefaultSelection(imageView)
                    }
                } ?: run {
                    crop.setDefaultSelection(imageView)
                }
            }

            override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
                crop.setPoints(listPoints, imageView)
            }
        })
    }

    private fun getBitmap(path:String): Single<Bitmap> {
        return Single.create<Bitmap> { emitter ->
            Common().run {
                fileToByte(path)?.let {
                    decodeSampledBitmapFromResource(it)?.let {bitmap ->
                        emitter.onSuccess(bitmap)
                    } ?: run {
                        emitter.onError(Throwable())
                    }
                } ?: run {
                    emitter.onError(Throwable())
                }
            }
        }
    }
    private fun initImageView(path:String) {

        getBitmap(path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.image.apply{
                    setImageBitmap(it)
                    setZoom(1f) //Important
                }
//                progress.hide()
            },{
                progress.dismiss()
                activity?.let {
                    m_C.simpleAlert(it, null, getString(R.string.failed_load_image)) {
                        it.supportFragmentManager
                            .beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.fragment_container_viewer, SlipViewerFragment(), ViewMode.View.name)
                            .commit()
                    }
                }

            })
    }
}


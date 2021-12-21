package com.officeslip.ui.viewer.thumb

import android.app.Activity.RESULT_OK
import android.graphics.Point
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.balysv.materialripple.MaterialRippleLayout
import com.officeslip.*
import com.officeslip.base.BaseFragment
import com.officeslip.base.BaseRecyclerView
import com.officeslip.base.subclass.TouchImageView
import com.officeslip.data.model.Slip
import com.officeslip.databinding.CardviewSearchSlipItemBinding
import com.officeslip.databinding.FragmentThumbBinding
import com.officeslip.ui.viewer.SharedSlipViewerViewModel
import com.officeslip.ui.viewer.SlipViewerActivity
import com.officeslip.util.Common
import com.officeslip.util.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject

@AndroidEntryPoint
class ThumbViewerFragment : BaseFragment<FragmentThumbBinding, ThumbViewerFragmentViewModel>(){

    override val layoutResourceId: Int
        get() =  R.layout.fragment_thumb
    override val viewModel by viewModels<ThumbViewerFragmentViewModel>()

    private val m_C = Common()
    private lateinit var progress: AlertDialog


    override fun initStartView() {
        activity?.let {
            binding.fragment = this@ThumbViewerFragment
            binding.sharedViewModel = activityViewModels<SharedSlipViewerViewModel>().value
            binding.parentActivity = it as SlipViewerActivity
        }
    }

    override fun initDataBinding() {

        binding.sharedViewModel?.apply {
//            val a = listSlip.hasObservers()
//            listSlip.observe(viewLifecycleOwner, Observer {
//
//                it.size
////                viewModel.addSlipItem(it.data as Slip)
//            })

//            curIdx.observe(viewLifecycleOwner, Observer {idx ->
//
////                listSlip.value?.run {
////                    val slip = this[idx]
////                    if(m_C.isBlank(slip.title)) {
////                       title.postValue(getString(R.string.slip))
////                    }
////                    else {
////                        title.postValue(slip.title)
////                    }
////                }
//            })
        }
    }

    override fun initAfterBinding() {
        initThumbView()
    }

    private fun initThumbView() {

        with(binding) {
            rcThumb.setHasFixedSize(true)
            rcThumb.setItemViewCacheSize(20)
            rcThumb.layoutManager = GridLayoutManager(this@ThumbViewerFragment.context,3)

//            rcThumb.layoutManager = AutoFitGridLayoutManager(this@ThumbViewerFragment.context,100.px)
            rcThumb.adapter = adapter.apply {

                items = ArrayList()
                Single.just(clickSubject.subscribe { position ->
                    selectThumbItem(position)
                })

                sharedViewModel?.listSlip?.value?.forEach {
                    addItem(it)
                }
            }
            rcThumb.addItemDecoration(GridSpacingItemDecoration(3, 10, true))

        }

    }

    fun confirm() {
        activity?.run {
            setResult(RESULT_OK, intent)
            finish()
        }
    }


    fun backToOriginal() {
        activity?.run {
            supportFragmentManager
                .popBackStack()
        }
    }

    private fun selectThumbItem(pos:Int) {
        backToOriginal()
        binding.sharedViewModel?.moveIdx?.postValue(pos)
    }

    fun zoomIn() {
        with(binding) {
            var spanCount = (rcThumb.layoutManager as GridLayoutManager).spanCount

            if(spanCount > 1) {
                spanCount--
                rcThumb.layoutManager = GridLayoutManager(this@ThumbViewerFragment.context,spanCount)
//                rcThumb.addItemDecoration(GridSpacingItemDecoration(spanCount, 10, true))
            }
        }
    }

    fun zoomOut() {
        with(binding) {
            var spanCount = (rcThumb.layoutManager as GridLayoutManager).spanCount

            if(spanCount < THUMB_VIEWER_MAX_ZOOM) {
                spanCount++
                rcThumb.layoutManager = GridLayoutManager(this@ThumbViewerFragment.context,spanCount)
//                rcThumb.itemdeco
//                rcThumb.addItemDecoration(GridSpacingItemDecoration(spanCount, 10, true))
            }
        }
    }

    private val adapter =  object : BaseRecyclerView.Adapter<Slip, CardviewSearchSlipItemBinding>(
            layoutResId = R.layout.cardview_search_slip_item,
            bindingVariableId = BR.slip
    ) {
        val clickSubject = PublishSubject.create<Int>()

        //Bind event listener programmatically
        override fun onBindViewHolder(
                holder: BaseRecyclerView.ViewHolder<CardviewSearchSlipItemBinding>,
                position: Int
        ) {
            holder.binding.image.setImageBitmap(null)
            holder.binding.sharedVm = binding.sharedViewModel
            holder.binding.position = position
            holder.binding.fragment = binding.fragment

            val spanCount = (binding.rcThumb.layoutManager as GridLayoutManager).spanCount
            holder.itemView.findViewById<LinearLayout>(R.id.layout_thumb).layoutParams.height = setItemHeight(spanCount)

            holder.itemView.findViewById<MaterialRippleLayout>(R.id.cardview).apply {
                setOnClickListener {
                    clickSubject.onNext(position)
                }
            }
            holder.itemView.findViewById<TouchImageView>(R.id.image).apply{
                setTouchEnable(false)
            }

            super.onBindViewHolder(holder, position)
        }

        private fun setItemHeight(spanCount:Int):Int {
            val display = binding.parentActivity?.windowManager?.defaultDisplay
            val size = Point()
            display?.getSize(size)

            var nLayoutMargin = 4.px
            var nCardViewMargin = 10.px


            var fPadding = 10
//            var nItemRow = THUMB_VIEWER_MAX_ZOOM - (THUMB_VIEWER_MAX_ZOOM - (spanCount - 1))
            var paddingSpace = (fPadding * spanCount) + 5
            var nAvrWidth = size.x - paddingSpace
            var nItemWidth = ((nAvrWidth / spanCount) - (nLayoutMargin + nCardViewMargin)).toInt()
            var nItemHeight = (nItemWidth * 170) / 100

            return nItemHeight
        }
    }
}

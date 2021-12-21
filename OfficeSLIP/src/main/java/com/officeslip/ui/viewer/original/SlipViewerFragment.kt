package com.officeslip.ui.viewer.original

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.officeslip.*
import com.officeslip.base.BaseFragment
import com.officeslip.base.BaseRecyclerView
import com.officeslip.data.model.Slip
import com.officeslip.databinding.FragmentSlipViewerBinding
import com.officeslip.databinding.ViewpagerSlipItemBinding
import com.officeslip.ui.history.HistoryActivity
import com.officeslip.ui.property.PropertyActivity
import com.officeslip.ui.viewer.SharedSlipViewerViewModel
import com.officeslip.ui.viewer.SlipViewerActivity
import com.officeslip.ui.search.SlipViewerOptionDialog
import com.officeslip.ui.viewer.SlipViewerViewModel
import com.officeslip.ui.viewer.editslip.EditSlipFragment
import com.officeslip.ui.viewer.thumb.ThumbViewerFragment
import com.officeslip.util.Common
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class SlipViewerFragment : BaseFragment<FragmentSlipViewerBinding, SlipViewerFragmentViewModel>(), SlipViewerOptionDialog.OnClickButton {

    override val layoutResourceId: Int
        get() =  R.layout.fragment_slip_viewer
    override val viewModel by viewModels<SlipViewerFragmentViewModel>()

    private val m_C = Common()
    private lateinit var progress: AlertDialog


    override fun initStartView() {
        activity?.let {
            binding.fragment = this@SlipViewerFragment
            binding.parentViewModel = viewModels<SlipViewerViewModel>({requireActivity()}).value
            binding.sharedViewModel = activityViewModels<SharedSlipViewerViewModel>().value
            binding.parentActivity = it as SlipViewerActivity
        }


        if(binding.pager.adapter == null) {
            initViewPager()
        }
    }

    override fun initDataBinding() {

        binding.sharedViewModel?.apply {
//            val a = listSlip.hasObservers()
            listSlip.observe(viewLifecycleOwner, Observer {
                viewPagerAdapter.run {
                    replaceAll(it)
                    notifyDataSetChanged()
                }
            })

            moveIdx.observe(viewLifecycleOwner, Observer { idx ->

               binding.pager.apply {
                    setCurrentItem(idx,false)
                }

                listSlip.value?.run {
                    val slip = this[idx]
                    if(m_C.isBlank(slip.title)) {
                        title.postValue(getString(R.string.slip))
                    }
                    else {
                        title.postValue(slip.title)
                    }
                }
            })
            curIdx.observe(viewLifecycleOwner, Observer {idx ->

                listSlip.value?.run {
                    val slip = this[idx]
                    if(m_C.isBlank(slip.title)) {
                       title.postValue(getString(R.string.slip))
                    }
                    else {
                        title.postValue(slip.title)
                    }
                }

                binding.pager.apply {
                    setCurrentItem(idx, false)
                }
            })
        }
    }

    override fun initAfterBinding() {}

    fun moveToThumbView() {
        activity?.run {
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.fragment_container_viewer, ThumbViewerFragment().apply {
                }, ViewMode.Thumb.name)

                .addToBackStack(this@SlipViewerFragment.javaClass.name)
            .commit()
        }
    }

    fun moveToEdit() {
        activity?.run {
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container_viewer, EditSlipFragment().apply {
//                                arguments = bundle
                }, ViewMode.Edit.name)
                .commit()

            binding.sharedViewModel?.run {
                curIdx.postValue(curIdx.value)

            }
        }
    }

    fun confirm() {
//        viewPagerAdapter.getCurViewHolder(pager.currentItem)?.let {holder->
//            holder.binding.image.setImageDrawable(getDrawable(R.drawable.arrow))
//        }
        activity?.run {
//            intent.apply {
//                putExtra(SLIP_INDEX, CUR_INDEX)
//            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    val openViewerOption = View.OnClickListener {

        activity?.run {
            SlipViewerOptionDialog().apply {
                setOnClickButtonListener(this@SlipViewerFragment)
                show(supportFragmentManager, this.tag)
            }
        }
    }

    private fun openHistoryViewer() {
        activity?.intent?.extras?.let { bundle ->
            openHistoryViewerRegistry.launch(Intent(activity, HistoryActivity::class.java).apply {
                binding.sharedViewModel?.curIdx?.value?.let {
                    putExtra(SLIP_ITEM, (bundle[SLIP_ITEM] as ArrayList<Slip>)[it])
                }
            })
        }
    }
    private val openHistoryViewerRegistry = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult -> }


    private fun openPropertyViewer() {
        activity?.intent?.extras?.let { bundle ->
            openPropertyViewerRegistry.launch(Intent(activity, PropertyActivity::class.java).apply {
                binding.sharedViewModel?.curIdx?.value?.let {
                    putExtra(BUNDLE_SDOC_NO, (bundle[SLIP_ITEM] as ArrayList<Slip>)[it].sdocNo)
                }
            })
        }
    }
    private val openPropertyViewerRegistry = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult -> }


    override fun onDialogBtnClick(strTag: String) {

        when(strTag.toUpperCase(Locale.getDefault()))
        {
            "SLIP_VIEWER_HISTORY" -> {
                openHistoryViewer()
            }
            "SLIP_VIEWER_PROPERTY" -> {
                openPropertyViewer()
            }
        }
    }

    private fun initViewPager() {
        binding.pager.apply {
                adapter = viewPagerAdapter.apply {
                    items = ArrayList<Slip>()
                    reduceDragSensitivity()

                    offscreenPageLimit = 3

                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)

                            binding.sharedViewModel?.run {
                                curIdx.postValue(position)
                            }
                        }
                    })
                }
            }
    }

    private val viewPagerAdapter = object : BaseRecyclerView.Adapter<Slip, ViewpagerSlipItemBinding>(
        layoutResId = R.layout.viewpager_slip_item,
        bindingVariableId = BR.slip
    ) {
        val mFragment = HashMap<Int, BaseRecyclerView.ViewHolder<ViewpagerSlipItemBinding>>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerView.ViewHolder<ViewpagerSlipItemBinding> {
            return super.onCreateViewHolder(parent, viewType)
        }
        override fun onBindViewHolder(
            holder: BaseRecyclerView.ViewHolder<ViewpagerSlipItemBinding>,
            position: Int
        ) {

            holder.binding.sharedVm = binding.sharedViewModel
            super.onBindViewHolder(holder, position)
//            binding.setVariable(BR.sharedVm, items[position] as SharedSlipViewerViewModel)
            mFragment[position] = holder
        }

        fun getCurViewHolder(position: Int): BaseRecyclerView.ViewHolder<ViewpagerSlipItemBinding>? {
            return mFragment[position]
        }

        override fun onViewDetachedFromWindow(holder: BaseRecyclerView.ViewHolder<ViewpagerSlipItemBinding>) {
            mFragment.remove(holder.adapterPosition)
            super.onViewDetachedFromWindow(holder)
        }
    }

    fun removeCurSlip() {
        activity?.run {
            m_C.simpleConfirm(this, "", getString(R.string.confirm_remove_slip), {
                binding.sharedViewModel?.run {

                    listSlip.value?.apply {
                        var idx = binding.pager.currentItem
                        removeAt(idx)

                        if(this.size <= 0) {
                            binding.parentActivity?.confirm()
                        }
                        else {
                            idx -= 1
                            viewPagerAdapter.run {
                                replaceAll(this@apply)
                                notifyDataSetChanged()
                            }
                            binding.pager.currentItem = idx
                            curIdx.postValue(idx)

                        }
                    }
                }
            }, {})
        }
    }

    /**
     * Reduces drag sensitivity of [ViewPager2] widget
     */
    fun ViewPager2.reduceDragSensitivity() {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop*4)       // "4" was obtained experimentally
    }
}

package com.officeslip.ui.search.slipkind

import android.app.Activity
import android.app.AlertDialog
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.officeslip.R
import com.officeslip.util.Common
import com.officeslip.BR
import com.officeslip.Status
import com.officeslip.base.BaseActivity
import com.officeslip.base.BaseRecyclerView
import com.officeslip.ViewFlag
import com.officeslip.data.model.SlipKindItem
import com.officeslip.databinding.ActivitySlipkindBinding
import com.officeslip.databinding.SlipkindListItemBinding
import com.officeslip.util.log.Logger
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.subjects.PublishSubject

@AndroidEntryPoint
class SlipKindActivity : BaseActivity<ActivitySlipkindBinding, SlipKindViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_slipkind
    override val viewModel by viewModels<SlipKindViewModel>()

    private val m_C = Common()
    private lateinit var progress: AlertDialog
    private var viewFlag: ViewFlag = ViewFlag.Add
//    private val sharedViewModel: SharedViewModel by inject()

    companion object {
        const val CUR_KIND_NM = "CUR_KIND_NM"
        const val CUR_KIND_NO = "CUR_KIND_NO"
        const val OPEN_FROM = "OPEN_FROM"
        const val SELECTED_KIND_NM = "SELECTED_KIND_NM"
        const val SELECTED_KIND_NO = "SELECTED_KIND_NO"
        const val SELECTED_KIND_BARCODE = "SELECTED_KIND_BARCODE"

    }

    override fun initStartView() {
        binding.activity = this
//        viewDataBinding.sharedViewModel = sharedViewModel

        intent.extras?.let { bundle ->
            (bundle[OPEN_FROM] as? ViewFlag)?.apply {
                viewFlag = this
            }
        }

        binding.viewRcSlipKind.adapter = adapter.apply {
            items = ArrayList<SlipKindItem>()
            viewModel.addDisposable(
                clickSubject.subscribe { position ->
                    adapter.updateItemAt(position).let { prevIdx ->
                            notifyItemChanged(position)
                            prevIdx?.apply {
                                notifyItemChanged(this)
                            }
                        }
                }
            )
        }
    }


    override fun initDataBinding() {

        viewModel.slipKind.observe(this, Observer {

            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                        this@SlipKindActivity
                        , null
                        , it.message
                    ) { }
                }
                Status.LOADING -> {
                    progress.show()
                }
            }
        }).apply {
            progress = m_C.getCircleProgress(this@SlipKindActivity) {
                viewModel.stopAgentExecution()
            }
        }

        viewModel.filteredSlipKind.observe(this, Observer {

            it.data?.let {
                (binding.viewRcSlipKind.adapter as? BaseRecyclerView.Adapter<Any, *>)?.run {
                    replaceAll(it as List<Any>)
                    notifyDataSetChanged()
                }
            }

        })
    }

    override fun initAfterBinding() {

        viewModel.getSlipKindList("1","", intent.getStringExtra(CUR_KIND_NO), viewFlag)

    }

    fun closeActivity() {
        this@SlipKindActivity.run {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }

    fun confirm() {
        var selectedItem: SlipKindItem? = null
        viewModel.slipKind.value?.data?.forEach {
            if(it.checked) {
                selectedItem = it
                return@forEach
            }
        }

        selectedItem?.let {
            intent.putExtra(SELECTED_KIND_NM,it.name)
            intent.putExtra(SELECTED_KIND_NO,it.code)
            intent.putExtra(SELECTED_KIND_BARCODE,it.barcode)

            Logger.debug("SlipKindActivity - Selected item : ${it.name}(${it.code})")
            setResult(RESULT_OK, intent)
            finish()
        } ?: run {
            m_C.simpleAlert(
                this@SlipKindActivity
                , null
                , getString(R.string.alert_select_slipkind)
            ) { }
        }
    }

    private val adapter = object : BaseRecyclerView.Adapter<SlipKindItem, SlipkindListItemBinding>(
        layoutResId = R.layout.slipkind_list_item,
        bindingVariableId = BR.kindItem
    ) {

        val clickSubject = PublishSubject.create<Int>()

        //Bind event listener programmatically
        override fun onBindViewHolder(
            holder: BaseRecyclerView.ViewHolder<SlipkindListItemBinding>,
            position: Int
        ) {

            holder.itemView.findViewById<ConstraintLayout>(R.id.layout_item).setOnClickListener {
                clickSubject.onNext(position)
            }
            super.onBindViewHolder(holder, position)
        }

        fun updateItemAt(position:Int):Int? {
            var prevIdx:Int? = null
            items.forEachIndexed { i, item ->
                if(item.checked) {
                    prevIdx = i
                    item.checked = false
                    return@forEachIndexed
                }
            }
            viewModel.resetCheckState()
            items[position].checked = true
            return prevIdx
        }
    }
}




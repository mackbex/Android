package com.officeslip.ui.addslip.extension

import android.app.Activity
import android.app.AlertDialog
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.officeslip.*
import com.officeslip.util.Common
import com.officeslip.base.BaseActivity
import com.officeslip.base.BaseRecyclerView
import com.officeslip.data.model.SearchListViewItem
import com.officeslip.data.model.SlipKindItem
import com.officeslip.data.model.User
import com.officeslip.databinding.*
import com.officeslip.util.log.Logger
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject

@AndroidEntryPoint
class ExtensionActivity : BaseActivity<ActivityExtensionBinding, ExtensionViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_extension
    override val viewModel by viewModels<ExtensionViewModel>()

    private val m_C = Common()
    private lateinit var progress: AlertDialog
    var extFlag: ExtensionFlag = ExtensionFlag.Copy
    lateinit var sdocNo:String
//    private val sharedViewModel: SharedViewModel by inject()

    companion object {
        const val ACTION_FLAG = "ACTION_FLAG"

    }

    override fun initStartView() {
        binding.activity = this
//        viewDataBinding.sharedViewModel = sharedViewModel

        intent.extras?.let { bundle ->
            (bundle[ACTION_FLAG] as ExtensionFlag)?.apply {
                extFlag = this
            }
            (bundle[BUNDLE_SDOC_NO] as String)?.apply {
                sdocNo = this
            }
        }

        binding.viewRcUserInfo.adapter = adapter.apply {
            items = ArrayList()
            viewModel.addDisposable(
                clickSubject.subscribe { position ->
                    adapter.updateItemAt(position).let { prevIdx ->
                        notifyItemChanged(position)
                        prevIdx?.apply {
                            notifyItemChanged(this)
                        }
                    }
            })
        }
    }


    override fun initDataBinding() {

        viewModel.moveSlip.observe(this, {
            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()
                    closeActivity()
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                            this@ExtensionActivity
                            , null
                            , it.message
                    ) { }
                }
                Status.LOADING -> {
                    progress.show()
                }
            }
        }).apply {
            progress = m_C.getCircleProgress(this@ExtensionActivity) {
                viewModel.stopAgentExecution()
            }
        }

        viewModel.copySlip.observe(this, {
            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()
                    closeActivity(Activity.RESULT_OK)
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                            this@ExtensionActivity
                            , null
                            , it.message
                    ) { }
                }
                Status.LOADING -> {
                    progress.show()
                }
            }
        }).apply {
            progress = m_C.getCircleProgress(this@ExtensionActivity) {
                viewModel.stopAgentExecution()
            }
        }

        viewModel.userList.observe(this, Observer {

            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                        this@ExtensionActivity
                        , null
                        , it.message
                    ) { }
                }
                Status.LOADING -> {
                    progress.show()
                }
            }
        }).apply {
            progress = m_C.getCircleProgress(this@ExtensionActivity) {
                viewModel.stopAgentExecution()
            }
        }
//
//        viewModel.filteredSlipKind.observe(this, Observer {
//
//            it.data?.let {
//                (binding.viewRcSlipKind.adapter as? BaseRecyclerView.Adapter<Any, *>)?.run {
//                    replaceAll(it as List<Any>)
//                    notifyDataSetChanged()
//                }
//            }
//
//        })
    }

    override fun initAfterBinding() {
    }



    fun searchUser(v: TextView, actionId:Int, event: KeyEvent?):Boolean {

        val imeAction = when (actionId) {
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_GO -> true
            else -> false
        }

        val keydownEvent = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

        if (imeAction or keydownEvent) {
            if(!m_C.isBlank(v.editableText.toString())) {
                viewModel.getUserList(v.editableText.toString())
            } else {
                m_C.simpleAlert(
                        this@ExtensionActivity
                        , null
                        , getString(R.string.input_user_info)
                ) { }
            }
            return true
        }
        return false
    }

    fun closeActivity(resultFlag:Int = Activity.RESULT_CANCELED) {
        this@ExtensionActivity.run {
            setResult(resultFlag, intent)
            finish()
        }
    }

    fun confirm() {
        var selectedUser: User? = null
        viewModel.userList.value?.data?.forEach {
            if(it.checked) {
                selectedUser = it
                return@forEach
            }
        }

        selectedUser?.let { user ->

            val msg = when(extFlag) {
                ExtensionFlag.Move -> getString(R.string.confirm_move_slip)
                ExtensionFlag.Copy -> getString(R.string.confirm_copy_slip)
            }

            m_C.simpleConfirm(this@ExtensionActivity, null, msg, {
                when(extFlag) {
                    ExtensionFlag.Move -> viewModel.moveSlip(sdocNo, user)
                    ExtensionFlag.Copy -> viewModel.copySlip(sdocNo, user)
                }
            }, {})

        } ?: run {
            m_C.simpleAlert(
                this@ExtensionActivity
                , null
                , getString(R.string.alert_select_user)
            ) { }
        }
    }

    private val adapter = object : BaseRecyclerView.Adapter<User, SelectExtUserListItemBinding>(
        layoutResId = R.layout.select_user_list_item,
        bindingVariableId = BR.userInfo
    ) {

        val clickSubject = PublishSubject.create<Int>()

        //Bind event listener programmatically
        override fun onBindViewHolder(
            holder: BaseRecyclerView.ViewHolder<SelectExtUserListItemBinding>,
            position: Int
        ) {

            holder.itemView.findViewById<ConstraintLayout>(R.id.layout_item).setOnClickListener {
                clickSubject.onNext(position)
            }
            super.onBindViewHolder(holder, position)
        }

//        fun selectUser(idx: Int) {
//            val item = items[idx]
//        }

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




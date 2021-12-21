package com.officeslip.ui.search.user

import android.app.Activity
import android.app.AlertDialog
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
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
import com.officeslip.data.model.User
import com.officeslip.databinding.ActivitySelectUserBinding
import com.officeslip.databinding.SelectUserListItemBinding
import com.officeslip.util.log.Logger
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.subjects.PublishSubject

@AndroidEntryPoint
class SelectUserActivity : BaseActivity<ActivitySelectUserBinding, SelectUserViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_select_user
    override val viewModel by viewModels<SelectUserViewModel>()

    private val m_C = Common()
    private lateinit var progress: AlertDialog
    private var viewFlag: ViewFlag = ViewFlag.Add
//    private val sharedViewModel: SharedViewModel by inject()

    companion object {
        const val CUR_USER_NM = "CUR_USER_NM"
        const val CUR_USER_ID = "CUR_USER_NO"
        const val OPEN_FROM = "OPEN_FROM"
        const val SELECTED_USER_NM = "SELECTED_USER_NM"
        const val SELECTED_USER_ID = "SELECTED_USER_ID"

    }

    override fun initStartView() {
        binding.activity = this
//        viewDataBinding.sharedViewModel = sharedViewModel

        intent.extras?.let { bundle ->
            (bundle[OPEN_FROM] as? ViewFlag)?.apply {
                viewFlag = this
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
                }
            )
        }
    }


    override fun initDataBinding() {

        viewModel.userInfo.observe(this, Observer {

            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                        this@SelectUserActivity
                        , null
                        , it.message
                    ) { }
                }
                Status.LOADING -> {
                    progress.show()
                }
            }
        }).apply {
            progress = m_C.getCircleProgress(this@SelectUserActivity) {
                viewModel.stopAgentExecution()
            }
        }

//        viewModel.filteredUserInfo.observe(this, Observer {
//
//            it.data?.let {
//                (binding.viewRcUserInfo.adapter as? BaseRecyclerView.Adapter<Any, *>)?.run {
//                    replaceAll(it as List<Any>)
//                    notifyDataSetChanged()
//                }
//            }
//
//        })
    }

    override fun initAfterBinding() { }

    fun searchUser(v: TextView, actionId:Int, event:KeyEvent?):Boolean {

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
                viewModel.getUserList( v.editableText.toString(), viewFlag)
            } else {
                m_C.simpleAlert(
                        this@SelectUserActivity
                        , null
                        , getString(R.string.input_user_info)
                ) { }
            }
            return true
        }
        return false
    }

    fun closeActivity() {
        this@SelectUserActivity.run {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }

    fun confirm() {
        var selectedItem: User? = null
        viewModel.userInfo.value?.data?.forEach {
            if(it.checked) {
                selectedItem = it
                return@forEach
            }
        }

        selectedItem?.let {
            intent.putExtra(SELECTED_USER_NM,it.userNm)
            intent.putExtra(SELECTED_USER_ID,it.userId)

            Logger.debug("SelectUserActivity - Selected item : ${it.userNm}(${it.userId})")
            setResult(RESULT_OK, intent)
            finish()
        } ?: run {
            m_C.simpleAlert(
                this@SelectUserActivity
                , null
                , getString(R.string.alert_select_user)
            ) { }
        }
    }

    private val adapter = object : BaseRecyclerView.Adapter<User, SelectUserListItemBinding>(
        layoutResId = R.layout.select_user_list_item,
        bindingVariableId = BR.userInfo
    ) {

        val clickSubject = PublishSubject.create<Int>()

        //Bind event listener programmatically
        override fun onBindViewHolder(
            holder: BaseRecyclerView.ViewHolder<SelectUserListItemBinding>,
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




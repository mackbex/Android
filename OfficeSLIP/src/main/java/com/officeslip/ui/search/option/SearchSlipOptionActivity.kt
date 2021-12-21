package com.officeslip.ui.search.option

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.widget.TextView
import androidx.activity.viewModels
import com.officeslip.*
import com.officeslip.base.BaseActivity
import com.officeslip.data.model.SearchSlipOption
import com.officeslip.SysInfo
import com.officeslip.databinding.ActivitySearchSlipOptionBinding
import com.officeslip.ui.search.SearchSlipFragment
import com.officeslip.ui.search.slipkind.SlipKindActivity
import com.officeslip.util.Common
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchSlipOptionActivity : BaseActivity<ActivitySearchSlipOptionBinding, SearchSlipOptionViewModel>() {
    private val m_C = Common()
    override val layoutResourceId: Int
        get() = R.layout.activity_search_slip_option
    override val viewModel by viewModels<SearchSlipOptionViewModel>()

    override fun initStartView() {
        binding.activity = this
//        binding.viewModel = viewModel

        intent.extras?.let {
           viewModel.searchSlipOption.value = it.get(SearchSlipFragment.CUR_SEARCH_OPTION) as SearchSlipOption
        } ?: run {
            viewModel.searchSlipOption.value = SearchSlipOption(
                SysInfo.userInfo[userId].asString,
                SysInfo.userInfo[userNm].asString,
                SysInfo.userInfo[partNo].asString,
                SysInfo.userInfo[partNm].asString,
                SysInfo.userInfo[corpNo].asString,
                SysInfo.userInfo[corpNm].asString,
            )
        }
    }

    override fun initAfterBinding() {

    }

    override fun initDataBinding() {

    }

    fun close() {
        finish()
    }

    fun apply() {
        intent.apply {
            putExtra(SearchSlipFragment.CUR_SEARCH_OPTION, viewModel.searchSlipOption.value)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    fun openSlipKindActivity() {
        Intent(this, SlipKindActivity::class.java).run {
            putExtra(SlipKindActivity.CUR_KIND_NO, viewModel.searchSlipOption.value?.slipKindNo)
            putExtra(SlipKindActivity.OPEN_FROM, ViewFlag.Search)
            startActivityForResult(this, RESULT_SELECT_SLIPKIND)
        }
    }

    fun openCalendar(view: TextView) {

        var dayInfo = view.text.split("-")

        DatePickerDialog(
            this,
            AlertDialog.THEME_HOLO_LIGHT,
            DatePickerDialog.OnDateSetListener{ _ , year, month, dayOfMonth ->
                viewModel.searchSlipOption.value?.let {
                    val timeVal = "$year-${String.format("%02d",month+1)}-${String.format("%02d",dayOfMonth)}"
                    when(view.tag) {
                        "FROM_DATE" -> {
                            it.fromDate = timeVal
                        }
                        "TO_DATE" -> {
                            it.toDate = timeVal
                        }
                    }
                    viewModel.searchSlipOption.postValue(it)
                }
            },
            dayInfo[0].toInt(),
            dayInfo[1].toInt() - 1,
            dayInfo[2].toInt()
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RESULT_SELECT_SLIPKIND -> {
                if (resultCode == Activity.RESULT_OK) {
                    val option = viewModel.searchSlipOption.value
                    option?.let { option ->
                        data?.extras?.let { data ->
                            option.slipKindNm =
                                data.get(SlipKindActivity.SELECTED_KIND_NM).toString()
                            option.slipKindNo =
                                data.get(SlipKindActivity.SELECTED_KIND_NO).toString()
                        }
                    }
                    viewModel.searchSlipOption.postValue(option)
                }
            }
        }
    }
//
//    fun changeStep() {
//        var step = ""
//        if(seg_used.isChecked) {
//            if(m_C.isBlank(step)) {step = "2,3,4,5,6"}
//            else {step += ",2,3,4,5,6"}
//        }
//
//        if(seg_unused.isChecked) {
//            if(m_C.isBlank(step)) {step = "0,1"}
//            else {step += ",0,1"}
//        }
//
//        if(seg_removed.isChecked) {
//            if(m_C.isBlank(step)) {step = "9"}
//            else {step += ",9"}
//        }
//
//        viewModel.searchSlipOption.value?.let {
//            it.step = step
//            viewModel.searchSlipOption.postValue(it)
//        }
//    }

}
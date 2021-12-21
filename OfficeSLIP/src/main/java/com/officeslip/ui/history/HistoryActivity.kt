package com.officeslip.ui.history

import android.app.Activity
import android.app.AlertDialog
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.officeslip.*
import com.officeslip.util.Common
import com.officeslip.base.BaseActivity
import com.officeslip.data.model.*
import com.officeslip.databinding.ActivityHistoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryActivity : BaseActivity<ActivityHistoryBinding, HistoryViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_history
    override val viewModel by viewModels<HistoryViewModel>()

    private val m_C = Common()
    lateinit var slip: Slip
    private lateinit var progress: AlertDialog

    companion object {

    }

    override fun initStartView() {
        binding.activity = this

        intent.extras?.let { bundle ->
            (bundle[SLIP_ITEM] as? Slip)?.apply {
                slip = this
            }
        }
    }


    override fun initDataBinding() {

        viewModel.historyList.observe(this, Observer {

            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                            this@HistoryActivity
                            , null
                            , it.message
                    ) { }
                }
                Status.LOADING -> {
                    progress.show()
                }
            }
        }).apply {
            progress = m_C.getCircleProgress(this@HistoryActivity) {
                viewModel.stopAgentExecution()
            }
        }

    }

    override fun initAfterBinding() {
        viewModel.getProperty(slip.sdocNo)
    }

    fun closeActivity() {
        this@HistoryActivity.run {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }


}




package com.officeslip.ui.property

import android.app.Activity
import android.app.AlertDialog
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.officeslip.*
import com.officeslip.util.Common
import com.officeslip.base.BaseActivity
import com.officeslip.data.model.*
import com.officeslip.databinding.ActivityPropertyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PropertyActivity : BaseActivity<ActivityPropertyBinding, PropertyViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_property
    override val viewModel by viewModels<PropertyViewModel>()

    private val m_C = Common()
    lateinit var sdocNo: String
    lateinit var folder: String
    private lateinit var progress: AlertDialog

    companion object {

    }

    override fun initStartView() {
        binding.activity = this

        intent.extras?.let { bundle ->
            (bundle[BUNDLE_SDOC_NO] as? String)?.apply {
                sdocNo = this
            }
            (bundle[BUNDLE_FOLDER] as? String)?.let {
                folder = it
            } ?: run { folder = "SLIPDOC" }
        }
    }


    override fun initDataBinding() {

        viewModel.slipInfo.observe(this, Observer {

            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                            this@PropertyActivity
                            , null
                            , it.message
                    ) { }
                }
                Status.LOADING -> {
                    progress.show()
                }
            }
        }).apply {
            progress = m_C.getCircleProgress(this@PropertyActivity) {
                viewModel.stopAgentExecution()
            }
        }

    }

    override fun initAfterBinding() {
        viewModel.getProperty(sdocNo, folder)
    }

    fun closeActivity() {
        this@PropertyActivity.run {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }


}




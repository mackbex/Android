package com.officeslip.ui.setting

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.officeslip.PageType
import com.officeslip.R
import com.officeslip.SysInfo
import com.officeslip.base.BaseFragment
import com.officeslip.databinding.FragmentSettingBinding
import com.officeslip.ui.login.LoginActivity
import com.officeslip.ui.main.MainViewModel
import com.officeslip.ui.main.SharedMainViewModel
import com.officeslip.ui.main.OnBackPressedListener
import com.officeslip.util.Common
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding, SettingViewModel>() {

    private val m_C = Common()
    private lateinit var progress: AlertDialog

    override val layoutResourceId: Int
        get() =  R.layout.fragment_setting
    override val viewModel by viewModels<SettingViewModel>()

    override fun initStartView() {

        activity?.let {
            binding.mainViewModel = viewModels<MainViewModel>({ requireActivity() }).value
            binding.sharedViewModel = activityViewModels<SharedMainViewModel>().value
            binding.fragment = this@SettingFragment
        }
    }

    override fun initDataBinding() {

//        viewModel.detectDoc.observe(viewLifecycleOwner, Observer {
//            if(binding.viewSwitchDetectDoc.isPressed) {
//                viewModel.setDetectDoc(it)
//            }
//        })

        viewModel.sendBugReport.observe(viewLifecycleOwner, {

            progress.dismiss()
            var resMsg = if(it.isSuccess) {
               "${getString(R.string.success_uplaod_bug_report)}${it.msg}"
            }
            else {
               getString(R.string.failed_uplaod_bug_report)
            }
           m_C.simpleAlert(activity, null,resMsg){}

        })
    }

    override fun initAfterBinding() {
        context?.run {
            val versionInfo = packageManager.getPackageInfo(packageName, 0)
            viewModel.version.postValue(versionInfo.versionName)
        }
        viewModel.detectDoc.postValue(SysInfo.DETECT_DOC)
//        viewModel.uploadSeminarOriginal.postValue(SysInfo.UPLOAD_SEMINAR_ORIGINAL)
        viewModel.developer.postValue(getString(R.string.developer_val))
    }


    fun confirmLogout() {
        activity?.run {
            m_C.simpleConfirm(this, null, getString(R.string.logout_confirm), {
                binding.mainViewModel?.onLogout()
            }, { })
        }
    }

    fun sendBugReport() {
        activity?.run {
            m_C.simpleConfirm(this, null, getString(R.string.confirm_send_bug_report), {
                progress = AlertDialog.Builder(activity).apply {
                    LayoutInflater.from(activity).inflate(R.layout.progress_line, null).apply {

                        findViewById<TextView>(R.id.tv_progTitle).text = getString(R.string.in_progress)
                        setView(this)
                        setCancelable(false)
                        setNegativeButton(getString(R.string.btn_cancel), DialogInterface.OnClickListener { dialog, _ ->
                            viewModel.stopAgentExecution()
                            dialog.dismiss()
                        })
                    }
                }.create()
                progress.show()
//                progress.findViewById<ProgressBar>(R.id.view_progress).max = 1
                viewModel.sendBugReport()
            }, {

            })
        }
    }
}
package com.officeslip.ui.login

import android.app.AlertDialog
import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.officeslip.FRAG_ADD_SLIP
import com.officeslip.base.BaseActivity
import com.officeslip.R
import com.officeslip.Status
import com.officeslip.SysInfo
import com.officeslip.databinding.ActivityLoginBinding
import com.officeslip.ui.main.MainActivity
import com.officeslip.util.Common
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {
    override val layoutResourceId: Int
        get() =  R.layout.activity_login
        override val viewModel by viewModels<LoginViewModel>()

    private val m_C = Common()
    private lateinit var progress:AlertDialog

    override fun initStartView() {
        binding.activity = this
    }

    override fun initDataBinding() {

        viewModel.autoLogin.observe(this, Observer {
            if(binding.viewSwitchAutoLogin.isPressed) {
                viewModel.setAutoLogin(it)
            }
        })


        viewModel.login.observe(this, Observer {
            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()

                    moveToAddSlip()
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(this@LoginActivity
                        ,null
                        ,it.message
                    ) { }
                }
                Status.LOADING -> {
                    progress.show()
                }
            }

        }).apply {
            progress = m_C.getCircleProgress(this@LoginActivity) {
                viewModel.stopAgentExecution()
            }
        }

        viewModel.resetPW.observe(this, {
            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()
                    m_C.simpleAlert(this@LoginActivity
                            ,null
                            ,getString(R.string.success_resetPW)
                    ) { }
                }
                Status.LOADING -> {
                    progress.show()
                }
                else -> {
                    progress.dismiss()
                    m_C.simpleAlert(this@LoginActivity
                        ,null
                        ,it.message
                    ) { }
                }
            }
        }).apply {
            progress = m_C.getCircleProgress(this@LoginActivity) {
                viewModel.stopAgentExecution()
            }
        }


    }

    override fun initAfterBinding() {
        viewModel.initLoginInfo()
    }

    fun moveToAddSlip() {
        Intent(this@LoginActivity, MainActivity::class.java).run {
            putExtra("ADD_FLAG", FRAG_ADD_SLIP)
            startActivity(this)
            this@LoginActivity.finish()
        }
    }


    fun confirmResetPassword() {
        m_C.simpleConfirm(this@LoginActivity, null, getString(R.string.confirm_resetPW), {
            viewModel.resetPassword()
        }, { })
    }

    override fun onBackPressed() {
        m_C.simpleConfirm(
                this@LoginActivity,
                null,
                getString(R.string.confirm_exit),
                {
                    finish()
                },
                { })
    }

}
package com.officeslip.ui.viewer

import android.app.AlertDialog
import androidx.activity.viewModels
import com.officeslip.*
import com.officeslip.base.BaseActivity
import com.officeslip.data.model.Slip
import com.officeslip.databinding.ActivitySlipviewerBinding
import com.officeslip.ui.viewer.editslip.EditSlipFragment
import com.officeslip.ui.viewer.original.SlipViewerFragment
import com.officeslip.ui.main.OnBackPressedListener
import com.officeslip.util.Common
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SlipViewerActivity : BaseActivity<ActivitySlipviewerBinding, SlipViewerViewModel>() {
    private val m_C = Common()
    override val layoutResourceId: Int
    get() = R.layout.activity_slipviewer
    override val viewModel by viewModels<SlipViewerViewModel>()
//    private val sharedViewModel: SharedSlipViewerViewModel by inject()

    lateinit var progress: AlertDialog

//    private lateinit var listSlipItem:List<Slip>
//    private var CUR_INDEX:Int = 0
//    private var VIEW_FLAG = FRAG_ADD_SLIP
//    private lateinit var CUR_ITEM:Slip



    override fun initStartView() {
        progress = m_C.getCircleProgress(applicationContext){}


        binding.viewModel = viewModel
        binding.activity = this
        binding.sharedViewModel =  viewModels<SharedSlipViewerViewModel>().value

        intent.extras?.let { bundle ->
            (bundle[VIEWER_FLAG] as? ViewFlag)?.apply {
                when(this) {
                    ViewFlag.Search, ViewFlag.Add -> {
//                    viewModel.title.postValue(getString(R.string.slip))
//                viewModel.viewMode.postValue(ViewMode.Edit)

                        supportFragmentManager
                            .beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.fragment_container_viewer, SlipViewerFragment().apply {
//                                arguments = bundle
                            }, ViewMode.View.name)
                            .commit()
                    }
                    ViewFlag.Edit -> {

//                viewModel.viewMode.postValue(ViewMode.View)

                        supportFragmentManager
                            .beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.fragment_container_viewer, EditSlipFragment().apply {
//                                arguments = bundle
                            }, ViewMode.Edit.name)
                            .commit()

                    }
                }

                binding.sharedViewModel?.viewFlag?.postValue(this)
            }
        }
    }

    override fun initDataBinding() { }

    override fun initAfterBinding() {

        intent.extras?.let {bundle ->

            (bundle[SLIP_ITEM] as? MutableList<Slip>)?.let { list ->

                with(binding) {
                    sharedViewModel?.listSlip?.postValue(list)

                when (bundle[VIEWER_FLAG]) {
                        ViewFlag.Search -> {
                            (bundle[SLIP_INDEX] as? Int)?.let {
                                sharedViewModel?.moveIdx?.postValue(it)
                            } ?: run {
                                sharedViewModel?.curIdx?.postValue(0)
                            }
                        }
                        else -> {
                            (bundle[SLIP_INDEX] as? Int)?.let {
                                sharedViewModel?.moveIdx?.postValue(it)
                            } ?: run {
                                sharedViewModel?.curIdx?.postValue(list.size - 1)
                            }
                        }
                    }
                }
            } ?: run {
                m_C.simpleAlert(this@SlipViewerActivity, null, getString(R.string.failed_load_image)) {
                    finish()
                }
            }
        } ?: run {
            m_C.simpleAlert(this@SlipViewerActivity, null, getString(R.string.failed_load_image)) {
                finish()
            }
        }

    }

    fun closeActivity() {
        this@SlipViewerActivity.run {
            finish()
        }
    }

    fun confirm() {
//        viewPagerAdapter.getCurViewHolder(pager.currentItem)?.let {holder->
//            holder.binding.image.setImageDrawable(getDrawable(R.drawable.arrow))
//        }
        this@SlipViewerActivity.run {
            intent.apply {
                putExtra(SLIP_ITEM, binding.sharedViewModel?.listSlip?.value as ArrayList<Slip>)
            }
            setResult(RESULT_OK, intent)

            finish()
        }
    }



    override fun onBackPressed() {
//
//        val frag = getCurrentFragTag()?.let {
//            getFragmentByTag(it)
//        } ?: run {
//            getFragmentByTag(FRAG_SCHEME_MAIN_MENU)
//        }
//
//        (frag as? OnBackPressedListener)?.onBackPressed() ?: run {
//            m_C.simpleConfirm(
//                this@SchemeMainActivity,
//                null,
//                getString(R.string.confirm_exit),
//                {
//                    this@SchemeMainActivity.finish()
//                },
//                { })
//        }

        supportFragmentManager.findFragmentByTag(ViewMode.Edit.name)?.let {
//
            (it as? OnBackPressedListener)?.onBackPressed() ?: run {
                super.onBackPressed()
            }
//            val frag = it as? EditSlipFragment
//            if(frag != null && frag.isVisible) {
//                supportFragmentManager
//                    .beginTransaction()
//                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
//                    .replace(R.id.fragment_container_viewer, SlipViewerFragment(), ViewMode.View.name)
//                    .commit()
//            }
//            else {
//                super.onBackPressed()
//            }
        } ?: run {
            super.onBackPressed()
        }
    }
}

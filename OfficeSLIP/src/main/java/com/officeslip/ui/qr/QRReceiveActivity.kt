package com.officeslip.ui.qr

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.widget.LinearLayout
import androidx.activity.viewModels
import com.balysv.materialripple.MaterialRippleLayout
import com.officeslip.*
import com.officeslip.util.Common
import com.officeslip.base.BaseActivity
import com.officeslip.base.BaseRecyclerView
import com.officeslip.base.subclass.TouchImageView
import com.officeslip.data.model.*
import com.officeslip.databinding.ActivityQrReceiveBinding
import com.officeslip.databinding.CardviewQrSlipItemBinding
import com.officeslip.ui.qr.QRScanFragment.Companion.BARCODE_CODE
import com.officeslip.ui.viewer.SlipViewerActivity
import com.officeslip.util.log.Logger
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject

@AndroidEntryPoint
class QRReceiveActivity : BaseActivity<ActivityQrReceiveBinding, QRReceiveViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_qr_receive
    override val viewModel by viewModels<QRReceiveViewModel>()

    private val m_C = Common()
    lateinit var barcode : String
    private lateinit var progress: AlertDialog
    var auth : Int = 0


    override fun initStartView() {
        binding.activity = this

        intent.extras?.let { bundle ->
            (bundle[BARCODE_CODE] as? String)?.apply {
                barcode = this
            }
        }

        auth = SysInfo.userInfo["AUTH"].asInt
    }


    override fun initDataBinding() {
        viewModel.getQRInfo.observe(this@QRReceiveActivity, {
            when(it.status) {
                Status.LOADING -> {
                    progress.show()
                }
                Status.SUCCESS -> {

                    it.data?.let { item ->
                        viewModel.qrInfo.postValue(item)
                        viewModel.getSlipItem(item.sdocNo)
                    } ?: run {
//                        initThumbView()

                    }
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                            this@QRReceiveActivity
                            , null
                            , it.message
                    ) { }

                }
            }
        }).apply {
            progress = m_C.getCircleProgress(this@QRReceiveActivity) {
                viewModel.stopAgentExecution()
            }
        }

        viewModel.getSlipItem.observe(this@QRReceiveActivity, {
            when(it.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    progress.dismiss()
                    it.data?.let { item ->
                        item.forEach { slip ->
                            addSlipToThumbView(slip)
                        }

                    } ?: run {
                    }
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                            this@QRReceiveActivity
                            , null
                            , it.message
                    ) { }
                }
            }
        })

        viewModel.changeQRRecvStat.observe(this@QRReceiveActivity, {
            when(it.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    progress.dismiss()
                    it.data?.let { res ->
                        if(res) {
                            val prev = viewModel.qrInfo.value?.apply {
                                recvStatus = if(recvStatus.equals("Y", true)) { "N" } else { "Y" }
                            }

                            viewModel.qrInfo.postValue(prev)
                        }
                    }
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                            this@QRReceiveActivity
                            , null
                            , it.message
                    ) { }
                }
            }
        })
    }

    fun changeRecvStatus(barcode:String, status:String) {
//        m_C.simpleConfirm(this, null, getString(R.string.confirm_change_qr_recv_stat), {
            viewModel.changeRecvStatus(barcode, status)
//        }, {})
    }

    override fun initAfterBinding() {
        initThumbView()
        viewModel.getQRInfo(barcode)
    }

    private fun initThumbView() {
        binding.layoutMainCell.rcThumb.adapter = adapter.apply {
            items = ArrayList()
            Single.just(clickSubject.subscribe { position ->
                    openSlipViewer(ViewFlag.Search, position)
            })
        }
    }

    private fun addSlipToThumbView(item:Slip) {
        adapter.apply {
            addItem(item)
            notifyItemInserted(itemCount - 1)
        }
    }

    private fun openSlipViewer(flag: ViewFlag, idx:Int? = null) {
        Intent(this@QRReceiveActivity, SlipViewerActivity::class.java).run {
            val slipList:List<Slip>? = viewModel.getSlipItem.value?.data

            slipList?.let {

                val thumbItem:ArrayList<Int> = ArrayList()
                it.forEachIndexed { index, slip ->
                    if(slip.docNo == BYTE_TRANSFER_FILE_DOCNO_START_IDX) {
                        thumbItem.add(index)
                    }
                }

                putExtra(SLIP_INDEX, thumbItem[idx ?: run {0}])
                putExtra(VIEWER_FLAG, flag)
                putExtra(SLIP_ITEM, it as ArrayList<Slip>)

                startActivity(this)
            } ?: run {
                viewModel.showSnackbar(R.string.no_slip_to_show)
            }

        }
    }

    fun closeActivity() {
        this@QRReceiveActivity.run {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }



    private val adapter =  object : BaseRecyclerView.Adapter<Slip, CardviewQrSlipItemBinding>(
            layoutResId = R.layout.cardview_qr_slip_item,
            bindingVariableId = BR.slip
    ) {
        val clickSubject = PublishSubject.create<Int>()
        var viewMode = ThumbMode.View

        //Bind event listener programmatically
        override fun onBindViewHolder(
                holder: BaseRecyclerView.ViewHolder<CardviewQrSlipItemBinding>,
                position: Int
        ) {
            holder.binding.image.setImageBitmap(null)
            holder.binding.viewModel = binding.viewModel
            holder.binding.activity = binding.activity
            holder.binding.position =  position//(binding.layoutMaincell.rcThumb.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

            Logger.debug("$position")

            holder.itemView.findViewById<LinearLayout>(R.id.layout_thumb).apply {

                if(viewMode == ThumbMode.Edit && items[position].selected) {
                    setBackgroundResource(R.drawable.thumbview_selected)
                }
                else {
                    setBackgroundResource(0)
                }
            }

            holder.itemView.findViewById<MaterialRippleLayout>(R.id.cardview)?.apply {
                setOnClickListener {
                    clickSubject.onNext(position)
                }
            }

            holder.itemView.findViewById<TouchImageView>(R.id.image).apply{
                setTouchEnable(false)
            }

            super.onBindViewHolder(holder, position)
        }
    }

}




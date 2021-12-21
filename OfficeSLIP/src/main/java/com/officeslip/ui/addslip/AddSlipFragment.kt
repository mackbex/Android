package com.officeslip.ui.addslip

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.balysv.materialripple.MaterialRippleLayout
import com.officeslip.*
import com.officeslip.base.BaseFragment
import com.officeslip.base.BaseRecyclerView
import com.officeslip.data.model.CollectionFlag
import com.officeslip.data.model.CollectionItem
import com.officeslip.data.model.Slip
import com.officeslip.databinding.CardviewSlipItemBinding
import com.officeslip.databinding.FragmentAddslipBinding
import com.officeslip.ui.viewer.SlipViewerActivity
import com.officeslip.ui.main.MainViewModel
import com.officeslip.ui.main.SharedMainViewModel
import com.officeslip.ui.search.slipkind.SlipKindActivity
import com.officeslip.ui.search.slipkind.SlipKindActivity.Companion.CUR_KIND_NO
import com.officeslip.ui.search.slipkind.SlipKindActivity.Companion.SELECTED_KIND_BARCODE
import com.officeslip.ui.search.slipkind.SlipKindActivity.Companion.SELECTED_KIND_NM
import com.officeslip.ui.search.slipkind.SlipKindActivity.Companion.SELECTED_KIND_NO
import com.officeslip.ui.search.user.SelectUserActivity
import com.officeslip.ui.search.user.SelectUserActivity.Companion.CUR_USER_ID
import com.officeslip.ui.search.user.SelectUserActivity.Companion.SELECTED_USER_ID
import com.officeslip.ui.search.user.SelectUserActivity.Companion.SELECTED_USER_NM
import com.officeslip.util.Common
import com.officeslip.util.log.Logger
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.File
import java.io.InputStream
import java.util.*


@AndroidEntryPoint
class AddSlipFragment : BaseFragment<FragmentAddslipBinding, AddSlipViewModel>(), AddSlipDialog.OnClickButton {


    override val layoutResourceId: Int
        get() =  R.layout.fragment_addslip
    override val viewModel by viewModels<AddSlipViewModel>()

    private val m_C = Common()
    private lateinit var progress: AlertDialog
//    private lateinit var mainViewModel:ViewModel
//    private lateinit var mainBinding: ViewDataBinding

    override fun initStartView() {
        activity?.let {

            binding.mainViewModel = viewModels<MainViewModel>({ requireActivity() }).value
            binding.fragment = this@AddSlipFragment
            binding.sharedViewModel = activityViewModels<SharedMainViewModel>().value

        }
    }

    fun initThumbView() {
        binding.layoutMainCell.rcThumb.adapter = adapter.apply {
           Single.just(clickSubject.subscribe { position ->
               when (viewMode) {
                   ThumbMode.View -> {
                       openSlipViewer(ViewFlag.Add, position)
                   }
                   ThumbMode.Edit -> {
                       selectThumbItem(position)
                   }
               }
           })
        }
    }

    private fun verifyData(vararg values: String, msgOnError: String):Boolean {

        var res = true
        for(value in values) {
            if(m_C.isBlank(value)) {
                viewModel.showSnackbar(msgOnError)
//                m_C.simpleAlert(activity, null, msgOnError ) {}
                res = false
            }
            break
        }
        return res
    }

//    override fun onBackPressed() {
//
////        var res = false
//        activity?.let {
//            m_C.simpleConfirm(it, null, getString(R.string.confirm_cancel_work), {
//
//                reset()
//                binding.mainViewModel?.pagerFlag?.postValue(PageType.MAIN)
//
//            }, { })
//        }
//    }

    private fun checkDocumentInfo():Boolean {
        viewModel.documentInfo.value?.let {
           if(!verifyData(it.regCorpNm, it.regCorpNo, msgOnError = getString(R.string.input_corp))) {
               binding.layoutCorp.isPressed = true
               return false
           }
           if(!verifyData(it.regPartNm, it.regPartNo, msgOnError = getString(R.string.input_part))) {
               binding.layoutPart.isPressed = true
               return false
           }
           if(!verifyData(it.regUserNm, it.regUserId, msgOnError = getString(R.string.input_user))) {
               binding.layoutUser.isPressed = true
               return false
           }
           if(!verifyData(it.slipKindNm, it.slipKindNo, msgOnError = getString(R.string.input_kind))) {
               binding.layoutKind.isPressed = true
               return false
           }
           if(!verifyData(it.sdocName, msgOnError = getString(R.string.input_sdoc_name))) {
               binding.layoutSdocName.isPressed = true
               return false
           }

            if(viewModel.getSlipList().size <= 0) {
                m_C.simpleAlert(activity, null, getString(R.string.err_add_slip)) {}
                return false
            }
        } ?: run {
            m_C.simpleAlert(activity, null, getString(R.string.err_input_data)) {}
            return false
        }
        return true

    }

    fun submitSlip() {
        if(checkDocumentInfo()) {
            activity?.run {
                m_C.simpleConfirm(this, null, getString(R.string.confirm_add_slip), {
                    progress = AlertDialog.Builder(activity).apply {
                        LayoutInflater.from(activity).inflate(R.layout.progress_line, null).apply {

                            findViewById<TextView>(R.id.tv_progTitle).text =
                                getString(R.string.in_progress)
                            setView(this)
                            setCancelable(false)
                            setNegativeButton(
                                getString(R.string.btn_cancel),
                                DialogInterface.OnClickListener { dialog, _ ->
                                    viewModel.stopAgentExecution()
                                    dialog.dismiss()
                                })
                        }
                    }.create()
                    progress.show()
                    progress.findViewById<ProgressBar>(R.id.view_progress).max = 3
                    viewModel.submit()
                }, {

                })
            }
        }
    }

    private fun refreshThumbView() {
        binding.viewModel?.run {
            adapter.items = getSlipList()

//            adapter.replaceAll(getSlipList())
            adapter.notifyDataSetChanged()
        }
    }
    private fun addSlipToThumbView(item: Slip) {
        adapter.apply {
            addItem(item)
            notifyItemInserted(itemCount - 1)
        }
    }

    private fun selectThumbItem(position: Int) {
        adapter.items[position].apply {
            selected = !selected
        }

        if(adapter.items.filter { it.selected }.count() > 0) {
            binding.layoutMainCell.viewRippleBtnRemove.visibility = View.VISIBLE
        }
        else {
            binding.layoutMainCell.viewRippleBtnRemove.visibility = View.GONE
        }

        adapter.notifyItemChanged(position)
    }

    override fun initDataBinding() {
        binding.viewModel?.slipCollectionItem?.observe(viewLifecycleOwner, Observer {
            when (it.flag) {
                CollectionFlag.ADD -> {
                    viewModel.addSlipItem(it.data as Slip)
                    refreshThumbView()
                    progress.dismiss()
                    openSlipViewer(ViewFlag.Edit) //Open edit view right after get image bitmap.

//                    addSlipToThumbView(it.data as Slip)
                }
//                CollectionFlag.REMOVE -> {
//                    viewModel.removeSlipItem(it.data)
//                }
                CollectionFlag.REPLACE -> {
                    viewModel.setSlipItem(it.data as List<Slip>)
                    refreshThumbView()
                }
            }
        })

        viewModel.slipItem.observe(viewLifecycleOwner, Observer {
            it?.status?.let { status ->
                when (status) {
                    Status.LOADING -> {

                    }
                    Status.SUCCESS -> {
                        val (slipItem) = guardLet(it.data) { return@let }

                        viewModel.slipCollectionItem.postValue(CollectionItem.add(slipItem))
                    }
                    Status.ERROR -> {
                        m_C.simpleAlert(
                            activity, null, it.message
                        ) { progress.dismiss() }
                    }
                }
            }
        })

        viewModel.checkBarcode.observe(viewLifecycleOwner, Observer {
            viewModel.documentInfo.value?.run {
                if(!m_C.isBlank(slipKindNo)) {
                    if(barcodeYn) {
                        if(!it) {
                            viewModel.showSnackbar(getString(R.string.barcode_forced, slipKindNm))
                            viewModel.checkBarcode.postValue(true)
                        }
                    }
                }
            }
        })



        viewModel.uploadProcess.observe(viewLifecycleOwner, Observer {
            progress.apply {
                findViewById<TextView>(R.id.tv_progTitle).text = it.msg
                findViewById<ProgressBar>(R.id.view_progress).progress = it.progress
            }
        })

        viewModel.uploadResult.observe(viewLifecycleOwner, Observer {

            progress.dismiss()

            if (it.isSuccess) {
                m_C.simpleAlert(activity, null, getString(R.string.upload_success_slip)) {
                    reset()
                }
            } else {
                m_C.simpleAlert(activity, null, it.msg) { }
            }
        })

        viewModel.imageProcess.observe(viewLifecycleOwner, Observer {

            it?.status?.let { status ->
                when (status) {
                    Status.LOADING -> {
//                        it.data?.let { info ->
//                            when(info.flag) {
//                                UploadMethod.Camera -> {
//                                    getImageFromCamera(info.uri)
//                                }
//                                UploadMethod.Gallery -> {
//                                    getImageFromGallery()
//                                }
//                            }
//                        }
                    }
                    Status.SUCCESS -> {
                        it.data?.let { info ->
                            when (info.flag) {
                                UploadMethod.Camera -> {
                                    getImageFromCamera(info.uri)
                                }
                                UploadMethod.Gallery -> {
                                    getImageFromGallery()
                                }
                            }
                        }
                    }
                    Status.ERROR -> {
                        m_C.simpleAlert(
                            activity, null, it.message
                        ) { progress.dismiss() }
                    }
                }
            }
        }).apply {
            context?.let {
                progress = m_C.getCircleProgress(it) {
//                viewModel.model.stopCurrentExecution()
                }
            }
        }

        binding.sharedViewModel?.currentPageType?.observe(viewLifecycleOwner, {
//            if (it == PageType.MAIN) {
//                reset()
//            }
//            binding.sharedViewModel?.currentPage?.postValue(null)
        })
    }

    override fun initAfterBinding() {
        setActionBar()
        initThumbView()
        reset()
//        viewModel.showSnackbar("증빙 명을 입력하세요.")
    }

    private fun getImageFromCamera(uri: Uri?) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).run {
            //                                data =
            //                                type = "camera/*"

            putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

            //                    CALL_ANOTHER_APPLICATION = true /*In security reason, set true for another app called from own activity*/
            putExtra(MediaStore.EXTRA_OUTPUT, uri)

//                                Handler().postDelayed(Runnable {
            startActivityForResult(this, RESULT_CAMERA)
//                                }, 300)
        }
    }

    private fun getImageFromGallery() {
        Intent(Intent.ACTION_PICK).run{
            data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            type = "image/*"

//            CALL_ANOTHER_APPLICATION = true /*In security reason, set true for another app called from own activity*/
            startActivityForResult(this, RESULT_GALLERY)
        }
    }

    private fun openSlipViewer(flag: ViewFlag, idx: Int? = null) {
        Intent(activity, SlipViewerActivity::class.java).run {
            idx?.let {
                putExtra(SLIP_INDEX, it)
            }
            putExtra(VIEWER_FLAG, flag)
            putExtra(SLIP_ITEM, viewModel.getSlipList())
            startActivityForResult(this, RESULT_CREATE_SLIP_IMAGE)
        }
        progress.dismiss()
    }

    private fun reset() {
        activity?.run {
            SysInfo.IS_SCHEME_CALL = m_C.detectScheme(this)
            viewModel.resetDocumentInfo()

            m_C.removeFolder(this, UPLOAD_PATH)
            viewModel.clearSlipItem()

            adapter.apply {
                items = viewModel.getSlipList()
                viewModel.setSlipItem(items)
                notifyDataSetChanged()
            }

            toggleSelectSlip(ThumbMode.View)
            viewModel.checkBarcode.postValue(false)

            //Reset progress
            progress = m_C.getCircleProgress(this) {
//                viewModel.model.stopCurrentExecution()
            }
        }
    }

    private fun setActionBar() {
        (activity as? AppCompatActivity)?.apply {

            val tool = view?.findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(tool)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }


    fun openSlipKindActivity() {
        Intent(activity, SlipKindActivity::class.java).run {
            putExtra(CUR_KIND_NO, binding.viewModel?.documentInfo?.value?.slipKindNo)
            putExtra(SlipKindActivity.OPEN_FROM, ViewFlag.Add)
            startActivityForResult(this, RESULT_SELECT_SLIPKIND)
        }
    }


    fun openSelectUserActivity() {
        selectUserRegistry.launch(Intent(activity, SelectUserActivity::class.java).apply {
            putExtra(CUR_USER_ID, binding.viewModel?.documentInfo?.value?.managerId)
            putExtra(SelectUserActivity.OPEN_FROM, ViewFlag.Add)
        })
    }
    private val selectUserRegistry = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {

            val doc = viewModel.documentInfo.value
            doc?.let { doc ->
                result.data?.extras?.let { data ->
                    doc.managerNm = data.get(SELECTED_USER_NM).toString()
                    doc.managerId = data.get(SELECTED_USER_ID).toString()
                }
            }
            viewModel.documentInfo.postValue(doc)
        }
    }

    private fun toggleSelectSlip(setMode: ThumbMode? = null) {
        activity?.run {
            adapter.apply {
                setMode?.let {
                    if (setMode == ThumbMode.Edit) {
                        binding.layoutMainCell.viewTextSlipSelect.text = getString(R.string.cancel)
                        binding.layoutMainCell.viewRippleBtnRemove.visibility = View.GONE
                        viewMode = ThumbMode.Edit
                    } else {
                        binding.layoutMainCell.viewTextSlipSelect.text = getString(R.string.select)
                        binding.layoutMainCell.viewRippleBtnRemove.visibility = View.GONE

                        items.forEach {
                            it.selected = false
                        }
                        viewMode = ThumbMode.View
                    }
                } ?: run {
                    if (viewMode == ThumbMode.View) {
                        binding.layoutMainCell.viewTextSlipSelect.text = getString(R.string.cancel)
                        binding.layoutMainCell.viewRippleBtnRemove.visibility = View.GONE
                        viewMode = ThumbMode.Edit
                    } else {
                        binding.layoutMainCell.viewTextSlipSelect.text = getString(R.string.select)
                        binding.layoutMainCell.viewRippleBtnRemove.visibility = View.GONE

                        items.forEach {
                            it.selected = false
                        }
                        viewMode = ThumbMode.View
                    }
                }
                notifyDataSetChanged()
            }
        }
    }

    val addSlip = View.OnClickListener {

        toggleSelectSlip(setMode = ThumbMode.View)

        activity?.run {
            AddSlipDialog().apply {
                setOnClickButtonListener(this@AddSlipFragment)
                show(supportFragmentManager, this.tag)
            }
        }
    }
    val addCamera = View.OnClickListener {

        toggleSelectSlip(setMode = ThumbMode.View)

        progress = m_C.getCircleProgress(requireContext()) {
//                viewModel.model.stopCurrentExecution()
        }
        progress.show()
        viewModel.prepareCamera()
    }
    val addGallery = View.OnClickListener {

        toggleSelectSlip(setMode = ThumbMode.View)

        progress = m_C.getCircleProgress(requireContext()) {
//                viewModel.model.stopCurrentExecution()
        }
        progress.show()
        viewModel.prepareGallery()
    }

    val selectSlip = View.OnClickListener {
        activity?.run {
            toggleSelectSlip()
        }
    }

    val removeSlip = View.OnClickListener {
        activity?.run {
            m_C.simpleConfirm(this, null, getString(R.string.confirm_remove_slip), {
                if (adapter.viewMode == ThumbMode.Edit) {
                    adapter.apply {
                        var removeCnt = 0

                        val checkedItems = items.filter { it.selected }

                        checkedItems.forEach {
                            m_C.removeFile(this@run, it.path)
                        }

                        items.removeAll(checkedItems)
                        notifyDataSetChanged()

//                    items.forEachIndexed { index, slip ->
//                        if (slip.selected) {
//                            removeCnt ++
//                            items.remove(slip)
//                            m_C.removeFile(this@run, slip.path)
//                            notifyItemRemoved(index)
//                        }
//                    }

                        if (checkedItems.isNotEmpty()) {
                            viewModel.setSlipItem(items)
                            toggleSelectSlip(ThumbMode.View)
                        } else {
                            m_C.simpleAlert(
                                this@run,
                                null,
                                getString(R.string.select_slip_to_remove)
                            ) {}
                        }
                    }
                }

            }, {})
        }
    }

    override fun onDialogBtnClick(strTag: String) {

        when(strTag.toUpperCase(Locale.getDefault()))
        {
            "ADD_SLIP_CAMERA" -> {
                progress.show()
                viewModel.prepareCamera()
            }
            "ADD_SLIP_GALLERY" -> {
                progress.show()
                viewModel.prepareGallery()

            }
//            "CLOSE_ADDSLIP_DIALOG" -> {
//                Log.d("CLOSE_ADDSLIP_DIALOG","CLOSE_ADDSLIP_DIALOG")
//            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            when (requestCode) {
                RESULT_SELECT_SLIPKIND -> {
                    if (resultCode == Activity.RESULT_OK) {
                        val doc = viewModel.documentInfo.value
                        doc?.let { doc ->
                            data?.extras?.let { data ->
                                doc.slipKindNm = data.get(SELECTED_KIND_NM).toString()
                                doc.slipKindNo = data.get(SELECTED_KIND_NO).toString()
                                doc.barcodeYn  = data.get(SELECTED_KIND_BARCODE) as Boolean

                                viewModel.checkBarcode.postValue(doc.barcodeYn)
                            }
                        }
                        viewModel.documentInfo.postValue(doc)
                    }
                }
                RESULT_CAMERA -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewModel.imageProcess.let {
                            it.value?.data?.let { info ->
                                info.localPath?.run {
                                    File(this).run {
                                        if (length() > 0) {
                                            viewModel.convertToSlipImage(this)
                                        } else {
                                            progress.dismiss()
                                        }
                                    }
                                } ?: run {
                                    m_C.simpleAlert(
                                            activity,
                                            null,
                                            getString(R.string.error_failed_load_image)
                                    ) { }
                                }
                            } ?: run {
                                m_C.simpleAlert(
                                        activity,
                                        null,
                                        getString(R.string.error_failed_load_image)
                                ) { }
                            }
                        }
                    }, 500)

                }
                RESULT_GALLERY -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (resultCode == Activity.RESULT_OK) {

                            m_C.getRealPathFromUri(data?.data, activity as Context).let { path ->
                                //                        list.forEach {
                                File(path).run {

                                    var stream: InputStream? = null
                                    var uriData: Uri = data?.data!!
//                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                        // Do something for lollipop and above versions
//                                        uriData = MediaStore.setRequireOriginal(data?.data!!)
//                                    }

                                    stream = requireContext().contentResolver.openInputStream(uriData)

                                    if (length() > 0) {
                                        val copiedName = "$UPLOAD_PATH/${this.name}"
                                        if (m_C.copyFile(this.absolutePath, "$copiedName")) {
                                            viewModel.convertToSlipImage(File(copiedName), stream)
                                        } else {
                                            m_C.simpleAlert(
                                                    activity,
                                                    null,
                                                    getString(R.string.error_failed_load_image)
                                            ) { }
                                        }
                                    } else {
                                        progress.dismiss()
                                    }
                                }
                                //                        }
                            }
                        } else {
                            progress.dismiss()
                        }
                    }, 500)
                }

                RESULT_CREATE_SLIP_IMAGE -> {
                    if (resultCode == Activity.RESULT_OK) {
                        data?.extras?.get(SLIP_ITEM)?.let {


                            viewModel.slipCollectionItem.postValue(CollectionItem.replace(it as List<Slip>))

//                        viewModel.slipList.postValue(it as CollectionItem<Slip>)
                        }
//                        val curIdx = it as Int
//                        rc_thumb.adapter?.notifyItemChanged(curIdx)
//                    }
                    }
//                binding.layoutMainCell.rcThumb.adapter?.notifyDataSetChanged()
                }
            }
        }
        catch(e:Exception) {
            Logger.error("Failed get image data", e);
        }
    }

    private val adapter =  object : BaseRecyclerView.Adapter<Slip, CardviewSlipItemBinding>(
        layoutResId = R.layout.cardview_slip_item,
        bindingVariableId = BR.slip
    ) {
        val clickSubject = PublishSubject.create<Int>()
        var viewMode = ThumbMode.View

        //Bind event listener programmatically
        override fun onBindViewHolder(
            holder: BaseRecyclerView.ViewHolder<CardviewSlipItemBinding>,
            position: Int
        ) {

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

                setOnLongClickListener {
                    if(viewMode == ThumbMode.View) {
                        toggleSelectSlip(setMode = ThumbMode.Edit)
//                    viewMode = ThumbMode.Edit
//                    view_rippleBtnRemove.visibility = View.VISIBLE
                    }

                    selectThumbItem(position)
                    true
                }
            }


            super.onBindViewHolder(holder, position)
        }
    }
}

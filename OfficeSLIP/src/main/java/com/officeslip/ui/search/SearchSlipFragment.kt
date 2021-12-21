package com.officeslip.ui.search

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.officeslip.util.Common
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.officeslip.*
import com.officeslip.base.BaseFragment
import com.officeslip.base.BaseRecyclerView
import com.officeslip.data.model.*
import com.officeslip.databinding.FragmentSearchSlipBinding
import com.officeslip.databinding.SearchSlipListItemBinding
import com.officeslip.ui.addslip.extension.ExtensionActivity
import com.officeslip.ui.property.PropertyActivity
import com.officeslip.ui.viewer.SlipViewerActivity
import com.officeslip.ui.main.MainViewModel
import com.officeslip.ui.main.SharedMainViewModel
import com.officeslip.ui.search.option.SearchSlipOptionActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.subjects.PublishSubject

@AndroidEntryPoint
class SearchSlipFragment : BaseFragment<FragmentSearchSlipBinding, SearchSlipViewModel>(), SwipeRefreshLayout.OnRefreshListener {

    override val layoutResourceId: Int
        get() =  R.layout.fragment_search_slip
    override val viewModel by viewModels<SearchSlipViewModel>()

    private val m_C = Common()
    private lateinit var progress: AlertDialog

    private lateinit var searchSlipOption: SearchSlipOption

    companion object {
        const val CUR_SEARCH_OPTION = "CUR_SEARCH_OPTION"
    }

    override fun initStartView() {

        activity?.let {
            binding.mainViewModel = viewModels<MainViewModel>({ requireActivity() }).value
            binding.sharedViewModel = activityViewModels<SharedMainViewModel>().value
            binding.fragment = this@SearchSlipFragment
        }

        setDefaultSearchOption()
        setActionBar()
        binding.swipeRefresh.setOnRefreshListener(this)

        binding.viewRcSearchSlip.adapter = adapter.apply {

            items = ArrayList<SearchSlipResultItem>()

            viewModel.addDisposable(
            showSlip.subscribe { position ->
                adapter.showItemAt(position)
            },
            removeSlip.subscribe { position ->
                adapter.removeItemAt(position)
//                notifyItemChanged(adapter.removeItemAt(position))
            },
            openProperty.subscribe { position ->
                adapter.openPropertyAt(position)
            },
            moveSlip.subscribe { position ->
                adapter.moveSlipAt(position)
            },
            copySlip.subscribe { position ->
                adapter.copySlipAt(position)
            },
            )
        }
    }

    override fun onRefresh() {
        viewModel.getSlipList(searchSlipOption)
    }

    override fun initDataBinding() {

        viewModel.searchResult.observe(viewLifecycleOwner, Observer {
            binding.swipeRefresh.isRefreshing = false

            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                        activity
                        , null
                        , it.message
                    ) { }

                }
                Status.LOADING -> {
                    progress.show()
                }
            }
        }).apply {
            progress = m_C.getCircleProgress(activity) {
                viewModel.stopAgentExecution()
            }
        }

        viewModel.getSlipItem.observe(viewLifecycleOwner, {
            when(it.status) {
                Status.LOADING -> {
                    progress.show()
                }
                Status.SUCCESS -> {
                    it.data?.let { list ->
                        progress.dismiss()
                        openSlipViewer(list)
                    } ?: run {

                    }
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                        activity
                        , null
                        , it.message
                    ) { }

                }
            }
        }).apply {
            progress = m_C.getCircleProgress(activity) {
                viewModel.stopAgentExecution()
            }
        }

//        viewModel.downAttach.observe(viewLifecycleOwner, Observer {
//            when(it.status) {
//                Status.SUCCESS -> {
//                    progress.dismiss()
//                }
//                Status.ERROR -> {
//                    progress.dismiss()
//                    m_C.simpleAlert(
//                        activity
//                        , null
//                        , it.message
//                    ) { }
//
//                }
//                Status.LOADING -> {
//                    progress.show()
//                }
//            }
//        }).apply {
//            progress = m_C.getCircleProgress(activity) {
//            }
//        }


        viewModel.removeSlip.observe(viewLifecycleOwner, Observer {

            when(it.status) {
                Status.SUCCESS -> {
                    progress.dismiss()
                    it.data?.let {position ->
                        //adapter.notifyItemRemoved(position)
                        adapter.items.removeAt(position)
                        adapter.notifyDataSetChanged()
                    } ?: run {
                        viewModel.getSlipList(searchSlipOption)
                    }
                }
                Status.ERROR -> {
                    progress.dismiss()
                    m_C.simpleAlert(
                        activity
                        , null
                        , it.message
                    ) { }

                }
                Status.LOADING -> {
                    progress.show()
                }
            }
        }).apply {
            progress = m_C.getCircleProgress(activity) {
                viewModel.stopAgentExecution()
            }
        }

        viewModel.filteredSearchResult.observe(this, Observer {

            it.data?.let {
                (binding.viewRcSearchSlip.adapter as? BaseRecyclerView.Adapter<Any, *>)?.run {
                    replaceAll(it as List<Any>)
                    notifyDataSetChanged()
                }
            }

        })
    }


    private fun openSlipViewer(slipList:ArrayList<Slip>) {
        openSlipViewerRegistry.launch(Intent(activity, SlipViewerActivity::class.java).apply {
            putExtra(VIEWER_FLAG, ViewFlag.Search)
            putExtra(SLIP_ITEM, slipList)
        })
    }
    private val openSlipViewerRegistry = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
//        if (result.resultCode == Activity.RESULT_OK) {
//        }
    }


    override fun initAfterBinding() {
        viewModel.getSlipList(searchSlipOption)
    }

    private fun setDefaultSearchOption() {
        searchSlipOption = SearchSlipOption(
                SysInfo.userInfo[userId].asString,
                SysInfo.userInfo[userNm].asString,
                SysInfo.userInfo[partNo].asString,
                SysInfo.userInfo[partNm].asString,
                SysInfo.userInfo[corpNo].asString,
                SysInfo.userInfo[corpNm].asString,
        )
    }

    private fun setActionBar() {
        (activity as? AppCompatActivity)?.apply {

            val tool = view?.findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(tool)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    fun moveToSearchOption() {
        Intent(activity, SearchSlipOptionActivity::class.java).run {
            putExtra(CUR_SEARCH_OPTION, searchSlipOption)
            startActivityForResult(this, RESULT_SEARCH_SLIP_OPTION)
        }
    }

    private fun openPropertyViewer(sdocNo:String, folder:String = "SLIPDOC") {
        openPropertyViewerRegistry.launch(Intent(activity, PropertyActivity::class.java).apply {
            putExtra(BUNDLE_SDOC_NO, sdocNo)
            putExtra(BUNDLE_FOLDER, folder)
        })
    }
    private val openPropertyViewerRegistry = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult -> }

    private fun openExtensionViewer(sdocNo:String, flag:ExtensionFlag) {
        openExtensionRegistry.launch(Intent(activity, ExtensionActivity::class.java).apply {
            putExtra(BUNDLE_SDOC_NO, sdocNo)
            putExtra(ExtensionActivity.ACTION_FLAG, flag)
        })
    }
    private val openExtensionRegistry = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.getSlipList(searchSlipOption)
        }
    }


    private val adapter = object : BaseRecyclerView.Adapter<SearchSlipResultItem, SearchSlipListItemBinding>(
        layoutResId = R.layout.search_slip_list_item,
        bindingVariableId = BR.slipItem
    ) {


        val showSlip = PublishSubject.create<SearchListViewItem>()
        val removeSlip = PublishSubject.create<SearchListViewItem>()
        val moveSlip = PublishSubject.create<SearchListViewItem>()
        val copySlip = PublishSubject.create<SearchListViewItem>()
        val openProperty = PublishSubject.create<SearchListViewItem>()


        //Bind event listener programmatically
        override fun onBindViewHolder(
            holder: BaseRecyclerView.ViewHolder<SearchSlipListItemBinding>,
            position: Int
        ) {
            holder.itemView.findViewById<Button>(R.id.btn_removeSlip).setOnClickListener {
                removeSlip.onNext(SearchListViewItem(it, position))
                holder.itemView.findViewById<SwipeRevealLayout>(R.id.swipe).close(true)
            }
            holder.itemView.findViewById<Button>(R.id.btn_openProperty).setOnClickListener {
                openProperty.onNext(SearchListViewItem(it, position))
                holder.itemView.findViewById<SwipeRevealLayout>(R.id.swipe).close(true)
            }
            holder.itemView.findViewById<Button>(R.id.btn_moveSlip).setOnClickListener {
                moveSlip.onNext(SearchListViewItem(it, position))
                holder.itemView.findViewById<SwipeRevealLayout>(R.id.swipe).close(true)
            }
            holder.itemView.findViewById<Button>(R.id.btn_copySlip).setOnClickListener {
                copySlip.onNext(SearchListViewItem(it, position))
                holder.itemView.findViewById<SwipeRevealLayout>(R.id.swipe).close(true)
            }
            holder.itemView.findViewById<LinearLayout>(R.id.view_layout).setOnClickListener {
                showSlip.onNext(SearchListViewItem(it, position))
                holder.itemView.findViewById<SwipeRevealLayout>(R.id.swipe).close(true)
            }
            super.onBindViewHolder(holder, position)
        }


        fun removeItemAt(vi:SearchListViewItem) {
            m_C.simpleConfirm(activity as Activity, null, getString(R.string.confirm_remove_slip),{
//                view_rcSearchSlip.childCount
                viewModel.removeSlip(items[vi.position], vi.position)

            },{})
        }
        fun openPropertyAt(vi:SearchListViewItem) {
            openPropertyViewer(items[vi.position].sdocNo, items[vi.position].folder)
        }

        fun copySlipAt(vi:SearchListViewItem) {
            openExtensionViewer(items[vi.position].sdocNo, ExtensionFlag.Copy)
        }

        fun moveSlipAt(vi:SearchListViewItem) {
            openExtensionViewer(items[vi.position].sdocNo, ExtensionFlag.Move)
        }

        fun showItemAt(vi:SearchListViewItem) {
            val item = items[vi.position]

            when(item.folder.toUpperCase()) {
                "SLIPDOC" -> {
                    viewModel.getSlipItem(item)
                }
                else -> {
//                    val progress = vi.view.findViewById<ProgressBar>(R.id.prog_download).apply {
//                        visibility = View.VISIBLE
//                        max = 100
//                        isIndeterminate = false
//                    }
//
//                    val progData = LiveEvent<AgentResponse<DownloadProgress>>().apply {
//                        observe(viewLifecycleOwner, Observer {
//
//                            when(it.status) {
//                                Status.ERROR -> {
//                                    progress.visibility = View.GONE
//                                }
//                                Status.LOADING -> {
//                                    it.data?.let { proc ->
//                                        progress.progress = proc.progress
//                                    } ?: run {   }
//                                }
//                                Status.SUCCESS -> {
//                                    viewModel.showSnackbar(R.string.success_download_file)
//                                    progress.visibility = View.GONE
//                                }
//                            }
//                        })
//                    }

                    m_C.simpleAlert(activity
                        ,null
                        ,getString(R.string.download_attach_not_allowed_on_mobile)
                    ) { }
//                    viewModel.downloadAttach(items[vi.position], vi.position, progData)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RESULT_SEARCH_SLIP_OPTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.extras?.let {
                        searchSlipOption = it.get(CUR_SEARCH_OPTION) as SearchSlipOption
                        viewModel.getSlipList(searchSlipOption)
                    }

                }
            }
        }
    }

}
package com.officeslip.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.officeslip.BR


abstract class BaseFragment<V : ViewDataBinding, R : BaseViewModel> : Fragment() {
    abstract val viewModel: R

    abstract val layoutResourceId: Int

    lateinit var binding: V
    lateinit var getContents:ActivityResultRegistry

    lateinit var mActivity:BaseActivity<*,*>

    private var viewContainer: ViewGroup? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is BaseActivity<*, *>) {
            mActivity = context
            mActivity.onFragmentAttached()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Fragment Option Menu 사용
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, layoutResourceId, container, false)
        if(viewContainer != container) {
            viewContainer = container
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setVariable(BR.viewModel, viewModel)
        binding.lifecycleOwner = this
//        binding.setVariable(BR.activity, mActivity)
        binding.executePendingBindings()

        snackbarObserving()
        initStartView()
        initDataBinding()
        initAfterBinding()
    }

  //  abstract fun getBindingVariable():Int
    /**
     * Do your other stuff in init after binding layout.
     */
    abstract fun initStartView()

    abstract fun initDataBinding()

    abstract fun initAfterBinding()


    fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        viewContainer?.run {
            imm.hideSoftInputFromWindow(this.windowToken, 0)
        }
    }

    fun showKeyboard(v: EditText) {
        if( v.isAttachedToWindow ) {
            v.isFocusableInTouchMode = true
            v.requestFocus()
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(v, 0)
        } else {
            v.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    v.let {
                        it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        v.isFocusableInTouchMode = true
                        v.requestFocus()
                        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(v, 0)
                    }
                }
            })
        }
    }

    interface Callback {
        fun onFragmentAttached()
        fun onFragmentDetached(tag: String?)
    }

    private fun snackbarObserving() {
        val snack = Snackbar.make(mActivity.findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG)
        snack.view.setBackgroundColor(ContextCompat.getColor(mActivity, com.officeslip.R.color.colorPrimary))
        viewModel.observeSnackbarMessage(this) {
            snack.setText(it).show()
        }
        viewModel.observeSnackbarMessageStr(this){
            snack.setText(it).show()
        }
    }
}
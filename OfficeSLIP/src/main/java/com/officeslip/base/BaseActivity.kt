package com.officeslip.base

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import com.officeslip.BR


/**
 * BaseKotlinActivity<ActivitySbsMainBinding>
 * 와 같이 상속 받을 때, ActivitySbsMainBinding 과 같은 파일이 자동생성되지 않는다면
 * 1. 해당 엑티비티의 레이아웃이 <layout></layout> 으로 감싸져 있는지 확인
 * 2. 다시 빌드 수행 or 클린 빌드 후 다시 빌드 수행
 * 3. 이름 확인 : sbs_main_activity => ActivitySbsMainBinding
 */
abstract class BaseActivity<T : ViewDataBinding, R : BaseViewModel> : AppCompatActivity(), BaseFragment.Callback {

    lateinit var binding: T

    /**
     * setContentView로 호출할 Layout의 리소스 Id.
     * ex) R.layout.activity_sbs_main
     */
    abstract val layoutResourceId: Int

    /**
     * viewModel 로 쓰일 변수.
     */
    abstract val viewModel: R

    /**
     * 레이아웃을 띄운 직후 호출.
     * 뷰나 액티비티의 속성 등을 초기화.
     * ex) 리사이클러뷰, 툴바, 드로어뷰..
     */
    abstract fun initStartView()

    /**
     * 두번째로 호출.
     * 데이터 바인딩 및 rxjava 설정.
     * ex) rxjava observe, databinding observe..
     */
    abstract fun initDataBinding()

    /**
     * 바인딩 이후에 할 일을 여기에 구현.
     * 그 외에 설정할 것이 있으면 이곳에서 설정.
     * 클릭 리스너도 이곳에서 설정.
     */
    abstract fun initAfterBinding()

   // abstract fun getBindingVariable():Int


    private var isSetBackButtonValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, layoutResourceId)
        binding.setVariable(BR.viewModel, viewModel)
        binding.executePendingBindings()

        binding.setLifecycleOwner { this.lifecycle } //= this.lifecycle

        snackbarObserving()
        initStartView()
        initDataBinding()
        initAfterBinding()

    }

    override fun onFragmentAttached() {

    }


    override fun onFragmentDetached(tag: String?) {

    }


    fun hideKeyboard() {
        this.currentFocus?.let { view ->
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun snackbarObserving() {
        viewModel.observeSnackbarMessage(this) {
            Snackbar.make(findViewById(android.R.id.content), it, Snackbar.LENGTH_LONG).show()
        }
        viewModel.observeSnackbarMessageStr(this){
            Snackbar.make(findViewById(android.R.id.content), it, Snackbar.LENGTH_LONG).show()
        }
    }
}
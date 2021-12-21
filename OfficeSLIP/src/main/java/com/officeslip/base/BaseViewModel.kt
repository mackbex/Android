package com.officeslip.base

import android.util.Log
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable


open class BaseViewModel : ViewModel(), Observable {

    /**
     * RxJava 의 observing을 위한 부분.
     * addDisposable을 이용하여 추가하기만 하면 된다
     */
    private val compositeDisposable = CompositeDisposable()
    private val snackbarMessage = LiveEvent<Int>()
    private val snackbarMessageString = LiveEvent<String>()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
        Log.d("disposable size : " ,"${compositeDisposable.size()}")
    }
    fun addDisposable(vararg disposables: Disposable) {
        for (disposable in disposables) {
            compositeDisposable.add(disposable)
        }
        Log.d("disposable size : " ,"${compositeDisposable.size()}")
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }


    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with [Bindable] to generate a field in
     * `BR` to be used as `fieldId`.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }


    /**
     * 스낵바를 보여주고 싶으면 viewModel 에서 이 함수를 호출
     */
    fun showSnackbar(stringResourceId:Int) {
        snackbarMessage.value = stringResourceId
    }
    fun showSnackbar(str:String){
        snackbarMessageString.value = str
    }


    /**
     * BaseKotlinActivity 에서 쓰는 함수
     */
    fun observeSnackbarMessage(lifeCycleOwner: LifecycleOwner, ob:(Int) -> Unit){
        snackbarMessage.observe(lifeCycleOwner, Observer {
            it?.run{
                ob(it)
            }
        })
    }
    fun observeSnackbarMessageStr(lifeCycleOwner: LifecycleOwner, ob:(String) -> Unit) {
        snackbarMessageString.observe(lifeCycleOwner, Observer{
            it?.run{
                ob(it)
            }
        })
    }
}
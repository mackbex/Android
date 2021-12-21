package com.officeslip.ui.main

import android.app.Application
import android.content.Context


import androidx.lifecycle.SavedStateHandle
import com.hadilq.liveevent.LiveEvent
import com.officeslip.PageType
import com.officeslip.base.BaseViewModel
import com.officeslip.data.local.AppInfoRepository
import com.officeslip.data.model.AppInfo
import com.officeslip.SysInfo
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.notifyObserver
import com.officeslip.util.Common
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository,
    private val appInfo: AppInfoRepository
    ) : BaseViewModel(){

    private val m_C = Common()
    val naviFlag : LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }
    val logout : LiveEvent<Boolean> by lazy {
        LiveEvent<Boolean>()
    }

    val pagerFlag : LiveEvent<PageType> by lazy {
        LiveEvent<PageType>()
    }

    fun toggleNavi() {
        naviFlag.notifyObserver()
//        naviFlag.apply {
//             postValue(value?.not())
//        }
    }

    fun onLogout() {
        Observable.just(appInfo)
            .subscribeOn(Schedulers.io())
            .subscribe { db ->
                SysInfo.IS_LOGGED = false
                db.upsert(AppInfo("IS_LOGGED", "0"))
                    logout.postValue(true)
            }
    }
}

package com.officeslip.ui.qr

import android.app.Application
import android.content.Context


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.base.BaseViewModel
import com.officeslip.data.local.AppInfoRepository
import com.officeslip.data.model.*
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.util.Common
import com.officeslip.util.agent.PreparedStatement
import com.officeslip.util.log.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

@HiltViewModel
class QRScanViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository,
    private val appInfo: AppInfoRepository
    ) : BaseViewModel() {

    private val m_C = Common()


}
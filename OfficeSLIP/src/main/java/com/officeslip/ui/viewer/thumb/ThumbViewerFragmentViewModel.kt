package com.officeslip.ui.viewer.thumb

import android.app.Application
import android.content.Context


import androidx.lifecycle.SavedStateHandle
import com.officeslip.base.BaseViewModel
import com.officeslip.data.remote.agent.AgentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

@HiltViewModel
class ThumbViewerFragmentViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository
    ) : BaseViewModel(){
//    val listSlip : LiveEvent<List<Slip>> by lazy {
//        LiveEvent<List<Slip>>()
//    }
}

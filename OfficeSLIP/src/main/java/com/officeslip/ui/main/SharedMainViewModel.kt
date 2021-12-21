package com.officeslip.ui.main

import android.app.Application
import android.content.Context


import androidx.lifecycle.SavedStateHandle
import com.hadilq.liveevent.LiveEvent
import com.officeslip.*
import com.officeslip.base.BaseViewModel
import com.officeslip.SysInfo
import com.officeslip.data.model.User
import com.officeslip.data.remote.agent.AgentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

@HiltViewModel
class SharedMainViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val agent: AgentRepository
) : BaseViewModel() {


    val userInfo: LiveEvent<User> by lazy {
        LiveEvent<User>()
    }

    val test = LiveEvent<String>()

    val currentPageType: LiveEvent<PageType> by lazy {
        LiveEvent<PageType>()
    }

    fun select(str: String) {
        test.value = str
    }

}
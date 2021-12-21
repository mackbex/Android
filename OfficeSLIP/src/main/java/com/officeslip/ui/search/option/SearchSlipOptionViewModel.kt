package com.officeslip.ui.search.option

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.hadilq.liveevent.LiveEvent
import com.officeslip.base.BaseViewModel
import com.officeslip.data.model.SearchSlipOption
import com.officeslip.data.remote.agent.AgentRepository
import com.officeslip.util.Common
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

@HiltViewModel
class SearchSlipOptionViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    val agent: AgentRepository
    ) : BaseViewModel() {

    val m_C = Common()
    val searchSlipOption : LiveEvent<SearchSlipOption> by lazy {
        LiveEvent<SearchSlipOption>()
    }

    var folderSlip:Boolean
        get() {
            searchSlipOption.value?.let{option ->
                return option.folderSlip
            } ?: run {
                return true
            }
        }
        set(value) {
            searchSlipOption.value?.let{option ->
                option.folderSlip = value
                searchSlipOption.postValue(option)
            }
        }

    var folderAttach:Boolean
        get() {
            searchSlipOption.value?.let{option ->
                return option.folderAttach
            } ?: run {
                return true
            }
        }
        set(value) {
            searchSlipOption.value?.let{option ->
                option.folderAttach = value
                searchSlipOption.postValue(option)
            }
        }

    var stepUsed:Boolean
        get() {
            searchSlipOption.value?.let{option ->
                return option.stepUsed["CHECKED"] as Boolean
            } ?: run {
                return true
            }
        }
        set(value) {
            searchSlipOption.value?.let{option ->
                option.stepUsed["CHECKED"] = value
                searchSlipOption.postValue(option)
            }
        }
    var stepUnused:Boolean
        get() {
            searchSlipOption.value?.let{option ->
                return option.stepUnused["CHECKED"] as Boolean
            } ?: run {
                return true
            }
        }
        set(value) {
            searchSlipOption.value?.let{option ->
                option.stepUnused["CHECKED"] = value
                searchSlipOption.postValue(option)
            }
        }
    var stepRemoved:Boolean
        get() {
            searchSlipOption.value?.let{option ->
                return option.stepRemoved["CHECKED"] as Boolean
            } ?: run {
                return false
            }
        }
        set(value) {
            searchSlipOption.value?.let{option ->
                option.stepRemoved["CHECKED"] = value
                searchSlipOption.postValue(option)
            }
        }
}
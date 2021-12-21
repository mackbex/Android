package com.example.practice.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice.model.Blog
import com.example.practice.repo.MainRepository
import com.example.practice.util.DataState
import com.hadilq.liveevent.LiveEvent
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val savedStateHandle: SavedStateHandle
    ):ViewModel() {

    private val _dataState:LiveEvent<DataState<List<Blog>>> = LiveEvent()
    val dataState:LiveData<DataState<List<Blog>>>
    get() = _dataState

    fun setStateEvent(mainStateEvent: MainStateEvent) {
        viewModelScope.launch {
            when (mainStateEvent) {
                is MainStateEvent.GetBlogEvents -> {
                    mainRepository.getBlog()
                        .onEach { dataState ->
                            _dataState.value = dataState
                        }
                        .launchIn(viewModelScope)
                }

                is MainStateEvent.None -> {
                    // No action
                }
            }
        }
    }

}


sealed class MainStateEvent {
    object GetBlogEvents : MainStateEvent()
    object None : MainStateEvent()
}

package com.material.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SharedViewModel @Inject constructor():ViewModel() {

    private var itemIndex = 0

    fun nextIndex():Int {
        itemIndex++
        return itemIndex
    }
}
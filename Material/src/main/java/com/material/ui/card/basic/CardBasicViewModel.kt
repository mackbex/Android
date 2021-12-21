package com.material.ui.card.basic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.usecases.CardItemUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardBasicViewModel @Inject constructor(
    private val cardItemUsecase: CardItemUsecase
) : ViewModel() {

    val cardViewItemState = cardItemUsecase.cardViewItemState

    fun getCardViewItem() {
        viewModelScope.launch(Dispatchers.IO) {
            cardItemUsecase.getCardItem()
        }
    }
}
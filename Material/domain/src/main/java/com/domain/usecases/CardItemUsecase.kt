package com.domain.usecases

import com.data.util.DataState
import com.domain.model.CardView
import com.domain.repos.BlogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class CardItemUsecase @Inject constructor(
    private val blogRepository: BlogRepository
) {
    private var _cardViewItemState = MutableStateFlow<DataState<List<CardView>>>(DataState.Loading)
    val cardViewItemState = _cardViewItemState


    suspend fun getCardItem() {
        blogRepository.getBlog().collect {
            _cardViewItemState.value = when(it) {
                is DataState.Success -> {
                    DataState.Success(it.data.map { blog ->
                        CardView(
                            title = blog.title,
                            image = blog.image,
                            body = blog.body
                        )
                    })
                }
                is DataState.Error -> {
                    DataState.Error(it.e)
                }
                DataState.Loading -> {
                    DataState.Loading
                }
            }
        }
    }
}
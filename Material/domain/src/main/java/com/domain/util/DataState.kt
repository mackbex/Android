package com.data.util

sealed class DataState<out R> {
    data class Success<T>(val data: T) : DataState<T>()
    data class Error(val e: Exception) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
}
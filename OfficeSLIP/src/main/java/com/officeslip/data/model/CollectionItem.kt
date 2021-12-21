package com.officeslip.data.model


enum class CollectionFlag {
    ADD,
    REMOVE,
    REPLACE
}

data class CollectionItem<out T>(val flag: CollectionFlag, val data: T) {
    companion object {
        fun <T> add(data: T): CollectionItem<T> {
            return CollectionItem(
                CollectionFlag.ADD,
                data
            )
        }

        fun <T> remove(data: T): CollectionItem<T> {
            return CollectionItem(
                CollectionFlag.REMOVE,
                data
            )
        }

        fun <T> replace(data: List<T>): CollectionItem<List<T>> {
            return CollectionItem(
                CollectionFlag.REPLACE,
                data
            )
        }
    }
}
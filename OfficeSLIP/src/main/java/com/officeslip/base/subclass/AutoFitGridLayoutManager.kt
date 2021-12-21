package com.officeslip.base.subclass

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class AutoFitGridLayoutManager(context: Context?, columnWidth: Int) : GridLayoutManager(context, 1) {

    private var columnWidth: Int = 0
    private var columnWidthChanged = true
    private var columHeight:Int = 0
    private var columnHeightChanged = true
    init {

        setColumnWidth(columnWidth)
    }

    fun setColumnWidth(newColumnWidth: Int) {
        if (newColumnWidth > 0 && newColumnWidth != columnWidth) {
            columnWidth = newColumnWidth
            columnWidthChanged = true
        }
    }


    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
        if (columnWidthChanged && columnWidth > 0) {
            val totalSpace: Int
            if (orientation == LinearLayoutManager.VERTICAL) {
                totalSpace = width - paddingRight - paddingLeft
            } else {
                totalSpace = height - paddingTop - paddingBottom
            }
            val spanCount = max(1, totalSpace / columnWidth)
            setSpanCount(spanCount)
            columnWidthChanged = false
        }
        super.onLayoutChildren(recycler, state)
    }
}
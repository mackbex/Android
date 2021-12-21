package com.officeslip.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.officeslip.BR
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

abstract class BaseRecyclerView {
    abstract class Adapter<ITEM : Any, B : ViewDataBinding>(
        @LayoutRes private val layoutResId: Int,
        private val bindingVariableId: Int? = null
    ) : RecyclerView.Adapter<ViewHolder<B>>() {


        lateinit var items: MutableList<ITEM>

        fun replaceAll(items: List<ITEM>?) {
            items?.let {
                this.items.run {
                    clear()
                    addAll(it)
                }
            }
        }

        fun clearItem() {
            this.items.clear()
        }

        fun addItem(item:ITEM) {
            this.items.run {
                add(item)
            }
        }

        fun removeItem(position:Int) {
            this.items.run {
                removeAt(position)
            }
        }

        fun getItem(position: Int): ITEM {
            return items[position]
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            object : ViewHolder<B>(
                layoutResId = layoutResId,
                parent = parent,
                bindingVariableId = bindingVariableId
            ) {}

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: ViewHolder<B>, position: Int) {
            holder.onBindViewHolder(items[position], position)
        }
    }

    abstract class ViewHolder<B : ViewDataBinding>(
        @LayoutRes layoutResId: Int,
        parent: ViewGroup,
        private val bindingVariableId: Int?
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
    ) {

        val binding: B = DataBindingUtil.bind(itemView)!!

        fun onBindViewHolder(item: Any?, position: Int) {
            try {
                bindingVariableId?.let {
                    binding.setVariable(it, item)
                    binding.setVariable(BR.position, position)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

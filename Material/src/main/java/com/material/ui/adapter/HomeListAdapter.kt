package com.material.ui.adapter

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.domain.model.MainList
import com.material.R
import com.material.databinding.ListitemHomeBinding
import com.material.util.ext.inflate

class HomeListAdapter() : ListAdapter<MainList, HomeListAdapter.ViewHolder>(ItemDiff) {

    object ItemDiff :DiffUtil.ItemCallback<MainList>() {
        override fun areItemsTheSame(oldEntity: MainList, newEntity: MainList): Boolean {
            return oldEntity == newEntity
        }

        override fun areContentsTheSame(oldEntity: MainList, newEntity: MainList): Boolean {
            return oldEntity == newEntity
        }
    }


    inner class ViewHolder(private val binding:ListitemHomeBinding):RecyclerView.ViewHolder(binding.root) {
        fun bindTo(entity:MainList) {
            binding.layoutPanel.setOnClickListener(binding.root)
            binding.layoutPanel.tvTitle.text = entity.title
            binding.layoutPanel.iconMenu.setImageDrawable(ContextCompat.getDrawable(binding.root.context, entity.icon))
            binding.layoutPanel.itemId = entity.id
            entity.items?.run {
                binding.layoutPanel.setItems(this)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ListitemHomeBinding.bind(parent.inflate(R.layout.listitem_home))
    )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindTo(getItem(position))


}
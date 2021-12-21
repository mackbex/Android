package com.material.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.domain.model.CardView
import com.material.R
import com.material.databinding.ListitemCardBasicBinding
import com.material.util.ext.inflate
import com.material.util.ext.load

class CardBasicAdapter() : ListAdapter<CardView, CardBasicAdapter.ViewHolder>(ItemDiff) {

    object ItemDiff :DiffUtil.ItemCallback<CardView>() {
        override fun areItemsTheSame(oldEntity: CardView, newEntity: CardView): Boolean {
            return oldEntity == newEntity
        }

        override fun areContentsTheSame(oldEntity: CardView, newEntity: CardView): Boolean {
            return oldEntity == newEntity
        }
    }


    inner class ViewHolder(private val binding:ListitemCardBasicBinding):RecyclerView.ViewHolder(binding.root) {
        fun bindTo(entity:CardView) {

            binding.imgMain.load(entity.image)
            binding.tvTitle.text = entity.title
            binding.tvBody.text = entity.body
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ListitemCardBasicBinding.bind(parent.inflate(R.layout.listitem_card_basic))
    )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindTo(getItem(position))


}
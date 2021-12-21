package com.example.practice.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.practice.R
import com.example.practice.databinding.ItemBlogBinding
import com.example.practice.model.Blog

class BlogAdapter(private val listener: OnItemClickListener) : RecyclerView.Adapter<BlogAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(v:TextView, position: Int)
    }

    private val items = ArrayList<Blog>()
    private lateinit var blog: Blog

    fun setItems(items: ArrayList<Blog>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemBlogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            listener.onItemClick(holder.binding.textTitle, position)
        }
    }


    inner class ViewHolder(val binding:ItemBlogBinding):RecyclerView.ViewHolder(binding.root){



        fun bind(blog:Blog){
//            binding.textTitle.text = blog.title
//            binding.textDescription.text = blog.body
            binding.model = blog
            Glide.with(binding.blogLayout).load(blog.image)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .apply(RequestOptions().centerCrop())
                .into(binding.image)

            binding.executePendingBindings()


        }
    }
}

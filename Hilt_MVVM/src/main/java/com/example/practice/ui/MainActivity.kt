package com.example.practice.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practice.BR
import com.example.practice.R
import com.example.practice.databinding.ActivityMainBinding
import com.example.practice.model.Blog
import com.example.practice.util.DataState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity()  {

    private lateinit var binding:ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: BlogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.apply {
            setVariable(BR.viewModel, viewModel)
            executePendingBindings()
            setLifecycleOwner { this@MainActivity.lifecycle }
        }

        initStartView()
        initDataBinding()
        initAfterBinding()
    }

    private fun initStartView(){
        adapter = BlogAdapter(object :BlogAdapter.OnItemClickListener {
            override fun onItemClick(v: TextView, position: Int) {
                Toast.makeText(this@MainActivity, v.text, Toast.LENGTH_SHORT).show()
            }
        })
        binding.blogRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.blogRecyclerview.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.setStateEvent(MainStateEvent.GetBlogEvents)
        }

    }

    private fun initDataBinding(){
        viewModel.dataState.observe(this, Observer { dataState ->
            when (dataState) {
                is DataState.Success<List<Blog>> -> {
                    displayLoading(false)
                    populateRecyclerView(dataState.data)
                }
                is DataState.Loading -> {
                    displayLoading(true)
                }
                is DataState.Error -> {
                    displayLoading(false)
                    displayError(dataState.exception.message)
                }
            }
        })
    }

    private fun initAfterBinding(){
        viewModel.setStateEvent(MainStateEvent.GetBlogEvents)
    }

    private fun displayError(message:String?) {
        if(!message.isNullOrEmpty()){
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
        }
        else {
            Snackbar.make(findViewById(android.R.id.content), "Unknown error", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun populateRecyclerView(blogs: List<Blog>) {
        if (blogs.isNotEmpty()) adapter.setItems(ArrayList(blogs))
    }

    private fun displayLoading(isLoading: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isLoading
    }


}
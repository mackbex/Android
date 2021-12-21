package com.material.ui.card.basic

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.data.util.DataState
import com.google.android.material.transition.MaterialContainerTransform
import com.material.R
import com.material.databinding.FragmentCardBasicBinding
import com.material.ui.adapter.CardBasicAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CardBasicFragment:Fragment(R.layout.fragment_card_basic) {
    private var _binding: FragmentCardBasicBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CardBasicViewModel by viewModels()
    private val mainAdapter = CardBasicAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCardBasicBinding.bind(view)

        with(binding.recycleMain) {
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            adapter = mainAdapter
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.cardViewItemState.collect {
                        when(it) {
                            is DataState.Success -> {
                                mainAdapter.submitList(it.data)
                            }
                            is DataState.Error -> {

                            }
                            is DataState.Loading -> {}
                        }

                    }
                }
            }
        }

        viewModel.getCardViewItem()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
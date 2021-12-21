package com.material.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.domain.model.MainList
import com.domain.model.MaterialItem
import com.material.R
import com.material.databinding.FragmentHomeBinding
import com.material.ui.SharedViewModel
import com.material.ui.adapter.HomeListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
    private val sharedViewModel:SharedViewModel by activityViewModels()

    private val mainAdapter = HomeListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        exitTransition = Hold()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentHomeBinding.bind(view)

        with(binding.recycleMain) {
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            adapter = mainAdapter
        }


        mainAdapter.submitList(listOf(
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Card",
                arrayListOf(
                    MaterialItem("Basic") {
                        val extras = FragmentNavigatorExtras(it to getString(R.string.transition_main_item_click))
                        findNavController().navigate(R.id.action_nav_home_to_nav_card_basic, null, null, extras)
                    },
                    MaterialItem("Dummy") {
                       // Toast.makeText(context, "Dummy", Toast.LENGTH_SHORT).show()
                    },
                    MaterialItem( "test1") {
                    },
                    MaterialItem( "test2") {
                    },
                    MaterialItem( "global") {
                    },
                    MaterialItem( "global") {
                    },
                    MaterialItem("global") {

                    },
                )
            ),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            MainList(sharedViewModel.nextIndex(), R.drawable.ic_baseline_view_list_24, "Dummy"),
            ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
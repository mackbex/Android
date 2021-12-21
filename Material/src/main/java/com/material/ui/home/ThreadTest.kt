package com.material.ui.home

import android.util.Log
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.material.R
import kotlinx.coroutines.*
import java.util.*

//fun getJob(scope:CoroutineScope, name:String):Job {
//        return  scope.launch(start = CoroutineStart.LAZY) {
//            for(index in 0..20) {
//                Log.d(name,"$index")
//                delay(1000)
//            }
//        }
//    }

//var dispatcher = Executors.newFixedThreadPool(100).asCoroutineDispatcher()
//
//
//var scope = CoroutineScope(dispatcher)
//
//var test1 = getJob(scope, "test1")
//var test2 = getJob(scope, "test2")
//val list = Stack<Job>()
//
//listOf(
//MaterialItem(1,"Basic") {
//    val extras = FragmentNavigatorExtras(it to getString(R.string.transition_main_item_click))
//    findNavController().navigate(R.id.action_nav_home_to_nav_card_basic, null, null, extras)
//},
//MaterialItem(2,"Dummy") {
//    // Toast.makeText(context, "Dummy", Toast.LENGTH_SHORT).show()
//    test1.start()
//    test2.start()
//},
//MaterialItem(3, "test1") {
//    test1.cancel()
//    test1 = getJob(scope, "test1")
//},
//MaterialItem(4, "test2") {
//    test2.cancel()
//    test2 = getJob(scope, "test2")
//},
//MaterialItem(5, "global") {
//    scope.coroutineContext.cancelChildren()
//    scope = CoroutineScope(dispatcher)
//
//    test1 = getJob(scope, "test1")
//    test2 = getJob(scope, "test2")
//},
//MaterialItem(6, "global") {
//
//    val temp = scope.launch {
//        for(index in 0..100) {
//            Log.d("test$j","$index")
//            delay(1000)
//        }
//    }
//    j++
//    list.add(temp)
//},
//MaterialItem(5, "global") {
//
//    if(!list.isEmpty()) {
//        list.pop().cancel()
//
//        if (list.size <= 0) {
//            scope = CoroutineScope(dispatcher)
//        }
//    }
//},
//)
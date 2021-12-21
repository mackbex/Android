package com.officeslip.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    companion object {
        const val NONE_EXISTS = -999
    }

    private val m_mapFragments:LinkedHashMap<String, Fragment> = LinkedHashMap()
//    private var m_curFragment:Fragment? = null

    private var currentTag:String? = null

    override fun getItemCount(): Int = m_mapFragments.size

    override fun createFragment(position: Int): Fragment {
        var resFrag:Fragment? = null
        var nIdx = 0
        for ((key, value) in m_mapFragments) {

            if(nIdx == position)
            {
                resFrag = m_mapFragments[key]
                break
            }
            nIdx++
        }

        return resFrag!!
    }

    fun addFragment(fragment:Fragment, tag:String?) {

        var frag:Fragment? = null

        tag?.run {
            if(m_mapFragments.get(tag) == null)
            {
                m_mapFragments.put(tag, fragment)
                frag = fragment
            }
            else
            {
                frag = m_mapFragments.get(tag)
            }

//            currentTag = tag

            notifyDataSetChanged()
        }
    }

    fun getFragmentByTag(tag:String?):Fragment? {
        tag?.apply {
            for ((key, value) in m_mapFragments) {

                if(key == tag)
                {
                    return value
                }
            }
        }
        return null
    }

    fun getCurrentFragTag():String? {
        return currentTag
    }

    fun getFragItemAtPosition(tag:String?):Int {
        var nResIdx = NONE_EXISTS
        var nIdx = 0
        tag?.apply {
            for ((key, value) in m_mapFragments) {

                if(key == tag)
                {
                    nResIdx = nIdx
                    currentTag = key
                    break
                }
                nIdx++
            }
        }
        return nResIdx
    }
}
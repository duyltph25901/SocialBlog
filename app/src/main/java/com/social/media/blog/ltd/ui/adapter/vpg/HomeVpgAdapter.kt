package com.social.media.blog.ltd.ui.adapter.vpg

import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.social.media.blog.ltd.ui.base.BaseFragment

class HomeVpgAdapter(
    fragmentActivity: FragmentActivity,
    private val fragmentList: MutableList<BaseFragment<out ViewDataBinding>>
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}
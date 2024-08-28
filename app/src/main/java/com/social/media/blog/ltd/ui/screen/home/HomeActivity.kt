package com.social.media.blog.ltd.ui.screen.home

import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.goneView
import com.social.media.blog.ltd.commons.extention.visibleView
import com.social.media.blog.ltd.databinding.ActivityHomeBinding
import com.social.media.blog.ltd.ui.adapter.vpg.HomeVpgAdapter
import com.social.media.blog.ltd.ui.base.BaseActivity
import com.social.media.blog.ltd.ui.base.BaseFragment
import com.social.media.blog.ltd.ui.screen.home.frg.AddFragment
import com.social.media.blog.ltd.ui.screen.home.frg.HomeFragment
import com.social.media.blog.ltd.ui.screen.home.frg.SaveFragment
import com.social.media.blog.ltd.ui.screen.home.frg.SettingFragment

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private lateinit var listIconBottomBar: MutableList<ImageView>
    private lateinit var listFragment: MutableList<BaseFragment<out ViewDataBinding>>
    private lateinit var homeVpgAdapter: HomeVpgAdapter

    private val indexHome = 0
    private val indexAdd = 1
    private val indexSave = 2
    private val indexSetting = 3

    override fun inflateViewBinding(): ActivityHomeBinding =
        ActivityHomeBinding.inflate(layoutInflater)

    override fun initVariable() {
        listIconBottomBar = getListIconBottomBar()
        listFragment = getListFragment()
        homeVpgAdapter = HomeVpgAdapter(this, listFragment)
    }

    override fun initView() {
        initDefaultFirstTab()
        initVpg()
    }

    override fun clickViews() = binding.apply {
        buttonHome.click { activeBottomBar(indexHome) }
        buttonAddPost.click { activeBottomBar(indexAdd) }
        buttonSavePost.click { activeBottomBar(indexSave) }
        buttonSetting.click { activeBottomBar(indexSetting) }
    }

    private fun getListIconBottomBar() = mutableListOf(
        binding.icHome, binding.icAddPost, binding.icSavePost, binding.icSetting
    )

    private fun getListFragment() = mutableListOf(
        HomeFragment(), AddFragment(), SaveFragment(), SettingFragment()
    )

    private fun activeBottomBar(index: Int) {
        changeUiBottomBar(index)
        changeIndexVpg(index)
    }

    private fun changeUiBottomBar(index: Int) {
        clearAllIconActive(index)
        activeUiBottom(index)
    }

    private fun clearAllIconActive(index: Int) = listIconBottomBar.forEach { img ->
        img.apply {
            isActivated = false
            setBackgroundResource(0)
        }
    }

    private fun activeUiBottom(index: Int) = listIconBottomBar[index].apply {
        isActivated = true
        setBackgroundResource(R.drawable.bg_ic_home)
    }

    private fun changeIndexVpg(index: Int) {
        binding.vpgMain.currentItem = index
    }

    private fun initDefaultFirstTab() = activeBottomBar(0)

    private fun initVpg() = binding.vpgMain.apply {
        val registerOnPageChangeCallback = object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    activeBottomBar(position)
                }
            }
        adapter = homeVpgAdapter
        currentItem = indexHome
        isUserInputEnabled = false
        offscreenPageLimit = listFragment.size

        registerOnPageChangeCallback(registerOnPageChangeCallback)
        unregisterOnPageChangeCallback(registerOnPageChangeCallback)
    }

}
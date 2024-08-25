package com.social.media.blog.ltd.ui.screen.intro

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.Routes.startLoginActivity
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.goneView
import com.social.media.blog.ltd.commons.extention.visibleView
import com.social.media.blog.ltd.databinding.ActivityIntroBinding
import com.social.media.blog.ltd.ui.adapter.rcv.IntroAdapter
import com.social.media.blog.ltd.ui.base.BaseActivity
import kotlin.math.abs

class IntroActivity : BaseActivity<ActivityIntroBinding>() {

    private lateinit var introAdapter: IntroAdapter
    private lateinit var compositePageTransformer: CompositePageTransformer
    private lateinit var listIndicatorView: MutableList<ImageView>

    private var posViewPager = 0

    private val viewModel: IntroViewModel by viewModels()

    override fun inflateViewBinding(): ActivityIntroBinding =
        ActivityIntroBinding.inflate(layoutInflater)

    override fun initVariable() {
        introAdapter = IntroAdapter()
        compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(100))
            addTransformer { view, position ->
                val r = 1 - abs(position)
                view.scaleY = 0.8f + r * 0.2f
                val absPosition = abs(position)
                view.alpha = 1.0f - (1.0f - 0.3f) * absPosition
            }
        }
        listIndicatorView = mutableListOf<ImageView>().apply {
            add(binding.indicator1)
            add(binding.indicator2)
            add(binding.indicator3)
            add(binding.indicator4)
        }
    }

    override fun initView() = binding.apply {
        vpg2.apply {
            adapter = introAdapter
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 4
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_ALWAYS
            setPageTransformer(compositePageTransformer)
            registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                @SuppressLint("InvalidAnalyticsName", "UseCompatLoadingForDrawables")
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    posViewPager = position
                    viewModel.updateIndicatorView(
                        indexSelected = position,
                        indicators = listIndicatorView
                    )
                    when (position) {
                        0 -> binding.frAds.visibleView()
                        1 -> binding.frAds.goneView()
                        2 -> binding.frAds.goneView()
                        else -> binding.frAds.visibleView()
                    }
                }
            })
        }
    }

    override fun fetchDataSource() = viewModel.apply {
        fetchDataIntro()
    }

    override fun observerDataSource() = viewModel.apply {
        introList.observe(this@IntroActivity) { listIntro ->
            introAdapter.submitData(listIntro)
        }
    }

    override fun clickViews() = binding.apply {
        textNext.click {
            if (vpg2.currentItem < 3) vpg2.currentItem++ else gotoNextScreen()
        }
    }

    private fun gotoNextScreen() {
        startLoginActivity(this)
        finish()
    }
}
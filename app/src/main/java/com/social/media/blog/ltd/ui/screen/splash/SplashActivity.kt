package com.social.media.blog.ltd.ui.screen.splash

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import com.social.media.blog.ltd.commons.AppConst.SPLASH_DELAY_TIME_MS_TYPE
import com.social.media.blog.ltd.commons.Routes.startIntroActivity
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.databinding.ActivitySplashBinding
import com.social.media.blog.ltd.ui.base.BaseActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun inflateViewBinding(): ActivitySplashBinding =
        ActivitySplashBinding.inflate(layoutInflater)

    override fun initView() {
        initShared()
        goToIntroActivity()
    }

    private fun goToIntroActivity() = Handler(Looper.myLooper()!!).postDelayed({
        startIntroActivity(this)
        finish()
    }, SPLASH_DELAY_TIME_MS_TYPE)

    private fun initShared() = SharedUtils.init(this)
}
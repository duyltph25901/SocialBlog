package com.social.media.blog.ltd.commons

import android.app.Activity
import android.content.Intent
import com.social.media.blog.ltd.ui.screen.intro.IntroActivity
import com.social.media.blog.ltd.ui.screen.login.LoginActivity

object Routes {

    fun startIntroActivity(fromActivity: Activity) =
        Intent(fromActivity, IntroActivity::class.java).apply {
            fromActivity.startActivity(this)
        }

    fun startLoginActivity(fromActivity: Activity) =
        Intent(fromActivity, LoginActivity::class.java).apply {
            fromActivity.startActivity(this)
        }

}
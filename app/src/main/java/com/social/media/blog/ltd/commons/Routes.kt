package com.social.media.blog.ltd.commons

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.social.media.blog.ltd.ui.screen.home.HomeActivity
import com.social.media.blog.ltd.ui.screen.intro.IntroActivity
import com.social.media.blog.ltd.ui.screen.login.LoginActivity
import com.social.media.blog.ltd.ui.screen.post.all.AllPostActivity
import com.social.media.blog.ltd.ui.screen.post.detail.PostDetailActivity
import com.social.media.blog.ltd.ui.screen.register.RegisterActivity

object Routes {

    fun startIntroActivity(fromActivity: Activity) =
        Intent(fromActivity, IntroActivity::class.java).apply {
            fromActivity.startActivity(this)
        }

    fun startLoginActivity(fromActivity: Activity) =
        Intent(fromActivity, LoginActivity::class.java).apply {
            fromActivity.startActivity(this)
        }

    fun startRegisterActivity(fromActivity: Activity) =
        Intent(fromActivity, RegisterActivity::class.java).apply {
            fromActivity.startActivity(this)
        }

    fun startHomeActivity(fromActivity: Activity) =
        Intent(fromActivity, HomeActivity::class.java).apply {
            fromActivity.startActivity(this)
        }

    fun startPostDetailActivity(fromActivity: Activity, bundle: Bundle) =
        Intent(fromActivity, PostDetailActivity::class.java).apply {
            putExtras(bundle)
            fromActivity.startActivity(this)
        }

    fun startAllPostActivity(fromActivity: Activity, bundle: Bundle) =
        Intent(fromActivity, AllPostActivity::class.java).apply {
            putExtras(bundle)
            fromActivity.startActivity(this)
        }

}
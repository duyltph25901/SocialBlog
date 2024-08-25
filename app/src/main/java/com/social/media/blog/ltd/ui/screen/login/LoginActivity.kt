package com.social.media.blog.ltd.ui.screen.login

import androidx.activity.viewModels
import com.social.media.blog.ltd.commons.Routes.startRegisterActivity
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.databinding.ActivityLoginBinding
import com.social.media.blog.ltd.ui.base.BaseActivity

class LoginActivity: BaseActivity<ActivityLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun inflateViewBinding(): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)

    override fun initVariable() {

    }

    override fun initView() = binding.apply {

    }

    override fun clickViews() = binding.apply {
        textRegisterAccount.click {
            startRegisterActivity(this@LoginActivity)
        }
    }
}
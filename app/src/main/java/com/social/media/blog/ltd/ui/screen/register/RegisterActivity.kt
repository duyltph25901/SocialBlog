package com.social.media.blog.ltd.ui.screen.register

import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.databinding.ActivityRegisterBinding
import com.social.media.blog.ltd.ui.base.BaseActivity

class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {


    override fun inflateViewBinding(): ActivityRegisterBinding =
        ActivityRegisterBinding.inflate(layoutInflater)

    override fun initView() = binding.apply {

    }

    override fun clickViews() = binding.apply {
        icBack.click { finish() }
    }
}
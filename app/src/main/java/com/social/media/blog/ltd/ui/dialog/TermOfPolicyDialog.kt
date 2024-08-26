package com.social.media.blog.ltd.ui.dialog

import android.content.Context
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.databinding.DialogTermOfPolicyBinding
import com.social.media.blog.ltd.ui.base.BaseDialog

class TermOfPolicyDialog(
    private val context: Context
): BaseDialog<DialogTermOfPolicyBinding>(context) {

    override fun getLayoutDialog(): Int = R.layout.dialog_term_of_policy

    override fun initViews() {
        setCancelable(false)
    }

    override fun onClickViews() {
        mBinding.apply {
            icClose.click { dismiss() }
        }
    }

}
package com.social.media.blog.ltd.ui.dialog

import android.content.Context
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.databinding.DialogChangeModePostBinding
import com.social.media.blog.ltd.ui.base.BaseDialog

class ChangeModePostDialog(
    context: Context,
    private val mode: Boolean,
    private val changeMode: (mode: Boolean) -> Unit
): BaseDialog<DialogChangeModePostBinding>(context) {

    override fun getLayoutDialog(): Int = R.layout.dialog_change_mode_post

    override fun initViews() {
        mBinding.apply {
            if (mode) {
                radioModePublic.isActivated = true
                radioModePrivate.isActivated = false
            } else {
                radioModePublic.isActivated = false
                radioModePrivate.isActivated = true
            }
        }
    }

    override fun onClickViews() {
        mBinding.apply {
            layoutPrivate.click {
                radioModePrivate.isActivated = true
                radioModePublic.isActivated = false

                changeMode.invoke(false)

                dismiss()
            }

            layoutPublic.click {
                radioModePrivate.isActivated = false
                radioModePublic.isActivated = true

                changeMode.invoke(true)

                dismiss()
            }

            iconClose.click { dismiss() }
        }
    }
}
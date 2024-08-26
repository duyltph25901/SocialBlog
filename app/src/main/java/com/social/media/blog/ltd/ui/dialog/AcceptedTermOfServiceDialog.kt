package com.social.media.blog.ltd.ui.dialog

import android.content.Context
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.toastMessageRes
import com.social.media.blog.ltd.databinding.DialogAcceptedTermOfServiceBinding
import com.social.media.blog.ltd.ui.base.BaseDialog

class AcceptedTermOfServiceDialog(
    contextParams: Context,
    var eventClickDone: (() -> Unit)? = null
) : BaseDialog<DialogAcceptedTermOfServiceBinding>(contextParams) {

    override fun getLayoutDialog(): Int = R.layout.dialog_accepted_term_of_service

    override fun initViews() {
        setCancelable(false)
    }

    override fun onClickViews() {
        mBinding.apply {
            icClose.click {
                dismiss()
                icChb.isActivated = false
            }

            icChb.click {
                it?.isActivated = !it!!.isActivated
            }

            buttonAccept.click {
                if (icChb.isActivated) {
                    eventClickDone?.invoke()
                    dismiss()
                } else root.context.toastMessageRes(R.string.please_click_the_agree_button)
            }
        }
    }
}
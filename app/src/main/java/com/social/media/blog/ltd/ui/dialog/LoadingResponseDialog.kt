package com.social.media.blog.ltd.ui.dialog

import android.content.Context
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.goneView
import com.social.media.blog.ltd.commons.extention.visibleView
import com.social.media.blog.ltd.databinding.DialogResponseLoadingBinding
import com.social.media.blog.ltd.ui.base.BaseDialog

class LoadingResponseDialog(
    context: Context,
    private val isShowButtonCancel: Boolean,
    var cancelProgress: (() -> Unit?)? = null
) : BaseDialog<DialogResponseLoadingBinding>(context) {

    override fun getLayoutDialog(): Int = R.layout.dialog_response_loading

    override fun initViews() {
        mBinding.apply {
            setCancelable(false)
            if (isShowButtonCancel) icCloseDialog.visibleView() else icCloseDialog.goneView()
        }
    }

    override fun onClickViews() {
        mBinding.apply {
            icCloseDialog.click {
                cancelProgress?.invoke()
                dismiss()
            }
        }
    }
}
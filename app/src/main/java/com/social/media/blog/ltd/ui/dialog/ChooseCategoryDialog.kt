package com.social.media.blog.ltd.ui.dialog

import android.content.Context
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.convertObjectToJson
import com.social.media.blog.ltd.databinding.DialogChooseCategoryBinding
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.ui.adapter.rcv.CategoryChosenAdapter
import com.social.media.blog.ltd.ui.base.BaseDialog

class ChooseCategoryDialog(
    context: Context,
    private val categoryAdapter: CategoryChosenAdapter
) : BaseDialog<DialogChooseCategoryBinding>(context) {

    override fun getLayoutDialog(): Int = R.layout.dialog_choose_category

    override fun initViews() {
        mBinding.rcvCategory.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            adapter = categoryAdapter
        }
    }

    override fun onClickViews() {
        mBinding.iconClose.click { dismiss() }
    }

    fun refreshDataRcv(category: CategoryModelDTO, newIndex: Int, refreshUIScreen: () -> Unit) {
        changeUiRcvSelected(newIndex)
        cacheCategoryMode(category, newIndex)
        refreshUIScreen.invoke()
        dismiss()
    }

    private fun changeUiRcvSelected(newIndex: Int) {
        val indexSelectedBefore = categoryAdapter.indexSelected
        categoryAdapter.indexSelected = newIndex
        categoryAdapter.notifyItemChanged(indexSelectedBefore)
    }

    private fun cacheCategoryMode(category: CategoryModelDTO, newIndex: Int) {
        val jsonCategory = mBinding.root.context.convertObjectToJson(category)
        SharedUtils.jsonCategory = jsonCategory
        SharedUtils.indexCategory = newIndex
    }

}
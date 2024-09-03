package com.social.media.blog.ltd.ui.adapter.rcv

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.databinding.ItemCategoryChosenBinding
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.ui.base.BaseRecyclerView

class CategoryChosenAdapter(
    private val chosenItem: (category: CategoryModelDTO, index: Int) -> Unit
): BaseRecyclerView<CategoryModelDTO>() {

    var indexSelected: Int = 0
        set(value) {
            field = value
            notifyItemChanged(value)
        }

    override fun getItemLayout(): Int = R.layout.item_category_chosen

    override fun setData(binding: ViewDataBinding, item: CategoryModelDTO, layoutPosition: Int) {
        if (binding is ItemCategoryChosenBinding) {
            Glide.with(binding.root.context).load(item.img.unActive).into(binding.imageCategory)
            binding.textCategoryName.text = item.name
            binding.icRadio.isActivated = isActiveImg(layoutPosition)

            binding.root.click { chosenItem.invoke(item, layoutPosition) }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun submitData(newData: List<CategoryModelDTO>) {
        list.apply {
            clear()
            addAll(newData)
            notifyDataSetChanged()
        }
    }

    private fun isActiveImg(layoutPosition: Int) = layoutPosition == indexSelected
}
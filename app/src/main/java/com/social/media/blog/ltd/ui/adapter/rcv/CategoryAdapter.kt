package com.social.media.blog.ltd.ui.adapter.rcv

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.databinding.ItemCategoryBinding
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.ui.base.BaseRecyclerView

class CategoryAdapter(
    private val selectedCategory: ((category: CategoryModelDTO, index: Int) -> Unit)? = null,
    private val unSelectedCategory: ((category: CategoryModelDTO, index: Int) -> Unit)? = null
) : BaseRecyclerView<CategoryModelDTO>() {

    var itemSelected: Int = -1
        set(value) {
            field = value
            notifyItemChanged(value)
        }

    override fun getItemLayout(): Int = R.layout.item_category

    override fun setData(binding: ViewDataBinding, item: CategoryModelDTO, layoutPosition: Int) {
        if (binding is ItemCategoryBinding) {
            binding.apply {
                Glide.with(root.context).load(
                    if (isItemSelected(layoutPosition)) item.img.active
                    else item.img.unActive
                ).into(imageCategory)
                textCategoryName.text = item.name
            }

            binding.root.click {
                if (isItemSelected(layoutPosition)) unSelectedCategory?.invoke(item, layoutPosition)
                else selectedCategory?.invoke(item, layoutPosition)
            }
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

    private fun isItemSelected(layoutPositionItem: Int) =
        itemSelected == layoutPositionItem
}
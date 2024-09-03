package com.social.media.blog.ltd.ui.adapter.rcv

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.databinding.ItemImageBinding
import com.social.media.blog.ltd.model.domain.ImageModelDomain
import com.social.media.blog.ltd.ui.base.BaseRecyclerView

class PickImageAdapter(
    private val chosenImage: (image: ImageModelDomain, index: Int) -> Unit
) : BaseRecyclerView<ImageModelDomain>() {
    override fun getItemLayout(): Int = R.layout.item_image

    var indexImageSelected: Int = -1
        set(value) {
            field = value
            notifyItemChanged(value)
        }

    override fun setData(binding: ViewDataBinding, item: ImageModelDomain, layoutPosition: Int) {
        if (binding is ItemImageBinding) {
            Glide.with(binding.root.context).load(item.path).into(binding.image)
            binding.layoutContainerImage.isActivated = (layoutPosition == indexImageSelected)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun submitData(newData: List<ImageModelDomain>) {
        list.apply {
            clear()
            addAll(newData)
            notifyDataSetChanged()
        }
    }

    override fun onClickViews(
        binding: ViewDataBinding,
        obj: ImageModelDomain,
        layoutPosition: Int
    ) {
        super.onClickViews(binding, obj, layoutPosition)

        if (binding is ItemImageBinding) {
            binding.root.click { chosenImage.invoke(obj, layoutPosition) }
        }
    }
}
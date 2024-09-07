package com.social.media.blog.ltd.ui.adapter.rcv

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.databinding.ItemPost2Binding
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.ui.base.BaseRecyclerView

class PostAdapterTypeTwo(
    private val showDetailsPost: (post: PostModelDTO, index: Int) -> Unit

): BaseRecyclerView<PostModelDTO>() {
    override fun getItemLayout(): Int = R.layout.item_post_2

    override fun setData(binding: ViewDataBinding, item: PostModelDTO, layoutPosition: Int) {
        if (binding is ItemPost2Binding) {
            binding.apply {
                textTitlePost.text = item.title
                textContentDemo.text = item.content
                Glide.with(getContext(this)).load(item.linkMedia).into(imagePost)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun submitData(newData: List<PostModelDTO>) {
        list.apply {
            clear()
            addAll(newData)
            notifyDataSetChanged()
        }
    }

    override fun onClickViews(binding: ViewDataBinding, obj: PostModelDTO, layoutPosition: Int) {
        if (binding is ItemPost2Binding) {
            binding.root.click {
                showDetailsPost.invoke(obj, layoutPosition)
            }
        }
    }

    private fun getContext(binding: ItemPost2Binding) = binding.root.context
}
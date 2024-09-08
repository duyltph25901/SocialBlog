package com.social.media.blog.ltd.ui.adapter.rcv

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.databinding.ItemPost2Binding
import com.social.media.blog.ltd.model.entities.PostSaveEntity
import com.social.media.blog.ltd.ui.base.BaseRecyclerView

class PostSaveAdapter(
    private var showDetailPost: (postSaveEntity: PostSaveEntity, indexedValue: Int) -> Unit
): BaseRecyclerView<PostSaveEntity>() {

    override fun getItemLayout(): Int = R.layout.item_post_2

    override fun setData(binding: ViewDataBinding, item: PostSaveEntity, layoutPosition: Int) {
        if (binding is ItemPost2Binding) {
            Glide.with(binding.root.context).load(item.linkMedia).into(binding.imagePost)
            binding.textTitlePost.text = item.title
            binding.textContentDemo.text = item.content
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun submitData(newData: List<PostSaveEntity>) {
        list.apply {
            clear()
            addAll(newData)
            notifyDataSetChanged()
        }
    }

    override fun onClickViews(binding: ViewDataBinding, obj: PostSaveEntity, layoutPosition: Int) {
        if (binding is ItemPost2Binding) {
            binding.root.click {
                showDetailPost.invoke(obj, layoutPosition)
            }
        }
    }
}
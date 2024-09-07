package com.social.media.blog.ltd.ui.adapter.rcv

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.formatCurrentTimeTo_hh_mm_dd_MM_yyyy
import com.social.media.blog.ltd.databinding.ItemCommentBinding
import com.social.media.blog.ltd.model.domain.PostModelDomain
import com.social.media.blog.ltd.ui.base.BaseRecyclerView

class CommentAdapter : BaseRecyclerView<PostModelDomain.CommentModelDomain>() {
    override fun getItemLayout(): Int = R.layout.item_comment

    override fun setData(
        binding: ViewDataBinding,
        item: PostModelDomain.CommentModelDomain,
        layoutPosition: Int
    ) {
        if (binding is ItemCommentBinding) {
            binding.apply {
                Glide.with(binding.root.context).load(item.avatarUser).into(avatarUser)
                textUserName.text = item.userName
                textComment.text = item.comment.comment
                textDateTime.text =
                    getDateTimeByFormatAlreadyExist(item.comment.commentedAt, root.context)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun submitData(newData: List<PostModelDomain.CommentModelDomain>) {
        list.apply {
            clear()
            addAll(newData)
            notifyDataSetChanged()
        }
    }

    private fun getDateTimeByFormatAlreadyExist(current: Long, context: Context) =
        context.formatCurrentTimeTo_hh_mm_dd_MM_yyyy(current)

    fun addToFirstListComment(item: PostModelDomain.CommentModelDomain) {
        list.add(0, item)
        notifyItemInserted(0)
    }
}
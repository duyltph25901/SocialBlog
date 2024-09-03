package com.social.media.blog.ltd.ui.adapter.rcv

import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.convertJsonToObject
import com.social.media.blog.ltd.databinding.ItemPost1Binding
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.model.dto.UserModelDTO
import com.social.media.blog.ltd.ui.base.BaseRecyclerView

class PostAdapterTypeOne: BaseRecyclerView<PostModelDTO>() {
    override fun getItemLayout(): Int = R.layout.item_post_1

    override fun setData(binding: ViewDataBinding, item: PostModelDTO, layoutPosition: Int) {
        if (binding is ItemPost1Binding) {
            binding.apply {
                textTitle.text = item.title
                textContent.text = item.content
                textViewAllComments.text = getTextViewAllComments(item, this)
                Glide.with(getContext(this)).load(item.linkMedia).into(imageViewPost)
                icFavorite.isActivated = isFavorite(item, binding)
            }
        }
    }

    override fun submitData(newData: List<PostModelDTO>) {
        list.apply {
            clear()
            addAll(newData)
            notifyDataSetChanged()
        }
    }

    override fun onClickViews(binding: ViewDataBinding, obj: PostModelDTO, layoutPosition: Int) {
        if (binding is ItemPost1Binding) {
            binding.root.click {

            }

            binding.icFavorite.click {

            }

            binding.icComment.click {

            }

            binding.icShare.click {  }

            binding.icon.click {  }

            binding.textAddAComment.click {  }

            binding.textSend.click {  }
        }
    }

    private fun getTextViewAllComments(post: PostModelDTO, binding: ItemPost1Binding) =
        getContext(binding).getString(R.string.view_all_comments, post.listComments.size)

    private fun getContext(binding: ItemPost1Binding) = binding.root.context

    private fun isFavorite(post: PostModelDTO, binding: ItemPost1Binding): Boolean {
        val jsonAccountCurrent = SharedUtils.jsonUserCache
        val userCurrent = convertJsonToUserModelDto(jsonAccountCurrent, binding)


        return post.listIdUserLiked.any { idUser -> idUser == userCurrent.idUser }
    }

    private fun convertJsonToUserModelDto(json: String, binding: ItemPost1Binding) =
        getContext(binding).convertJsonToObject(json, UserModelDTO::class.java)
}
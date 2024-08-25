package com.social.media.blog.ltd.ui.adapter.rcv

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.databinding.ItemOnBoardingBinding
import com.social.media.blog.ltd.model.domain.IntroModelDomain
import com.social.media.blog.ltd.ui.base.BaseRecyclerView

class IntroAdapter : BaseRecyclerView<IntroModelDomain>() {
    override fun getItemLayout(): Int = R.layout.item_on_boarding

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun setData(binding: ViewDataBinding, item: IntroModelDomain, layoutPosition: Int) {
        if (binding is ItemOnBoardingBinding) {
            binding.tvTitle.text = context?.getString(item.titleResource)
            context?.let {
                Glide.with(it).load(it.getDrawable(item.imgDrawableResource))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean = false

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean = false
                    }).into(binding.imgGuide)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun submitData(newData: List<IntroModelDomain>) {
        list.clear()
        list.addAll(newData)
        notifyDataSetChanged()
    }
}
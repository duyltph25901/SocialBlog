package com.social.media.blog.ltd.ui.screen.images

import android.app.Activity
import android.content.Intent
import androidx.activity.viewModels
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.AppConst.KEY_INDEX_SELECTED_IMAGE
import com.social.media.blog.ltd.commons.AppConst.KEY_RESULT_PICK_IMAGE
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.convertObjectToJson
import com.social.media.blog.ltd.databinding.ActivityPickImageBinding
import com.social.media.blog.ltd.model.domain.ImageModelDomain
import com.social.media.blog.ltd.ui.adapter.rcv.PickImageAdapter
import com.social.media.blog.ltd.ui.base.BaseActivity

class PickImageActivity : BaseActivity<ActivityPickImageBinding>() {

    private val viewModel: PickImageViewModel by viewModels()

    private lateinit var imageAdapter: PickImageAdapter

    private var indexImageSelectedBefore = -1

    override fun inflateViewBinding(): ActivityPickImageBinding =
        ActivityPickImageBinding.inflate(layoutInflater)

    override fun initVariable() {
        indexImageSelectedBefore = intent.getIntExtra(KEY_INDEX_SELECTED_IMAGE, -1)

        initImageAdapter()
    }

    override fun initView() = binding.apply {
        rcvImage.apply {
            adapter = imageAdapter
        }
    }

    override fun fetchDataSource() = viewModel.apply {
        getAllImageFromDevice(binding.root.context)
    }

    override fun observerDataSource() = viewModel.apply {
        images.observe(this@PickImageActivity) { listImages ->
            submitDataAdapter(listImages)
        }
    }

    override fun clickViews() = binding.apply {
        icBack.click { finish() }
    }

    private fun initImageAdapter() {
        imageAdapter = PickImageAdapter { image, index -> chooseImageFromDevice(image, index) }.apply {
            indexImageSelected = indexImageSelectedBefore
        }
    }

    private fun submitDataAdapter(newData: MutableList<ImageModelDomain>) =
        imageAdapter.submitData(newData)

    private fun chooseImageFromDevice(image: ImageModelDomain, index: Int) {
        changeUiAdapter(index)
        sendDataToScreenBefore(image, index)
    }

    private fun changeUiAdapter(index: Int) {
        val indexSelectedBefore = imageAdapter.indexImageSelected
        imageAdapter.indexImageSelected = index
        if (isUserSelectedImageFromDevice(indexSelectedBefore)) imageAdapter.notifyItemChanged(
            indexSelectedBefore
        )
    }

    private fun isUserSelectedImageFromDevice(index: Int) = index != -1

    private fun sendDataToScreenBefore(image: ImageModelDomain, index: Int) {
        val valueReturnJsonType = convertObjectToJson(image)
        val returnIntent = Intent().apply {
            putExtra(KEY_RESULT_PICK_IMAGE, valueReturnJsonType)
            putExtra(KEY_INDEX_SELECTED_IMAGE, index)
        }
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

}
package com.social.media.blog.ltd.ui.screen.crop_img

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageView
import com.social.media.blog.ltd.commons.AppConst.KEY_IMAGE_PATH_AFTER_CROP
import com.social.media.blog.ltd.commons.AppConst.KEY_IMAGE_URI_TO_CROP
import com.social.media.blog.ltd.commons.AppConst.KEY_PATH_IMAGE_TO_CROP
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.databinding.ActivityCropImageBinding
import com.social.media.blog.ltd.ui.base.BaseActivity
import com.social.media.blog.ltd.ui.dialog.LoadingResponseDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class CropImageActivity : BaseActivity<ActivityCropImageBinding>() {

    private val viewModel: CropImageViewModel by viewModels()

    private lateinit var dialogLoading: LoadingResponseDialog

    override fun inflateViewBinding(): ActivityCropImageBinding =
        ActivityCropImageBinding.inflate(layoutInflater)

    override fun fetchDataSource() {
        val pathImagePicked = intent.getStringExtra(KEY_PATH_IMAGE_TO_CROP) ?: ""
        val uriStringType = intent.getStringExtra(KEY_IMAGE_URI_TO_CROP)

        refreshDataPathImagePicked(pathImagePicked)
        refreshDataUriTakenPhoto(
            uri = if (isUserTakenPhoto(uriStringType)) Uri.parse(uriStringType) else null
        )
    }

    override fun observerDataSource() = viewModel.apply {
        pathImagePicked.observe(this@CropImageActivity) { pathImage ->
            if (pathImage.isNotEmpty()) {
                val bitmapAfterConvert = convertPathImageToBitmapType(pathImage)
                binding.cropImageView.setImageBitmap(bitmapAfterConvert)
            }
        }

        uriTakenPhoto.observe(this@CropImageActivity) { uri ->
            if (uri != null) {
                binding.cropImageView.setImageUriAsync(uri)
            }
        }

        isLoadingReturnValue.observe(this@CropImageActivity) { loading ->
            if (loading) dialogLoading.show() else dialogLoading.cancel()
        }
    }

    override fun initView() {
        initCropImageView()
        initDialogLoading()
    }

    override fun clickViews() = binding.apply {
        icBack.click { finish() }

        icDone.click { handleDone() }
    }

    private fun isUserTakenPhoto(uriStrType: String?) =
        viewModel.isUserTakenPhoto(uriStrType)

    private fun refreshDataPathImagePicked(path: String) =
        viewModel.refreshDataPathImagePicked(path)

    private fun refreshDataUriTakenPhoto(uri: Uri?) =
        viewModel.refreshDataUriTakenPhoto(uri)

    private fun initCropImageView() = binding.cropImageView.apply {
        cropShape = CropImageView.CropShape.RECTANGLE
    }

    private fun handleDone() {
        lifecycleScope.launch(Dispatchers.IO) {
            showDialogLoading()
            val bitmapAfterCrop = getImageBitmapAfterCrop()
            val pathImageAfterSave =
                viewModel.saveImageToFile(this@CropImageActivity, bitmapAfterCrop!!)
            val valueReturnByteArrayType = convertBitmapToByteArray(bitmapAfterCrop)

            withContext(Dispatchers.Main) {
                delay(2000)
                if (!isValueReturnNull(valueReturnByteArrayType)) {
                    val intentReturn = Intent().apply {
                        putExtra(KEY_IMAGE_PATH_AFTER_CROP, pathImageAfterSave)
                    }
                    setResult(Activity.RESULT_OK, intentReturn)
                }

                hideDialogLoading()
                finish()
            }
        }
    }

    private fun getImageBitmapAfterCrop() = binding.cropImageView.croppedImage

    private fun convertBitmapToByteArray(bitmapInput: Bitmap?): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmapInput?.compress(Bitmap.CompressFormat.WEBP, 50, stream)

        return if (bitmapInput == null) null else stream.toByteArray()
    }

    private fun isValueReturnNull(value: ByteArray?) = value == null

    private fun initDialogLoading() {
        dialogLoading = LoadingResponseDialog(this, false)
    }

    private fun showDialogLoading() = viewModel.showLoadingReturnValue()

    private fun hideDialogLoading() = viewModel.hideLoadingReturnValue()
}
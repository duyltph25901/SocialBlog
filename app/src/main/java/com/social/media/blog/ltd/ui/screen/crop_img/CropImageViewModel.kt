package com.social.media.blog.ltd.ui.screen.crop_img

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class CropImageViewModel : ViewModel() {

    private val _pathImagePicked = MutableLiveData<String>()
    private val _uriTakenPhoto = MutableLiveData<Uri?>()
    private val _isLoadingReturnValue = MutableLiveData<Boolean>()

    val pathImagePicked: LiveData<String> = _pathImagePicked
    val uriTakenPhoto: LiveData<Uri?> = _uriTakenPhoto
    val isLoadingReturnValue = _isLoadingReturnValue

    fun refreshDataPathImagePicked(path: String) = _pathImagePicked.postValue(path)

    fun refreshDataUriTakenPhoto(uri: Uri?) = _uriTakenPhoto.postValue(uri)

    fun convertPathImageToBitmapType(path: String): Bitmap =
        BitmapFactory.decodeFile(path)

    fun isUserTakenPhoto(uriStrType: String?) = uriStrType != null

    suspend fun saveImageToFile(context: Context, bitmap: Bitmap): String =
        viewModelScope.async(Dispatchers.IO) {
            val imageFile: File = File(context.cacheDir, "socialBlog_${System.currentTimeMillis()}_crop_photos.png")
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()

            return@async imageFile.absolutePath
        }.await()

    fun showLoadingReturnValue() = _isLoadingReturnValue.postValue(true)

    fun hideLoadingReturnValue() = _isLoadingReturnValue.postValue(false)
}
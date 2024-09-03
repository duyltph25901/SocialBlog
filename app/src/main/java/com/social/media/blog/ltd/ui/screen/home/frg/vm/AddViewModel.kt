package com.social.media.blog.ltd.ui.screen.home.frg.vm

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.social.media.blog.ltd.commons.AppConst.categoryRef
import com.social.media.blog.ltd.commons.AppConst.postRef
import com.social.media.blog.ltd.commons.AppConst.postStorageRef
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.commons.extention.convertJsonToObject
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.model.dto.UserModelDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AddViewModel : ViewModel() {

    private val _userAccountCurrent = MutableLiveData<UserModelDTO>()
    private val _modePost = MutableLiveData<Boolean>()
    private val _categoryMode = MutableLiveData<CategoryModelDTO>()
    private val _categories = MutableLiveData<MutableList<CategoryModelDTO>>()
    private val _pathImageSelected = MutableLiveData<String>()
    private val _bitmapImageAfterCrop = MutableLiveData<Bitmap?>()
    private val _isLoadingUploadPost = MutableLiveData<Boolean>()
    private val _resultAfterUploadPost = MutableLiveData<Boolean>()

    val userAccountCurrent: LiveData<UserModelDTO> = _userAccountCurrent
    val modePost: LiveData<Boolean> = _modePost
    val categoryMode: LiveData<CategoryModelDTO> = _categoryMode
    val categories: LiveData<MutableList<CategoryModelDTO>> = _categories
    val pathImageSelected: LiveData<String> = _pathImageSelected
    val bitmapImageAfterCrop: LiveData<Bitmap?> = _bitmapImageAfterCrop
    val isLoadingUploadPost: LiveData<Boolean> = _isLoadingUploadPost
    val resultAfterUploadPost: LiveData<Boolean> = _resultAfterUploadPost

    private val onEventListenerCategories: ValueEventListener

    init {
        onEventListenerCategories = createCategoriesEventListener()
    }

    fun initModePost() {
        val modePost = getModePost()
        postValueModePost(modePost)
    }

    fun initCategoryPost(context: Context) {
        val jsonCategoryPost = getJsonCategoryPost()
        val categoryPost = convertJsonToCategoryModelDto(jsonCategoryPost, context)
        postValueCategoryMode(categoryPost)
    }

    fun initUserAccountCurrent(context: Context) {
        val jsonAccountCurrent = getJsonAccountCurrent()
        val userAccountCurrent = convertJsonToUserModelDto(jsonAccountCurrent, context)
        postValueUserAccountCurrent(userAccountCurrent)
    }

    fun refreshModePost(value: Boolean) = _modePost.postValue(value)

    fun fetchDataCategories() = viewModelScope.launch(Dispatchers.IO) {
        categoryRef.addValueEventListener(onEventListenerCategories)
    }

    private fun createCategoriesEventListener() = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val listCategories = getListCategoriesFromSnapShot(snapshot)
            _categories.postValue(listCategories)
        }

        override fun onCancelled(error: DatabaseError) = postEmptyListCategory()

    }

    private fun postEmptyListCategory() = _categories.postValue(mutableListOf())

    private fun getListCategoriesFromSnapShot(dataSnapshot: DataSnapshot): MutableList<CategoryModelDTO> {
        val listCategories = mutableListOf<CategoryModelDTO>()
        for (categorySnapShot in dataSnapshot.children) {
            val categoryModelDTO = categorySnapShot.getValue(CategoryModelDTO::class.java)
            if (categoryModelDTO != null) {
                listCategories.add(categoryModelDTO)
            }
        }
        return listCategories
    }

    private fun getModePost() = SharedUtils.modePost

    private fun postValueModePost(mode: Boolean) = _modePost.postValue(mode)

    private fun getJsonCategoryPost() = SharedUtils.jsonCategory

    private fun convertJsonToCategoryModelDto(json: String, context: Context) =
        context.convertJsonToObject(json, CategoryModelDTO::class.java)

    private fun postValueCategoryMode(category: CategoryModelDTO) =
        _categoryMode.postValue(category)

    private fun getJsonAccountCurrent() = SharedUtils.jsonUserCache

    private fun convertJsonToUserModelDto(json: String, context: Context) =
        context.convertJsonToObject(json, UserModelDTO::class.java)

    private fun postValueUserAccountCurrent(user: UserModelDTO) =
        _userAccountCurrent.postValue(user)

    fun refreshCategoryMode(category: CategoryModelDTO) =
        _categoryMode.postValue(category)

    fun refreshPathImageSelected(path: String) = _pathImageSelected.postValue(path)

    fun refreshBitmapImageAfterCrop(bitmap: Bitmap?) = _bitmapImageAfterCrop.postValue(bitmap)

    fun getValueBitmapAfterCrop() = _bitmapImageAfterCrop.value

    fun handleUploadPost(post: PostModelDTO, bitmapImage: Bitmap) =
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingUploadPost.postValue(true)
            val byteArrayImage = getByteArrayFromBitmap(bitmapImage)
            val idAuthor = post.idAuthor
            val result = handleUploadImageAndReturnLinkHttp(
                idAuthor = idAuthor,
                byteArrayImage = byteArrayImage
            )
            post.apply {
                mediaLocation = result.first
                linkMedia = result.second
            }
            val resultAfterPush = handlePushPost(post)
            _resultAfterUploadPost.postValue(resultAfterPush)
            _isLoadingUploadPost.postValue(false)
        }

    private fun getByteArrayFromBitmap(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        return data
    }

    private suspend fun handleUploadImageAndReturnLinkHttp(
        idAuthor: String,
        byteArrayImage: ByteArray
    ): Pair<String, String> = suspendCoroutine { continuation ->
        val imagesRef =
            postStorageRef.child("$idAuthor/${System.currentTimeMillis()}_$idAuthor.webp")
        val uploadTask = imagesRef.putBytes(byteArrayImage)
        uploadTask.addOnSuccessListener { taskSnapShot ->

            val storageReference = taskSnapShot.metadata?.reference
            val gsUri = storageReference?.toString()
            Log.d("Firebase", "GS URL: $gsUri")

            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                continuation.resume(Pair(gsUri!!, uri.toString()))
            }.addOnFailureListener { e ->
                Log.e("error", e.message.toString())
                continuation.resume(Pair("", ""))
            }
        }.addOnFailureListener { exception ->
            Log.e("error", exception.message.toString())
            continuation.resume(Pair("", ""))
        }
    }

    private suspend fun handlePushPost(post: PostModelDTO): Boolean =
        suspendCoroutine { continuation ->
            postRef.child(post.id).setValue(post)
                .addOnSuccessListener { continuation.resume(true) }
                .addOnFailureListener { continuation.resume(false) }
        }
}
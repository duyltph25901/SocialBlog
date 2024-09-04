package com.social.media.blog.ltd.ui.screen.home.frg.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.social.media.blog.ltd.commons.AppConst.categoryRef
import com.social.media.blog.ltd.commons.AppConst.postRef
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.commons.extention.convertJsonToObject
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.model.dto.UserModelDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HomeViewModel : ViewModel() {

    companion object {
        internal const val FLAG_REQUEST_API_TRUE = 1
        internal const val FLAG_REQUEST_API_FAIL = 0
    }

    private val _isLoadingCategory = MutableLiveData<Boolean>()
    private val _isLoadingPostOne = MutableLiveData<Boolean>()
    private val _isLoadingResponse = MutableLiveData<Boolean>()
    private val _categories = MutableLiveData<MutableList<CategoryModelDTO>>()
    private val _posts = MutableLiveData<MutableList<PostModelDTO>>()

    val isLoadingCategory: LiveData<Boolean> = _isLoadingCategory
    val isLoadingPostOne: LiveData<Boolean> = _isLoadingPostOne
    val isLoadingResponse: LiveData<Boolean> = _isLoadingResponse
    val categories: LiveData<MutableList<CategoryModelDTO>> = _categories
    val posts: LiveData<MutableList<PostModelDTO>> = _posts

    private val onEventListenerCategories: ValueEventListener
    private val onEventListenerPost: ValueEventListener

    init {
        onEventListenerCategories = createCategoriesEventListener()
        onEventListenerPost = createPostEventListener()
    }

    fun fetchCategories() = viewModelScope.launch(Dispatchers.IO) {
        _isLoadingCategory.postValue(true)
        categoryRef.addValueEventListener(onEventListenerCategories)
        _isLoadingCategory.postValue(false)
    }

    fun removeEventListenerCategories() = categoryRef.removeEventListener(onEventListenerCategories)

    fun removeEventListenerPosts() = postRef.removeEventListener(onEventListenerPost)

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

    private fun createPostEventListener() = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val listPosts = getListPostsFromSnapShot(snapshot)
            _posts.postValue(listPosts)
        }

        override fun onCancelled(error: DatabaseError) = postEmptyListPost()
    }

    private fun postEmptyListPost() = _posts.postValue(mutableListOf())

    private fun getListPostsFromSnapShot(snapshot: DataSnapshot): MutableList<PostModelDTO> {
        val listPosts = mutableListOf<PostModelDTO>()
        for (categorySnapShot in snapshot.children) {
            val postModelDTO = categorySnapShot.getValue(PostModelDTO::class.java)
            if (postModelDTO != null) {
                listPosts.add(postModelDTO)
            }
        }

        return listPosts
    }

    fun fetchPosts() = viewModelScope.launch(Dispatchers.IO) {
        _isLoadingPostOne.postValue(true)
        postRef.addValueEventListener(onEventListenerPost)
        _isLoadingPostOne.postValue(false)
    }

    fun handleLikePost(post: PostModelDTO, context: Context) =
        viewModelScope.async(Dispatchers.IO) {
            postValueShowDialogLoading()
            val jsonCurrentUser = getJsonUserCache()
            val currentUser = convertJsonToObject(jsonCurrentUser, UserModelDTO::class.java, context)
            val idCurrentUser = getIdUser(currentUser)
            post.listIdUserLiked = updateListUserLiked(post, idCurrentUser)
            // update real time
            val responseAfterUpdate = updatePostAndReturnFlagUpdate(post)
            postValueHideDialogLoading()
            return@async responseAfterUpdate
        }

    private fun isUserLikedPostBefore(post: PostModelDTO, idUser: String) =
        post.listIdUserLiked.contains(idUser)

    private suspend fun updatePostAndReturnFlagUpdate(post: PostModelDTO) =
        suspendCoroutine { continuation ->
            postRef.child(post.id).setValue(post)
                .addOnSuccessListener { continuation.resume(FLAG_REQUEST_API_TRUE) }
                .addOnFailureListener { continuation.resume(FLAG_REQUEST_API_FAIL) }
        }

    private fun <T> convertJsonToObject(json: String, classOfT: Class<T>, context: Context): T =
        context.convertJsonToObject(json, classOfT)

    private fun getIdUser(user: UserModelDTO) = user.idUser

    private fun getJsonUserCache() = SharedUtils.jsonUserCache

    private fun postValueShowDialogLoading() = _isLoadingResponse.postValue(true)

    private fun postValueHideDialogLoading() = _isLoadingResponse.postValue(false)

    private fun updateListUserLiked(post: PostModelDTO, idUser: String) =
        post.listIdUserLiked.apply {
            if (isUserLikedPostBefore(post, idUser)) remove(idUser)
            else add(idUser)
        }

    fun handleAddComment(post: PostModelDTO, comment: String, context: Context) =
        viewModelScope.async(Dispatchers.IO) {
            postValueShowDialogLoading()
            val jsonCurrentUser = getJsonUserCache()
            val currentUser = convertJsonToObject(jsonCurrentUser, UserModelDTO::class.java, context)
            val idCurrentUser = getIdUser(currentUser)
            post.listComments = updateListUserComment(post, comment, idCurrentUser)
            // update real time
            val responseAfterUpdate = updatePostAndReturnFlagUpdate(post)
            postValueHideDialogLoading()
            return@async responseAfterUpdate
        }

    private fun updateListUserComment(post: PostModelDTO, comment: String, idUser: String) =
        post.listComments.apply {
            add(
                PostModelDTO.Comment(
                idUser = idUser,
                comment = comment,
            ))
        }

    fun updateViewPostPlusOne(post: PostModelDTO) =
        viewModelScope.async(Dispatchers.IO) {
            postValueShowDialogLoading()
            post.views += 1
            // update real time
            val responseAfterUpdate = updatePostAndReturnFlagUpdate(post)
            postValueHideDialogLoading()
            return@async responseAfterUpdate
        }
}
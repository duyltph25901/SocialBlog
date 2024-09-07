package com.social.media.blog.ltd.ui.screen.post.all

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.social.media.blog.ltd.commons.AppConst.FLAG_REQUEST_API_FAIL
import com.social.media.blog.ltd.commons.AppConst.FLAG_REQUEST_API_TRUE
import com.social.media.blog.ltd.commons.AppConst.postRef
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.commons.extention.convertJsonToObject
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.model.dto.UserModelDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AllPostViewModel : ViewModel() {

    private val _listPost = MutableLiveData<MutableList<PostModelDTO>>()
    private val _isLoading = MutableLiveData<Boolean>()

    val listPost: LiveData<MutableList<PostModelDTO>> = _listPost
    val isLoading: LiveData<Boolean> = _isLoading

    private val onEventListenerPost: ValueEventListener

    init {
        onEventListenerPost = createPostEventListener()
    }

    fun fetchAllPostFromApi() = viewModelScope.launch(Dispatchers.IO) {
        showLoading()
        postRef.addValueEventListener(onEventListenerPost)
        hideLoading()
    }

    fun removeEventListenerPosts() = postRef.removeEventListener(onEventListenerPost)

    private fun createPostEventListener() = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val listPosts = getListPostsFromSnapShot(snapshot)
            _listPost.postValue(
                listPosts.sortedByDescending { it.createdAt }.toMutableList()
            )
        }

        override fun onCancelled(error: DatabaseError) = postEmptyListPost()
    }

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

    private fun postEmptyListPost() = _listPost.postValue(mutableListOf())

    private fun showLoading() = _isLoading.postValue(true)

    private fun hideLoading() = _isLoading.postValue(false)

    fun updateViewPostPlusOne(post: PostModelDTO) =
        viewModelScope.async(Dispatchers.IO) {
            showLoading()
            post.views += 1
            // update real time
            val responseAfterUpdate = updatePostAndReturnFlagUpdate(post)
            hideLoading()
            return@async responseAfterUpdate
        }

    private suspend fun updatePostAndReturnFlagUpdate(post: PostModelDTO) =
        suspendCoroutine { continuation ->
            postRef.child(post.id).setValue(post)
                .addOnSuccessListener { continuation.resume(FLAG_REQUEST_API_TRUE) }
                .addOnFailureListener { continuation.resume(FLAG_REQUEST_API_FAIL) }
        }

    fun handleAddComment(post: PostModelDTO, comment: String, context: Context) =
        viewModelScope.async(Dispatchers.IO) {
            showLoading()
            val jsonCurrentUser = getJsonUserCache()
            val currentUser =
                convertJsonToObject(jsonCurrentUser, UserModelDTO::class.java, context)
            val idCurrentUser = getIdUser(currentUser)
            post.listComments = updateListUserComment(post, comment, idCurrentUser)
            // update real time
            val responseAfterUpdate = updatePostAndReturnFlagUpdate(post)
            hideLoading()
            return@async responseAfterUpdate
        }

    fun handleLikePost(post: PostModelDTO, context: Context) =
        viewModelScope.async(Dispatchers.IO) {
            showLoading()
            val jsonCurrentUser = getJsonUserCache()
            val currentUser =
                convertJsonToObject(jsonCurrentUser, UserModelDTO::class.java, context)
            val idCurrentUser = getIdUser(currentUser)
            post.listIdUserLiked = updateListUserLiked(post, idCurrentUser)
            // update real time
            val responseAfterUpdate = updatePostAndReturnFlagUpdate(post)
            hideLoading()
            return@async responseAfterUpdate
        }

    private fun getJsonUserCache() = SharedUtils.jsonUserCache

    private fun <T> convertJsonToObject(json: String, classOfT: Class<T>, context: Context): T =
        context.convertJsonToObject(json, classOfT)

    private fun getIdUser(user: UserModelDTO) = user.idUser

    private fun updateListUserComment(post: PostModelDTO, comment: String, idUser: String) =
        post.listComments.apply {
            add(
                PostModelDTO.Comment(
                    idUser = idUser,
                    comment = comment,
                )
            )
        }

    private fun updateListUserLiked(post: PostModelDTO, idUser: String) =
        post.listIdUserLiked.apply {
            if (isUserLikedPostBefore(post, idUser)) remove(idUser)
            else add(idUser)
        }

    private fun isUserLikedPostBefore(post: PostModelDTO, idUser: String) =
        post.listIdUserLiked.contains(idUser)
}
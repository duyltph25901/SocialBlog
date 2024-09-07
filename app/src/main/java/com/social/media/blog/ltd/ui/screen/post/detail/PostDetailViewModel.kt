package com.social.media.blog.ltd.ui.screen.post.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.social.media.blog.ltd.commons.AppConst.FLAG_REQUEST_API_FAIL
import com.social.media.blog.ltd.commons.AppConst.FLAG_REQUEST_API_TRUE
import com.social.media.blog.ltd.commons.AppConst.KEY_SHOW_POST_DETAIL
import com.social.media.blog.ltd.commons.AppConst.postRef
import com.social.media.blog.ltd.commons.AppConst.userRef
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.commons.extention.convertJsonToObject
import com.social.media.blog.ltd.model.domain.PostModelDomain
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.model.dto.UserModelDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PostDetailViewModel : ViewModel() {
    private val _postModelDomain = MutableLiveData<PostModelDomain>()
    private val _isLoading = MutableLiveData<Boolean>()

    val postModelDomain: LiveData<PostModelDomain> = _postModelDomain
    val isLoading: LiveData<Boolean> = _isLoading

    fun getValuePostModelDomain(intent: Intent) = viewModelScope.launch(Dispatchers.IO) {
        showLoading()
        val bundle = intent.extras
        bundle?.let { bundleNotNull ->
            val postModelDtoTemp =
                bundleNotNull.getSerializable(KEY_SHOW_POST_DETAIL) as PostModelDTO
            val def = getListCommentModelDomain(postModelDtoTemp)
            val rs = def.await()
            val listComments = rs.first
            val author = rs.second
            def.cancel()
            val postModelDomainTemp = PostModelDomain(
                post = postModelDtoTemp,
                listComment = listComments,
                author = author
            )
            _postModelDomain.postValue(postModelDomainTemp)
        }
        hideLoading()
        cancel()
    }

    private suspend fun getListCommentModelDomain(post: PostModelDTO) =
        viewModelScope.async(Dispatchers.IO) {
            val listComments =
                mutableListOf<PostModelDomain.CommentModelDomain>()
            val listUser = fetchUsersDto()
            val author = listUser.find { it.idUser == post.idAuthor } ?: UserModelDTO()
            for (i in post.listComments.indices) {
                for (j in listUser.indices) {
                    val comment = post.listComments[i]
                    val user = listUser[j]
                    if (comment.idUser == user.idUser) {
                        val commentDomain = PostModelDomain.CommentModelDomain(
                            comment = comment,
                            userName = user.userName,
                            avatarUser = user.avatar,
                        )
                        listComments.add(commentDomain)
                    }
                }
            }
            return@async Pair(listComments.sortedByDescending { it.comment.commentedAt }.toMutableList(), author)
        }

    private suspend fun fetchUsersDto(): MutableList<UserModelDTO> =
        suspendCoroutine { continuation ->
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val listUsersModelDto = getListUserFromSnapShot(dataSnapshot)

                    continuation.resume(listUsersModelDto)
                }

                override fun onCancelled(error: DatabaseError) =
                    continuation.resume(mutableListOf())
            })
        }

    private fun getListUserFromSnapShot(dataSnapshot: DataSnapshot): MutableList<UserModelDTO> {
        val listUsersModelDto = mutableListOf<UserModelDTO>()
        for (userSnapShot in dataSnapshot.children) {
            val userModelDto = userSnapShot.getValue(UserModelDTO::class.java)
            if (userModelDto != null) {
                listUsersModelDto.add(userModelDto)
            }
        }

        return listUsersModelDto
    }

    fun showLoading() = _isLoading.postValue(true)

    fun hideLoading() = _isLoading.postValue(false)

    fun getJsonUserCurrent() = SharedUtils.jsonUserCache

    fun convertJsonToUserModelDto(json: String, context: Context) =
        context.convertJsonToObject(json, UserModelDTO::class.java)

    suspend fun updateDatabaseReturnResponse(post: PostModelDTO) =
        suspendCoroutine { continuation ->
            // update real time
            postRef.child(post.id).setValue(post)
                .addOnSuccessListener { continuation.resume(FLAG_REQUEST_API_TRUE) }
                .addOnFailureListener { continuation.resume(FLAG_REQUEST_API_FAIL) }
        }

    fun handleAddComment(post: PostModelDTO, comment: String, context: Context) =
        viewModelScope.async(Dispatchers.IO) {
            showLoading()
            val jsonCurrentUser = getJsonUserCurrent()
            val currentUser = convertJsonToUserModelDto(jsonCurrentUser, context)
            val idCurrentUser = getIdUser(currentUser)
            post.listComments = updateListUserComment(post, comment, idCurrentUser)
            // update real time
            val responseAfterUpdate = updatePostAndReturnFlagUpdate(post)
            hideLoading()
            return@async responseAfterUpdate
        }

    private fun getIdUser(user: UserModelDTO) = user.idUser

    private fun updateListUserComment(post: PostModelDTO, comment: String, idUser: String) =
        post.listComments.apply {
            add(PostModelDTO.Comment(idUser, comment))
        }

    private suspend fun updatePostAndReturnFlagUpdate(post: PostModelDTO) =
        suspendCoroutine { continuation ->
            postRef.child(post.id).setValue(post)
                .addOnSuccessListener { continuation.resume(FLAG_REQUEST_API_TRUE) }
                .addOnFailureListener { continuation.resume(FLAG_REQUEST_API_FAIL) }
        }

    fun refreshPostModelDomainComment(comment: PostModelDomain.CommentModelDomain) =
        _postModelDomain.value?.listComment?.add(comment)

    fun refreshPostModelDomainLikeOrUnLike(postModelDto: PostModelDTO) =
        postModelDto.also { _postModelDomain.value?.post = it }
}
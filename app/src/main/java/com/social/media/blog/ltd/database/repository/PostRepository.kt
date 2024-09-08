package com.social.media.blog.ltd.database.repository

import com.social.media.blog.ltd.database.dao.PostEntityDAO
import com.social.media.blog.ltd.model.entities.PostSaveEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostRepository(
    private val postDao: PostEntityDAO
) {

    fun fetchAllPostsSaved() = postDao.fetchAllPostSaveEntities()

    suspend fun savePost(post: PostSaveEntity) = withContext(Dispatchers.IO) {
        postDao.savePostEntity(post)
    }

    suspend fun unSavePost(id: String) = withContext(Dispatchers.IO) {
        postDao.unSavePostEntity(id)
    }

    suspend fun isUserSavePostBefore(id: String) = withContext(Dispatchers.IO) {
        postDao.isUserSavePostEntityBefore(id).size > 0
    }

}
package com.social.media.blog.ltd.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.social.media.blog.ltd.model.entities.PostSaveEntity

@Dao
interface PostEntityDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePostEntity(post: PostSaveEntity)

    @Query("SELECT * FROM POST")
    fun fetchAllPostSaveEntities(): LiveData<MutableList<PostSaveEntity>>

    @Query("DELETE FROM POST WHERE ID = :id")
    fun unSavePostEntity(id: String)

    @Query("SELECT * FROM POST WHERE id == :id")
    fun isUserSavePostEntityBefore(id: String): MutableList<PostSaveEntity>

}
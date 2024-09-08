package com.social.media.blog.ltd.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "POST")
data class PostSaveEntity(
    @PrimaryKey(autoGenerate = true)
    var idDataLocal: Long = 0L,

    var id: String = UUID.randomUUID().toString(),
    var idAuthor: String = "",
    var title: String = "",
    var content: String = "",
    var linkMedia: String = "", // link image url video url,
    var mediaLocation: String = "", // location of video or image on firebase
    var views: Int = 0,
    var isPublic: Boolean = true,
    var createdAt: Long = System.currentTimeMillis(),
    var updateAt: Long = createdAt,
    var listIdUserLiked: String = "",

    var listComments: String = "", // type item is json
    var category: String = "{}", // type object is json
) {
}
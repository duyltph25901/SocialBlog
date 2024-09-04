package com.social.media.blog.ltd.model.dto

import java.util.UUID
import java.io.Serializable

data class PostModelDTO(
    var id: String = UUID.randomUUID().toString(),
    var idAuthor: String = "",
    var title: String = "",
    var content: String = "",
    var linkMedia: String = "", // link image url video url,
    var mediaLocation: String = "", // location of video or image on firebase
    var views: Int = 0,
    var isPublic: Boolean = true,
    var category: CategoryModelDTO = CategoryModelDTO(),
    var createdAt: Long = System.currentTimeMillis(),
    var updateAt: Long = createdAt,
    var listIdUserLiked: MutableList<String> = mutableListOf(),
    var listComments: MutableList<Comment> = mutableListOf()

): Serializable {

    data class Comment(
        var idUser: String = "",
        var comment: String = "",
        var commentedAt: Long = System.currentTimeMillis()
    ): Serializable

}
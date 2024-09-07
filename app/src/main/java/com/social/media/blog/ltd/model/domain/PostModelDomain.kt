package com.social.media.blog.ltd.model.domain

import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.model.dto.UserModelDTO

data class PostModelDomain(
    var post: PostModelDTO = PostModelDTO(),
    var listComment: MutableList<CommentModelDomain> = mutableListOf(),
    var author: UserModelDTO = UserModelDTO()
) {

    data class CommentModelDomain(
        var comment: PostModelDTO.Comment = PostModelDTO.Comment(),
        var userName: String = "",
        var avatarUser: String = "",
    )

}
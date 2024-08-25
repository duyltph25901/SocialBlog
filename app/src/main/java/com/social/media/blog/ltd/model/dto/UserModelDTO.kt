package com.social.media.blog.ltd.model.dto

data class UserModelDTO(
    var idUser: String = "",
    var userName: String = "",
    var email: String = "",
    var password: String = "",
    var avatar: String = "https://firebasestorage.googleapis.com/v0/b/socialblog-4693a.appspot.com/o/avatar%2Favatar_default.png?alt=media&token=476aeb23-4163-4c55-a326-c65bb3836cc6",
    var exemplaryPoint: Int = 1000,
    var cratedAccountAt: Long = System.currentTimeMillis()
) {
}
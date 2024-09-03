package com.social.media.blog.ltd.model.domain

data class FolderModelDomain(
    var folderName: String,
    var images: MutableList<ImageModelDomain>,
) {
}
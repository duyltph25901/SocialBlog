package com.social.media.blog.ltd.model.domain

data class ImageModelDomain(
    var path: String = "",
    var folderName: String = "",
    var typeModel: Int = TYPE_IMAGE_MODEL_NORMAL
) {

    companion object {
        internal const val TYPE_IMAGE_MODEL_NORMAL = 0
        internal const val TYPE_IMAGE_MODEL_ADS = 1
    }

}
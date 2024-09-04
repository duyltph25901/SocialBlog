package com.social.media.blog.ltd.model.dto

import java.io.Serializable

data class CategoryModelDTO(
    var id: String = "1",
    var img: ImageCategoryModelDTO = ImageCategoryModelDTO(),
    var name: String = ""
): Serializable {

    /* *
     *      Image category object is link https://
     * */
    data class ImageCategoryModelDTO(
        var active: String = "",
        var unActive: String = ""
    ): Serializable

}
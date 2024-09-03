package com.social.media.blog.ltd.model.dto

data class CategoryModelDTO(
    var id: String = "1",
    var img: ImageCategoryModelDTO = ImageCategoryModelDTO(),
    var name: String = ""
) {

    /* *
     *      Image category object is link https://
     * */
    data class ImageCategoryModelDTO(
        var active: String = "",
        var unActive: String = ""
    ) { }

}
package com.social.media.blog.ltd.ui.screen.home.frg.vm

import androidx.lifecycle.ViewModel
import com.social.media.blog.ltd.database.repository.PostRepository

class SaveViewModel(
    private val repository: PostRepository
): ViewModel() {

    val listPost = repository.fetchAllPostsSaved()

}
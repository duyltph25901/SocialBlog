package com.social.media.blog.ltd.ui.screen.home.frg.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.social.media.blog.ltd.database.repository.PostRepository

@Suppress("UNCHECKED_CAST")
class ViewModelSaveFactory(
    private val repository: PostRepository
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SaveViewModel::class.java))
            return SaveViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
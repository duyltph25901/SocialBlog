package com.social.media.blog.ltd.ui.screen.home.frg.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.social.media.blog.ltd.commons.AppConst.categoryRef
import com.social.media.blog.ltd.commons.AppConst.postRef
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.model.dto.PostModelDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {

    private val _isLoadingCategory = MutableLiveData<Boolean>()
    private val _isLoadingPostOne = MutableLiveData<Boolean>()
    private val _categories = MutableLiveData<MutableList<CategoryModelDTO>>()
    private val _posts = MutableLiveData<MutableList<PostModelDTO>>()

    val isLoadingCategory: LiveData<Boolean> = _isLoadingCategory
    val isLoadingPostOne: LiveData<Boolean> = _isLoadingPostOne
    val categories: LiveData<MutableList<CategoryModelDTO>> = _categories
    val posts: LiveData<MutableList<PostModelDTO>> = _posts

    private val onEventListenerCategories: ValueEventListener
    private val onEventListenerPost: ValueEventListener

    init {
        onEventListenerCategories = createCategoriesEventListener()
        onEventListenerPost = createPostEventListener()
    }

    fun fetchCategories() = viewModelScope.launch(Dispatchers.IO) {
        _isLoadingCategory.postValue(true)
        categoryRef.addValueEventListener(onEventListenerCategories)
        _isLoadingCategory.postValue(false)
    }

    fun removeEventListenerCategories() = categoryRef.removeEventListener(onEventListenerCategories)

    fun removeEventListenerPosts() = postRef.removeEventListener(onEventListenerPost)

    private fun createCategoriesEventListener() = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val listCategories = getListCategoriesFromSnapShot(snapshot)
            _categories.postValue(listCategories)
        }

        override fun onCancelled(error: DatabaseError) = postEmptyListCategory()

    }

    private fun postEmptyListCategory() = _categories.postValue(mutableListOf())

    private fun getListCategoriesFromSnapShot(dataSnapshot: DataSnapshot): MutableList<CategoryModelDTO> {
        val listCategories = mutableListOf<CategoryModelDTO>()
        for (categorySnapShot in dataSnapshot.children) {
            val categoryModelDTO = categorySnapShot.getValue(CategoryModelDTO::class.java)
            if (categoryModelDTO!= null) {
                listCategories.add(categoryModelDTO)
            }
        }
        return listCategories
    }

    private fun createPostEventListener() = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val listPosts = getListPostsFromSnapShot(snapshot)
            _posts.postValue(listPosts)
        }

        override fun onCancelled(error: DatabaseError) = postEmptyListPost()
    }

    private fun postEmptyListPost() = _posts.postValue(mutableListOf())

    private fun getListPostsFromSnapShot(snapshot: DataSnapshot): MutableList<PostModelDTO> {
        val listPosts = mutableListOf<PostModelDTO>()
        for (categorySnapShot in snapshot.children) {
            val postModelDTO = categorySnapShot.getValue(PostModelDTO::class.java)
            if (postModelDTO!= null) {
                listPosts.add(postModelDTO)
            }
        }

        return listPosts
    }

    fun fetchPosts() = viewModelScope.launch(Dispatchers.IO) {
        _isLoadingPostOne.postValue(true)
        postRef.addValueEventListener(onEventListenerPost)
        _isLoadingPostOne.postValue(false)
    }
}
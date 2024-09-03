package com.social.media.blog.ltd.ui.screen.home.frg.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.social.media.blog.ltd.commons.AppConst.categoryRef
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {

    private val _isLoadingCategory = MutableLiveData<Boolean>()

    private val _categories = MutableLiveData<MutableList<CategoryModelDTO>>()

    private val onEventListenerCategories: ValueEventListener

    init {
        onEventListenerCategories = createCategoriesEventListener()
    }

    val isLoadingCategory: LiveData<Boolean> = _isLoadingCategory
    val categories: LiveData<MutableList<CategoryModelDTO>> = _categories

    fun fetchCategories() = viewModelScope.launch(Dispatchers.IO) {
        _isLoadingCategory.postValue(true)
        categoryRef.addValueEventListener(onEventListenerCategories)
        _isLoadingCategory.postValue(false)
    }

    fun removeEventListenerCategories() = categoryRef.removeEventListener(onEventListenerCategories)

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
}
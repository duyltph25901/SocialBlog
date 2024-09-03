package com.social.media.blog.ltd.ui.screen.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.AppConst.userRef
import com.social.media.blog.ltd.commons.extention.toastMessageRes
import com.social.media.blog.ltd.model.dto.UserModelDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _users = MutableLiveData<MutableList<UserModelDTO>>()
    private val _isLoadingFetchUsers = MutableLiveData<Boolean>()

    private val eventListener: ValueEventListener

    init {
        showLoading()
        eventListener = createUserEventListener()
    }

    val users: LiveData<MutableList<UserModelDTO>> = _users
    val isLoadingFetchUsers: LiveData<Boolean> = _isLoadingFetchUsers

    fun toastMessageInComing(context: Context) =
        context.toastMessageRes(R.string.this_feature_will_be_released_soon)

    fun fetchDataUser() = viewModelScope.launch(Dispatchers.IO) {
        userRef.addValueEventListener(eventListener)
    }

    fun searchEmailAndPassword(email: String, password: String, list: MutableList<UserModelDTO>) =
        list.firstOrNull { user ->
            user.email == email && user.password == password
        }

    private fun createUserEventListener(): ValueEventListener =
        object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listUsersModelDto = getListUserFromSnapShot(dataSnapshot)
                postListUserData(listUsersModelDto)
                hideLoading()
            }

            override fun onCancelled(error: DatabaseError) {
                postListUsersEmpty()
                hideLoading()
            }
        }

    private fun postListUsersEmpty() = _users.postValue(mutableListOf())

    private fun postListUserData(data: MutableList<UserModelDTO>) = _users.postValue(data)

    private fun getListUserFromSnapShot(dataSnapshot: DataSnapshot): MutableList<UserModelDTO> {
        val listUsersModelDto = mutableListOf<UserModelDTO>()
        for (userSnapShot in dataSnapshot.children) {
            val userModelDto = userSnapShot.getValue(UserModelDTO::class.java)
            if (userModelDto != null) {
                listUsersModelDto.add(userModelDto)
            }
        }

        return listUsersModelDto
    }

    fun stopListeningUserData() = userRef.removeEventListener(eventListener)

    private fun showLoading() = _isLoadingFetchUsers.postValue(true)

    private fun hideLoading() = _isLoadingFetchUsers.postValue(false)
}
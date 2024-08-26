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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginViewModel : ViewModel() {

    private val _users = MutableLiveData<MutableList<UserModelDTO>>()
    private val _isLoadingFetchUsers = MutableLiveData<Boolean>()
    private val _userDtoAfterSearch = MutableLiveData<UserModelDTO?>()

    val users: LiveData<MutableList<UserModelDTO>> = _users
    val isLoadingFetchUsers: LiveData<Boolean> = _isLoadingFetchUsers
    val userDtoAfterSearch: LiveData<UserModelDTO?> = _userDtoAfterSearch

    fun toastMessageInComing(context: Context) =
        context.toastMessageRes(R.string.this_feature_will_be_released_soon)

    fun fetchDataUser() = viewModelScope.launch(Dispatchers.IO) {
        _isLoadingFetchUsers.postValue(true)
        val resultListUser = fetchDataUserReturnResult()
        _users.postValue(resultListUser)
        _isLoadingFetchUsers.postValue(false)
    }

    fun searchEmailAndPassword(email: String, password: String, list: MutableList<UserModelDTO>) =
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingFetchUsers.postValue(true)
            val resultAfterSearch = searchEmailAndPasswordReturnResult(email, password, list)
            _userDtoAfterSearch.postValue(resultAfterSearch)
            _isLoadingFetchUsers.postValue(false)
        }


    private suspend fun searchEmailAndPasswordReturnResult(
        email: String,
        password: String,
        list: MutableList<UserModelDTO>
    ): UserModelDTO? = suspendCoroutine { continuation ->
        continuation.resume(list.firstOrNull { user ->
            user.email == email && user.password == password
        })
    }

    private suspend fun fetchDataUserReturnResult(): MutableList<UserModelDTO> =
        suspendCoroutine { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapShot: DataSnapshot) {
                    val listUsersModelDto = mutableListOf<UserModelDTO>()
                    for (userSnapShot in dataSnapShot.children) {
                        val userModelDto = userSnapShot.getValue(UserModelDTO::class.java)
                        if (userModelDto != null) {
                            listUsersModelDto.add(userModelDto)
                        }
                    }

                    continuation.resume(listUsersModelDto)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(mutableListOf())
                }
            }
            userRef.addValueEventListener(listener)
        }
}
package com.social.media.blog.ltd.ui.screen.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.social.media.blog.ltd.commons.AppConst.auth
import com.social.media.blog.ltd.commons.AppConst.userRef
import com.social.media.blog.ltd.model.dto.UserModelDTO
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RegisterViewModel : ViewModel() {

    private val _isLoadingNetWork = MutableLiveData<Boolean>()
    private val _isInsertSuccess = MutableLiveData<Boolean>()

    val isLoadingNetwork: LiveData<Boolean> = _isLoadingNetWork
    val isInsertSuccess: LiveData<Boolean> = _isInsertSuccess

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->

    }

    fun register(email: String, password: String, userName: String) =
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            _isLoadingNetWork.postValue(true)
            val userAfterRegister = registerAuthentication(email, password)
            userAfterRegister?.let { userFirebaseAuth ->
                val userDto =
                    getUserDtoFromUserFirebaseAuthentication(userFirebaseAuth, userName, password)
                _isInsertSuccess.postValue(registerAccountRealTimeDb(userDto))
            }
            _isLoadingNetWork.postValue(false)

            throw Exception()
        }

    private suspend fun registerAuthentication(
        email: String,
        password: String
    ): FirebaseUser? =
        suspendCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        auth.currentUser?.let {
                            continuation.resume(it)
                        }
                    } else continuation.resume(null)
                }
        }

    /**
     * This fun will be returned results after insert done
     * If true -> insert successful else failure
     * **/
    private suspend fun registerAccountRealTimeDb(userDto: UserModelDTO) =
        suspendCoroutine { continuation ->
            userRef.child(userDto.idUser).setValue(userDto)
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resume(false)
                }
        }

    private fun getUserDtoFromUserFirebaseAuthentication(
        userFirebase: FirebaseUser,
        userName: String,
        password: String
    ): UserModelDTO =
        UserModelDTO(
            idUser = userFirebase.uid,
            userName = userName,
            email = userFirebase.email ?: "",
            password = password,
        )
}
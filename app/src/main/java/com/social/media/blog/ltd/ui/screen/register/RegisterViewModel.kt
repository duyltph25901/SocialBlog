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
    private val _messageErrorRegister = MutableLiveData<String>("")

    val isLoadingNetwork: LiveData<Boolean> = _isLoadingNetWork
    val isInsertSuccess: LiveData<Boolean> = _isInsertSuccess
    val messageErrorRegister: LiveData<String> = _messageErrorRegister

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->

    }

    fun register(email: String, password: String, userName: String) =
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            _isLoadingNetWork.postValue(true)
            val resultAuthentication = registerAuthentication(email, password)
            val messageError = resultAuthentication.first
            val userAfterRegister = resultAuthentication.second
            if (messageError != null) {
                _messageErrorRegister.postValue(messageError.toString())
            } else {
                userAfterRegister?.let { userFirebaseAuth ->
                    val userDto =
                        getUserDtoFromUserFirebaseAuthentication(userFirebaseAuth, userName, password)
                    _isInsertSuccess.postValue(registerAccountRealTimeDb(userDto))
                }
            }
            _isLoadingNetWork.postValue(false)

            throw Exception()
        }

    private suspend fun registerAuthentication(
        email: String,
        password: String
    ): Pair<String?, FirebaseUser?> =
        suspendCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnFailureListener {
                    continuation.resume(Pair(it.message.toString(), null))
                }.addOnSuccessListener {
                    continuation.resume(Pair(null, it.user!!))
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

    fun clearMessageErrorRegister() = _messageErrorRegister.postValue("")
}
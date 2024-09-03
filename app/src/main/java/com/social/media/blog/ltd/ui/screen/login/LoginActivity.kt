package com.social.media.blog.ltd.ui.screen.login

import androidx.activity.viewModels
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.Routes.startHomeActivity
import com.social.media.blog.ltd.commons.Routes.startRegisterActivity
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.convertObjectToJson
import com.social.media.blog.ltd.commons.extention.toastMessageRes
import com.social.media.blog.ltd.databinding.ActivityLoginBinding
import com.social.media.blog.ltd.model.dto.UserModelDTO
import com.social.media.blog.ltd.ui.base.BaseActivity
import com.social.media.blog.ltd.ui.dialog.LoadingResponseDialog

class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var dialogLoading: LoadingResponseDialog
    private lateinit var listUsers: MutableList<UserModelDTO>

    override fun inflateViewBinding(): ActivityLoginBinding =
        ActivityLoginBinding.inflate(layoutInflater)

    override fun initVariable() {
        dialogLoading = LoadingResponseDialog(this@LoginActivity, false)

        listUsers = mutableListOf()
    }

    override fun clickViews() = binding.apply {
        textRegisterAccount.click { startRegisterActivity(this@LoginActivity) }
        icGg.click { toastMessageInComing() }
        icFb.click { toastMessageInComing() }
        icIg.click { toastMessageInComing() }
        icTelegrams.click { toastMessageInComing() }
        buttonLogin.click { eventLogin() }
    }

    override fun fetchDataSource() = viewModel.apply {
        if (isNetworkAvailable(this@LoginActivity)) fetchDataUser() else showNetworkError()
    }

    override fun observerDataSource() = viewModel.apply {
        isLoadingFetchUsers.observe(this@LoginActivity) { isLoading ->
            if (isLoading) dialogLoading.show() else dialogLoading.cancel()
        }

        users.observe(this@LoginActivity) { listUserDto ->
            submitListData(listUserDto)
        }
    }

    private fun toastMessageInComing() = viewModel.toastMessageInComing(this)

    private fun eventLogin() {
        val email = getEmail()
        val password = getPassword()
        val isInputEmpty = isInputEmpty(email, password)

        /* *
           validate null input
        * */
        if (!isNetworkAvailable(this)) {
            showNetworkError()
            return
        }; if (isInputEmpty) {
            showEmptyInputError()
            return
        }

        /* *
           handle search account
        * */
        handleSearchUser(email = email, password = password)
    }

    private fun cacheUserToSharedPref(user: UserModelDTO) {
        val jsonUserCache = convertObjectToJson(user)
        SharedUtils.jsonUserCache = jsonUserCache
    }

    private fun getEmail() = binding.inputEmailAddress.text.toString().trim()

    private fun getPassword() = binding.inputPassword.text.toString().trim()

    private fun isInputEmpty(email: String, password: String) =
        email.isEmpty() || password.isEmpty()

    private fun showNetworkError() = toastMessageRes(R.string.no_network_connection)

    private fun showEmptyInputError() = toastMessageRes(R.string.this_field_cannot_be_left_blank)

    private fun showAccountNotFound() = toastMessageRes(R.string.account_not_found)

    private fun handleSearchUser(email: String, password: String) =
        searchUser(email, password) { user ->
            if (user != null) onUserFound(user) else onUserNotFound()
        }

    private fun searchUser(
        email: String,
        password: String,
        callBack: (user: UserModelDTO?) -> Unit
    ) =
        viewModel.apply {
            val userAfterSearch = searchEmailAndPassword(email = email, password = password, list = listUsers)
            callBack.invoke(userAfterSearch)
        }

    private fun onUserFound(user: UserModelDTO) {
        cacheUserToSharedPref(user)
        startHomeActivity(this)
        removeEventListenerFirebase()
        finish()
    }

    private fun onUserNotFound() = showAccountNotFound()

    private fun submitListData(list: MutableList<UserModelDTO>) =
        listUsers.apply {
            clear()
            addAll(list)
        }

    private fun removeEventListenerFirebase() = viewModel.stopListeningUserData()
}
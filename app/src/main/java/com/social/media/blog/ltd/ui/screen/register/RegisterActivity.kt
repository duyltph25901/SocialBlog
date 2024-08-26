package com.social.media.blog.ltd.ui.screen.register

import androidx.activity.viewModels
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.toastMessage
import com.social.media.blog.ltd.commons.extention.toastMessageRes
import com.social.media.blog.ltd.commons.extention.validateEmail
import com.social.media.blog.ltd.commons.extention.validatePassword
import com.social.media.blog.ltd.databinding.ActivityRegisterBinding
import com.social.media.blog.ltd.ui.base.BaseActivity
import com.social.media.blog.ltd.ui.dialog.AcceptedTermOfServiceDialog
import com.social.media.blog.ltd.ui.dialog.LoadingResponseDialog
import com.social.media.blog.ltd.ui.dialog.TermOfPolicyDialog

class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {

    private val viewModel: RegisterViewModel by viewModels()

    private lateinit var loadingDialog: LoadingResponseDialog
    private lateinit var dialogTermOfPolicy: TermOfPolicyDialog
    private lateinit var dialogAcceptPolicy: AcceptedTermOfServiceDialog

    override fun inflateViewBinding(): ActivityRegisterBinding =
        ActivityRegisterBinding.inflate(layoutInflater)

    override fun initVariable() {
        loadingDialog = LoadingResponseDialog(this, true)
        dialogTermOfPolicy = TermOfPolicyDialog(this)
        dialogAcceptPolicy = AcceptedTermOfServiceDialog(this)
    }

    override fun initView() = binding.apply {

    }

    override fun clickViews() = binding.apply {
        icBack.click { finish() }
        buttonRegister.click { eventRegister() }
        textTermOfPolicy.click { dialogTermOfPolicy.show() }
    }

    override fun observerDataSource() = viewModel.apply {
        isLoadingNetwork.observe(this@RegisterActivity) { isLoading ->
            if (isLoading) loadingDialog.show() else loadingDialog.cancel()
        }

        isInsertSuccess.observe(this@RegisterActivity) { isSuccess ->
            if (isSuccess) {
                binding.inputUserName.text.clear()
                binding.inputPassword.text.clear()
                binding.inputEmailAddress.text.clear()
                toastMessageRes(R.string.account_registration_successful)
            }
        }

        messageErrorRegister.observe(this@RegisterActivity) { message ->
            if (message.isNotEmpty()) toastMessage(message)
            clearMessageErrorRegister()
        }
    }

    private fun eventRegister() {
        val email = binding.inputEmailAddress.text.toString().trim()
        val userName = binding.inputUserName.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()
        val isInputNull = email.isEmpty() || userName.isEmpty() || password.isEmpty()
        val isEmail = validateEmail(email)
        val isPassword = validatePassword(password)

        if (!isNetworkAvailable(this)) {
            toastMessageRes(R.string.no_network_connection)
            return
        }; if (isInputNull) {
            toastMessageRes(R.string.this_field_cannot_be_left_blank)
            return
        }; if (!isEmail) {
            toastMessageRes(R.string.invalid_email)
            return
        }; if (!isPassword) {
            toastMessageRes(R.string.password_minimum_six_characters)
            return
        }

        dialogAcceptPolicy.apply {
            eventClickDone = {
                val jobRegisterAccount = viewModel.register(email, password, userName)
                loadingDialog.cancelProgress = {
                    jobRegisterAccount.cancel()
                }
            }
        }.show()
    }
}
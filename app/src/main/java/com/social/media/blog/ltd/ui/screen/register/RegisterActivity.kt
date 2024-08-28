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

    override fun clickViews() = binding.apply {
        icBack.click { finish() }
        buttonRegister.click { eventRegister() }
        textTermOfPolicy.click { showDialogTermOfPolicy() }
    }

    override fun observerDataSource() = viewModel.apply {
        isLoadingNetwork.observe(this@RegisterActivity) { isLoading ->
            if (isLoading) showDialogLoading() else hideDialogLoading()
        }

        isInsertSuccess.observe(this@RegisterActivity) { isSuccess ->
            if (isSuccess) {
                clearInputField()
                showMessageRegisterAccountSuccess()
            }
        }

        messageErrorRegister.observe(this@RegisterActivity) { message ->
            if (message.isNotEmpty()) showErrorMessage(message)
            clearMessageErrorRegister()
        }
    }

    private fun eventRegister() {
        val email = getEmail()
        val userName = getUserName()
        val password = getPassword()
        val isInputNull = isStrEmpty(email, password, userName)
        val isEmail = validateEmail(email)
        val isPassword = validatePassword(password)

        if (!isNetworkAvailable(this)) {
            showMessageNoInternet()
            return
        }; if (isInputNull) {
            showMessageNotNull()
            return
        }; if (!isEmail) {
            showMessageEmailInvalid()
            return
        }; if (!isPassword) {
            showMessagePasswordInvalid()
            return
        }

        showDialogAcceptPolicy(email = email, password = password, userName = userName)
    }

    private fun showDialogTermOfPolicy() = dialogTermOfPolicy.show()

    private fun getEmail() = binding.inputEmailAddress.text.toString().trim()

    private fun getUserName() = binding.inputUserName.text.toString().trim()

    private fun getPassword() = binding.inputPassword.text.toString().trim()

    private fun isStrEmpty(email: String, userName: String, password: String) =
        email.isEmpty() || userName.isEmpty() || password.isEmpty()

    private fun showMessageNoInternet() =
        toastMessageRes(R.string.no_network_connection)

    private fun showMessageNotNull() =
        toastMessageRes(R.string.this_field_cannot_be_left_blank)

    private fun showMessageEmailInvalid() =
        toastMessageRes(R.string.invalid_email)

    private fun showMessagePasswordInvalid() =
        toastMessageRes(R.string.password_minimum_six_characters)

    private fun showDialogAcceptPolicy(email: String, password: String, userName: String) =
        dialogAcceptPolicy.apply {
            eventClickDone = {
                val jobRegisterAccount = viewModel.register(email, password, userName)
                loadingDialog.cancelProgress = {
                    jobRegisterAccount.cancel()
                }
            }
        }.show()

    private fun showDialogLoading() = loadingDialog.show()

    private fun hideDialogLoading() = loadingDialog.cancel()

    private fun showErrorMessage(message: String) = toastMessage(message)

    private fun showMessageRegisterAccountSuccess() = toastMessageRes(R.string.account_registration_successful)

    private fun clearInputUserName() = binding.inputUserName.text.clear()

    private fun clearInputPassword() = binding.inputPassword.text.clear()

    private fun clearInputEmail() = binding.inputEmailAddress.text.clear()

    private fun clearInputField()  {
        clearInputUserName()
        clearInputPassword()
        clearInputEmail()
    }
}
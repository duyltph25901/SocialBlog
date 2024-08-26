package com.social.media.blog.ltd.commons.extention

import android.content.Context
import android.widget.Toast
import com.social.media.blog.ltd.commons.ConvertUtils.convertJsonToObjectConvertUtils
import com.social.media.blog.ltd.commons.ConvertUtils.convertObjectToJsonConvertUtils
import java.util.Locale
import java.util.regex.Pattern

var toast: Toast? = null

fun Context.setAppLocale(language: String): Context {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}

fun Context.toastMessage(message: String) {
    toast?.cancel()
    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    toast!!.show()
}

fun Context.toastMessageRes(message: Int) {
    toast?.cancel()
    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    toast!!.show()
}

fun Context.validateEmail(email: String): Boolean {
    val emailPattern =
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    val pattern = Pattern.compile(emailPattern)

    return pattern.matcher(email).matches()
}

fun Context.validatePassword(password: String): Boolean = password.length >= 6

fun <T> Context.convertObjectToJson(obj: T): String = convertObjectToJsonConvertUtils(obj = obj)

fun <T> Context.convertJsonToObject(json: String, classOfT: Class<T>): T? =
    convertJsonToObjectConvertUtils(json = json, classOfT = classOfT)
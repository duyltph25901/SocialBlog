package com.social.media.blog.ltd.commons

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.extention.toastMessageRes
import com.tbruyelle.rxpermissions3.RxPermissions

object PermissionUtils {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun isReadMediaImageAccepted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

    fun isReadExternalStorageAccepted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("CheckResult")
    fun requestReadMediaImagePermission(fragment: Fragment, actionAfterAccept: () -> Unit) {
        RxPermissions(fragment).requestEach(Manifest.permission.READ_MEDIA_IMAGES)
            .subscribe { permission ->
                when {
                    permission.granted -> actionAfterAccept.invoke()

                    permission.shouldShowRequestPermissionRationale -> Unit

                    else -> fragment.context?.toastMessageRes(R.string.please_allow_us_to_access_your_camera_and_take_picture)

                }
            }
    }

    @SuppressLint("CheckResult")
    fun requestReadExternalPermission(fragment: Fragment, actionAfterAccept: () -> Unit) {
        RxPermissions(fragment).requestEach(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe { permission ->
                when {
                    permission.granted -> actionAfterAccept.invoke()

                    permission.shouldShowRequestPermissionRationale -> Unit

                    else -> fragment.context?.toastMessageRes(R.string.please_allow_us_to_access_your_camera_and_take_picture)
                }
            }
    }

    fun isCameraPermissionAccepted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("CheckResult")
    fun requestCameraPermission(fragment: Fragment, callback: () -> Unit) {
        RxPermissions(fragment).requestEach(Manifest.permission.CAMERA).subscribe { permission ->
            when {
                permission.granted -> callback.invoke()

                permission.shouldShowRequestPermissionRationale -> Unit

                else -> fragment.context?.toastMessageRes(R.string.message_denied_camera_permission)
            }
        }
    }

}
package com.social.media.blog.ltd.commons

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object AppConst {
    // STRING
    internal const val KEY_RESULT_PICK_IMAGE = "KEY_RESULT_PICK_IMAGE"
    internal const val KEY_INDEX_SELECTED_IMAGE = "KEY_INDEX_SELECTED_IMAGE"
    internal const val KEY_PATH_IMAGE_TO_CROP = "KEY_PATH_IMAGE_TO_CROP"
    internal const val KEY_IMAGE_URI_TO_CROP = "KEY_IMAGE_URI_TO_CROP"
    internal const val KEY_IMAGE_PATH_AFTER_CROP = "KEY_IMAGE_PATH_AFTER_CROP"
    internal const val KEY_SHOW_POST_DETAIL = "KEY_SHOW_POST_DETAIL"

    // NUMBER
    internal const val SPLASH_DELAY_TIME_MS_TYPE = 4000L

    // FIREBASE VARIABLES
    private val database = FirebaseDatabase.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val userRef = database.getReference("Users")
    val categoryRef = database.getReference("Categories")
    val postRef = database.getReference("Posts")
    val postStorageRef = storageRef.child("post")
}
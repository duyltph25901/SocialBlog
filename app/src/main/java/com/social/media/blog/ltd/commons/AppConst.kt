package com.social.media.blog.ltd.commons

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object AppConst {

    // NUMBER
    internal const val SPLASH_DELAY_TIME_MS_TYPE = 4000L

    // FIREBASE VARIABLES
    private val database = FirebaseDatabase.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val userRef = database.getReference("Users")
}
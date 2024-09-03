package com.social.media.blog.ltd.commons

import android.content.Context
import android.content.SharedPreferences

object SharedUtils {
    private const val PREFERENCES_NAME = "SocialBlog"
    private var sharePref: SharedPreferences? = null

    fun init(context: Context) {
        if (sharePref == null) {
            sharePref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        }
    }

    fun setValue(keyWord: String, value: Any?) {
        val editor = sharePref?.edit()
        when (value) {
            is Int -> editor?.putInt(keyWord, value)
            is Float -> editor?.putFloat(keyWord, value)
            is Long -> editor?.putLong(keyWord, value)
            is Boolean -> editor?.putBoolean(keyWord, value)
            is String -> editor?.putString(keyWord, value)
        }
        editor?.apply()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getValue(keyWord: String, defaultValue: T): T {
        return when (defaultValue) {
            is Int -> (sharePref?.getInt(keyWord, defaultValue) ?: defaultValue) as T
            is Long -> (sharePref?.getLong(keyWord, defaultValue) ?: defaultValue) as T
            is Float -> (sharePref?.getFloat(keyWord, defaultValue) ?: defaultValue) as T
            is Boolean -> (sharePref?.getBoolean(keyWord, defaultValue) ?: defaultValue) as T
            is String -> (sharePref?.getString(keyWord, defaultValue) ?: defaultValue) as T
            else -> return defaultValue
        }
    }

    var jsonUserCache: String
        get() = getValue("json_user_cache", "{}")
        set(value) = setValue("json_user_cache", value)

    /**
     *      If true -> post is public mode
     *      else false -> post is private
     * **/
    var modePost: Boolean
        get() = getValue("mode_post", true)
        set(value) = setValue("mode_post", value)


    var indexCategory: Int
        get() = getValue("index_category_post", 0)
        set(value) = setValue("index_category_post", value)

    /**
     *      category post will be saved by json
     * **/
    var jsonCategory: String
        get() = getValue("json_category_post", "{}")
        set(value) = setValue("json_category_post", value)
}
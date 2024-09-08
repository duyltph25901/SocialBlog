package com.social.media.blog.ltd.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.social.media.blog.ltd.database.dao.PostEntityDAO
import com.social.media.blog.ltd.model.entities.PostSaveEntity

@Database(
    entities = [PostSaveEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostEntityDAO

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            synchronized(this) {
                if (instance != null) instance!!
                else {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "SocialBlogDatabase.db"
                    )
                        .allowMainThreadQueries().build()
                    instance!!
                }
            }
    }

}
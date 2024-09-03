package com.social.media.blog.ltd.ui.screen.images

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.media.blog.ltd.model.domain.FolderModelDomain
import com.social.media.blog.ltd.model.domain.ImageModelDomain
import com.social.media.blog.ltd.model.domain.ImageModelDomain.Companion.TYPE_IMAGE_MODEL_NORMAL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PickImageViewModel: ViewModel() {

    private val _images = MutableLiveData<MutableList<ImageModelDomain>>()

    private var listFolder: MutableList<FolderModelDomain> = mutableListOf()
    private var listImage: MutableList<ImageModelDomain> = mutableListOf()
    private var listAllImage: MutableList<ImageModelDomain> = mutableListOf()

    val images: LiveData<MutableList<ImageModelDomain>> = _images

    @SuppressLint("Recycle")
    fun getAllImageFromDevice(context: Context) = viewModelScope.launch(Dispatchers.IO) {

        // start progress
        listFolder.clear()
        listImage.clear()
        listAllImage.clear()

        // handle action progress
        var position = 0
        val cursor: Cursor?
        var columnIndexData = 0
        var columnIndexFolderName = 0
        var absolutePathOfImage: String
        var folder = false
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection =
            arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )
        val orderBy = MediaStore.Images.Media.DATE_TAKEN
        cursor = context.contentResolver.query(
            uri, projection, null, null,
            "$orderBy DESC"
        )
        if (cursor != null) {
            columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        }
        if (cursor != null) {
            columnIndexFolderName =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(columnIndexData)
                for (i in 0 until listFolder.size) {
                    if (listFolder[i].folderName == cursor.getString(columnIndexFolderName)
                    ) {
                        folder = true
                        position = i
                        break
                    } else {
                        folder = false
                    }
                }
                if (folder) {
                    val alPath: MutableList<ImageModelDomain> = mutableListOf()
                    alPath.addAll(listFolder[position].images)
                    val file = File(absolutePathOfImage)

                    cursor.getString(columnIndexFolderName)?.let {
                        alPath.add(
                            ImageModelDomain(
                                path = file.path,
                                it,
                                TYPE_IMAGE_MODEL_NORMAL
                            )
                        )
                    }

                    listFolder[position].images = alPath
                } else {
                    val alPath: MutableList<ImageModelDomain> = mutableListOf()
                    val file = File(absolutePathOfImage)

                    cursor.getString(columnIndexFolderName)?.let {
                        alPath.add(
                            ImageModelDomain(
                                path = file.path,
                                it,
                                TYPE_IMAGE_MODEL_NORMAL
                            )
                        )
                    }

                    cursor.getString(columnIndexFolderName)?.let {
                        if (cursor.getType(columnIndexFolderName) == Cursor.FIELD_TYPE_STRING) {
                            val objModel =
                                FolderModelDomain(
                                    cursor.getString(columnIndexFolderName),
                                    alPath
                                )
                            listFolder.add(objModel)
                        }
                    }

                }
            }

            listFolder.forEach { item ->
                if (item.images.size > 0) {
                    listImage.addAll(item.images)
                    listAllImage.addAll(item.images)
                }
            }

            listFolder.add(
                0,
                FolderModelDomain(
                    "All Images",
                    listAllImage
                )
            )
        }

        // end progress
        _images.postValue(listImage)
    }

}
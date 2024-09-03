/**
 * Explain the code lines and flow of the application
 *
 * @see
 * If the user selects the camera icon,
 * the user can open the camera. After the user takes a photo,
 * it will automatically be taken to the crop screen.
 * The data sent from the current screen to the crop screen is in the form of a uri.
 *
 * @see
 * If the user selects the photo icon, the application will open the photo selection screen.
 * After selecting the photo, the application will automatically navigate to the crop screen.
 * The data sent from the current screen to the crop screen is a string meaning the path of the image.
 *
 * @see
 * After cropping an image,
 * the data sent from the crop screen to the current screen is a path of the cropped image.
 *
 * @see
 * Main stream
 * 1. Take photo:
 *      Clicking the Camera icon will open the camera screen. If the user takes a photo,
 *  the app essentially returns to the current screen and checks the data after taking the photo.
 *  If there is data, it will automatically take the user to the cropping screen.
 * 2. Pick Image:
 *      Clicking on the photo selection icon will open the photo selection screen. After selecting,
 * the essence will return to the current screen and check whether there is data or not.
 * If there is data, it will send that data to the crop screen, otherwise nothing will be done.
 * **/

package com.social.media.blog.ltd.ui.screen.home.frg

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.AppConst.KEY_IMAGE_PATH_AFTER_CROP
import com.social.media.blog.ltd.commons.AppConst.KEY_IMAGE_URI_TO_CROP
import com.social.media.blog.ltd.commons.AppConst.KEY_INDEX_SELECTED_IMAGE
import com.social.media.blog.ltd.commons.AppConst.KEY_PATH_IMAGE_TO_CROP
import com.social.media.blog.ltd.commons.AppConst.KEY_RESULT_PICK_IMAGE
import com.social.media.blog.ltd.commons.AppConst.postStorageRef
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.convertJsonToObject
import com.social.media.blog.ltd.commons.extention.goneView
import com.social.media.blog.ltd.commons.extention.isCameraPermissionAccepted
import com.social.media.blog.ltd.commons.extention.isReadExternalStorageAccepted
import com.social.media.blog.ltd.commons.extention.isReadMediaImageAccepted
import com.social.media.blog.ltd.commons.extention.requestCameraPermission
import com.social.media.blog.ltd.commons.extention.requestReadExternalStoragePermission
import com.social.media.blog.ltd.commons.extention.requestReadMediaImagePermission
import com.social.media.blog.ltd.commons.extention.toastMessageRes
import com.social.media.blog.ltd.commons.extention.visibleView
import com.social.media.blog.ltd.databinding.FragmentAddBinding
import com.social.media.blog.ltd.model.domain.ImageModelDomain
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.model.dto.UserModelDTO
import com.social.media.blog.ltd.ui.adapter.rcv.CategoryChosenAdapter
import com.social.media.blog.ltd.ui.base.BaseFragment
import com.social.media.blog.ltd.ui.dialog.ChangeModePostDialog
import com.social.media.blog.ltd.ui.dialog.ChooseCategoryDialog
import com.social.media.blog.ltd.ui.dialog.LoadingResponseDialog
import com.social.media.blog.ltd.ui.screen.crop_img.CropImageActivity
import com.social.media.blog.ltd.ui.screen.home.frg.vm.AddViewModel
import com.social.media.blog.ltd.ui.screen.images.PickImageActivity
import java.io.ByteArrayOutputStream
import java.io.File

class AddFragment : BaseFragment<FragmentAddBinding>() {

    private val viewModel: AddViewModel by viewModels()

    private lateinit var dialogChangeModePost: ChangeModePostDialog
    private lateinit var dialogChangeCategory: ChooseCategoryDialog
    private lateinit var dialogLoading: LoadingResponseDialog
    private lateinit var listCategories: MutableList<CategoryModelDTO>
    private lateinit var categoryAdapter: CategoryChosenAdapter

    private var indexImageSelected = -1

    private var uriTakePhoto: Uri? = null

    override fun getLayoutFragment(): Int = R.layout.fragment_add

    override fun initVariables() {
        initAdapterCategory()
        initListCategories()
        initDialogChangeModePost()
        initDialogChangeCategory()
        initDialogLoading()
    }

    override fun fetchDataSrc() {
        viewModel.apply {
            fetchDataCategories()
            initModePost()
            initUserAccountCurrent(mBinding.root.context)
        }
    }

    override fun observerData() {
        viewModel.apply {
            userAccountCurrent.observe(viewLifecycleOwner) { account ->
                Glide.with(mBinding.root.context).load(account.avatar).into(mBinding.avatarUser)
                mBinding.textUserName.text = account.userName
            }

            modePost.observe(viewLifecycleOwner) { isPublicMode ->
                mBinding.textMode.text =
                    if (isPublicMode) mBinding.root.context.getString(R.string.public_text)
                    else mBinding.root.context.getString(R.string.private_text)
                Glide.with(mBinding.root.context).load(
                    if (isPublicMode) R.drawable.ic_public else R.drawable.ic_private
                ).into(mBinding.icStatusPost)
            }

            categoryMode.observe(viewLifecycleOwner) { category ->
                mBinding.textCategoryName.text = category.name
            }

            categories.observe(viewLifecycleOwner) { categoriesObserve ->
                addDataToListCategories(categoriesObserve)
            }

            pathImageSelected.observe(viewLifecycleOwner) { pathImage ->
                if (pathImage.isNotEmpty()) {
                    this@AddFragment.refreshBitmapImageAfterCrop(null)
                    updateUiByPathImage(pathImage)
                }
            }

            bitmapImageAfterCrop.observe(viewLifecycleOwner) { bitmapImage ->
                if (bitmapImage != null) {
                    updateUiByBitmapAfterConvert(bitmapImage)
                    this@AddFragment.refreshPathImageSelected("")
                }
            }

            isLoadingUploadPost.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) dialogLoading.show() else dialogLoading.cancel()
            }

            resultAfterUploadPost.observe(viewLifecycleOwner) { isSuccess ->
                if (isSuccess) {
                    showMessageUploadPostSuccess()
                    clearInputUI()
                } else showMessageUploadPostFailure()
            }
        }
    }

    override fun onClickViews() {
        mBinding.apply {
            icChooseMode.click { showDialogChangeModePost() }
            icChooseCategory.click { showDialogChangeCategory() }
            icPhoto.click { openImagesScreen() }
            icCamera.click { openCamera() }
            buttonPublish.click { publishPost() }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.initCategoryPost(mBinding.root.context)
    }

    private fun initDialogChangeModePost() {
        dialogChangeModePost = ChangeModePostDialog(mBinding.root.context, SharedUtils.modePost)
        { modeChanged ->
            SharedUtils.modePost = modeChanged
            viewModel.refreshModePost(modeChanged)
        }
    }

    private fun initDialogChangeCategory() {
        dialogChangeCategory = ChooseCategoryDialog(mBinding.root.context, categoryAdapter)
    }

    private fun initDialogLoading() {
        dialogLoading = LoadingResponseDialog(
            mBinding.root.context,
            false, null
        )
    }

    private fun initListCategories() {
        listCategories = mutableListOf()
    }

    private fun initAdapterCategory() {
        categoryAdapter = CategoryChosenAdapter { category, index ->
            dialogChangeCategory.refreshDataRcv(category, index) {
                viewModel.refreshCategoryMode(category)
            }
        }
    }

    private fun submitCategoryListAdapter(data: MutableList<CategoryModelDTO>) =
        categoryAdapter.submitData(data)

    private fun addDataToListCategories(data: MutableList<CategoryModelDTO>) {
        listCategories.apply {
            clear()
            addAll(data)
        }

        submitCategoryListAdapter(data)
    }

    private fun openImagesScreen() {
        if (isThisTiramisuVersionOrHigher()) {
            if (!isReadMediaImageAccepted()) requestReadMediaImagePermission()
            else openImagesActivity()
        } else {
            if (!isReadExternalStorage()) requestReadExternalStoragePermission()
            else openImagesActivity()
        }
    }

    private fun showDialogChangeModePost() = dialogChangeModePost.show()

    private fun showDialogChangeCategory() = dialogChangeCategory.show()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isReadMediaImageAccepted() = mBinding.root.context.isReadMediaImageAccepted()

    private fun isReadExternalStorage() = mBinding.root.context.isReadExternalStorageAccepted()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestReadMediaImagePermission() =
        mBinding.root.context.requestReadMediaImagePermission(fragment = this) {
            openImagesActivity()
        }

    private fun requestReadExternalStoragePermission() =
        mBinding.root.context.requestReadExternalStoragePermission(fragment = this) { openImagesActivity() }

    private fun openImagesActivity() = resulLaunchPickImage.launch(
        Intent(
            mBinding.root.context,
            PickImageActivity::class.java
        ).apply {
            putExtra(KEY_INDEX_SELECTED_IMAGE, indexImageSelected)
        })

    private val resulLaunchPickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            /**
             *  Logic: If the user selects a photo in the gallery, it will transfer that photo to the crop screen
             *  Otherwise, do nothing
             */

            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val extra = data.extras
                    if (extra != null) {
                        val imageSelectedJsonType = extra.getString(KEY_RESULT_PICK_IMAGE)
                        val indexChosenImage = extra.getInt(KEY_INDEX_SELECTED_IMAGE, -1)
                        indexImageSelected = indexChosenImage
                        if (imageSelectedJsonType != null) {
                            val imageModel = convertJsonToImageModelDomain(imageSelectedJsonType)
                            val intentCropImage = getIntentCropImageActivity(imageModel.path, null)
                            refreshPathImageSelected(imageModel.path)
                            goToCropImageScreen(intentCropImage)
                        }
                    }
                }
            }
        }

    private fun convertJsonToImageModelDomain(json: String) =
        mBinding.root.context.convertJsonToObject(json, ImageModelDomain::class.java)

    private fun openCamera() {
        if (isCameraPermissionAccepted()) openCameraScreenSystem() else requestCameraPermission()
    }

    private fun isCameraPermissionAccepted() =
        mBinding.root.context.isCameraPermissionAccepted()

    private fun requestCameraPermission() =
        mBinding.root.context.requestCameraPermission(this) { }

    private fun openCameraScreenSystem() {
        try {
            uriTakePhoto = createImageUri()
            resultLauncherTakeCamera.launch(uriTakePhoto)
        } catch (e: Exception) {
            showMessageTakePhotoAgain()
        }
    }

    @SuppressLint("LogNotTimber")
    private val resultLauncherTakeCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { haveData ->
            try {
                if (haveData) {
                    if (uriTakePhoto != null) {
                        val intentCrop =
                            getIntentCropImageActivity(
                                valuePathImage = "",
                                valueImageTakePhoto = uriTakePhoto.toString()
                            )
                        goToCropImageScreen(intentCrop)
                    }
                } else showMessageTakePhotoAgain()
            } catch (e: Exception) {
                showMessageTakePhotoAgain()
            }
        }

    private fun createImageUri(): Uri {
        val image = File(
            mBinding.root.context.applicationContext.filesDir,
            "socialBlog_${System.currentTimeMillis()}_camera_photos.png"
        )
        return FileProvider.getUriForFile(
            mBinding.root.context,
            mBinding.root.context.packageName,
            image
        )
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("LogNotTimber")
    private var resultLauncherCropImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val extra = data.getStringExtra(KEY_IMAGE_PATH_AFTER_CROP)
                    if (extra != null) {
                        val bitmapAfterConvert = convertPathImageToBitmap(extra)
                        refreshBitmapImageAfterCrop(bitmapAfterConvert)
                    }
                }
            }
        }

    private fun refreshBitmapImageAfterCrop(bitmap: Bitmap?) =
        viewModel.refreshBitmapImageAfterCrop(bitmap)

    private fun showImageMediaAndHideIconMedia() = mBinding.apply {
        icMedia.goneView()
        imgMedia.visibleView()
    }

    private fun showIconMediaAndHIdeImageMedia() = mBinding.apply {
        icMedia.visibleView()
        imgMedia.goneView()
    }

    private fun updateUiByPathImage(path: String) {
        showImageMediaAndHideIconMedia()
        Glide.with(mBinding.root.context).load(path)
            .into(mBinding.imgMedia)
    }

    private fun updateUiByBitmapAfterConvert(bitmap: Bitmap) {
        showImageMediaAndHideIconMedia()
        Glide.with(mBinding.root.context).load(bitmap).into(mBinding.imgMedia)
    }

    private fun refreshPathImageSelected(path: String) = viewModel.refreshPathImageSelected(path)

    private fun showMessageTakePhotoAgain() =
        mBinding.root.context.toastMessageRes((R.string.take_photo_again))

    private fun goToCropImageScreen(intent: Intent) = resultLauncherCropImage.launch(intent)

    private fun getIntentCropImageActivity(valuePathImage: String, valueImageTakePhoto: String?) =
        Intent(mBinding.root.context, CropImageActivity::class.java).apply {
            putExtra(KEY_PATH_IMAGE_TO_CROP, valuePathImage)
            putExtra(KEY_IMAGE_URI_TO_CROP, valueImageTakePhoto)
        }

    private fun convertPathImageToBitmap(path: String) =
        BitmapFactory.decodeFile(
            path,
            /*BitmapFactory.Options().apply { inSampleSize = 7 *//*Giảm độ phân giải ảnh xuống còn 1/7*//* }*/
        )

    private fun getTitlePost() = mBinding.inputTitlePost.text.toString().trim()

    private fun getContentPost() = mBinding.inputDescription.text.toString().trim()

    private fun getModePost() = SharedUtils.modePost

    private fun convertJsonToCategoryModelDto(json: String) =
        mBinding.root.context.convertJsonToObject(json, CategoryModelDTO::class.java)

    private fun convertJsonToAccountCurrent() =
        mBinding.root.context.convertJsonToObject(
            SharedUtils.jsonUserCache,
            UserModelDTO::class.java
        )

    private fun getIdUserCurrent() = convertJsonToAccountCurrent().idUser

    private fun getValueBitmapAfterCrop() = viewModel.getValueBitmapAfterCrop()

    private fun isNullInput(title: String, content: String, bitmapImg: Bitmap?) =
        title.isEmpty() || content.isEmpty() || bitmapImg == null

    private fun showMessageNullInput() =
        mBinding.root.context.toastMessageRes(R.string.this_field_cannot_be_left_blank)

    private fun publishPost() {
        val title = getTitlePost()
        val content = getContentPost()
        val modePost = getModePost()
        val categoryMode = convertJsonToCategoryModelDto(SharedUtils.jsonCategory)
        val idAuthor = getIdUserCurrent()
        val bitmapImageFinal = getValueBitmapAfterCrop()

        if (isNullInput(title, content, bitmapImageFinal)) {
            showMessageNullInput()
            return
        }

        val post = getPostModelDto(
            idAuthor = idAuthor,
            title = title,
            content = content,
            modePost = modePost,
            categoryMode = categoryMode
        )
        handlePushPost(post, bitmapImageFinal!!)
    }

    private fun getPostModelDto(
        idAuthor: String,
        title: String,
        content: String,
        modePost: Boolean,
        categoryMode: CategoryModelDTO
    ) = PostModelDTO(
        idAuthor = idAuthor,
        title = title,
        content = content,
        isPublic = modePost,
        category = categoryMode,
    )

    private fun handlePushPost(post: PostModelDTO, bitmapImage: Bitmap) =
        viewModel.handleUploadPost(post, bitmapImage)

    private fun showMessageUploadPostFailure() =
        mBinding.root.context.toastMessageRes(R.string.upload_post_fail)
    
    private fun showMessageUploadPostSuccess() =
        mBinding.root.context.toastMessageRes(R.string.upload_post_success)

    private fun clearInputUI() {
        clearInput()
        clearObserveVariable()
        showIconMediaAndHIdeImageMedia()
    }

    private fun clearInput() {
        clearInputDescription()
        clearInputTitle()
    }

    private fun clearInputDescription() = mBinding.inputDescription.text.clear()

    private fun clearInputTitle() = mBinding.inputTitlePost.text.clear()

    private fun clearObserveVariable() {
        refreshPathImageSelected("")
        refreshBitmapImageAfterCrop(null)
    }
}
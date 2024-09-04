package com.social.media.blog.ltd.ui.screen.home.frg

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.AppConst.KEY_SHOW_POST_DETAIL
import com.social.media.blog.ltd.commons.Routes
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.commons.extention.convertObjectToJson
import com.social.media.blog.ltd.commons.extention.goneView
import com.social.media.blog.ltd.commons.extention.hideKeyboard
import com.social.media.blog.ltd.commons.extention.toastMessageRes
import com.social.media.blog.ltd.commons.extention.visibleView
import com.social.media.blog.ltd.databinding.FragmentHomeBinding
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.ui.adapter.rcv.CategoryAdapter
import com.social.media.blog.ltd.ui.adapter.rcv.PostAdapterTypeOne
import com.social.media.blog.ltd.ui.base.BaseFragment
import com.social.media.blog.ltd.ui.dialog.LoadingResponseDialog
import com.social.media.blog.ltd.ui.screen.home.frg.vm.HomeViewModel
import com.social.media.blog.ltd.ui.screen.home.frg.vm.HomeViewModel.Companion.FLAG_REQUEST_API_TRUE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var postAdapterOne: PostAdapterTypeOne
    private lateinit var loadingDialog: LoadingResponseDialog

    override fun getLayoutFragment(): Int = R.layout.fragment_home

    override fun initVariables() {
        initCategoryAdapter()
        initPostAdapterOne()
        initDialogResponseLoading()
    }

    override fun initViews() {
        mBinding.apply {
            rcvCategory.apply {
                adapter = categoryAdapter
                setHasFixedSize(true)
                clipToPadding = false
                setItemViewCacheSize(20)
            }

            rcv2.apply {
                adapter = postAdapterOne
                setHasFixedSize(true)
                clipToPadding = false
                setItemViewCacheSize(20)
            }
        }
    }

    override fun fetchDataSrc() {
        viewModel.apply {
            fetchCategories()
            fetchPosts()
        }
    }

    override fun observerData() {
        viewModel.apply {
            isLoadingCategory.observe(viewLifecycleOwner) { loading ->
                if (loading) showLoadingCategory() else hideLoadingCategory()
            }

            categories.observe(viewLifecycleOwner) { listCategories ->
                setNewDataCategory(listCategories)
                cacheCategoryPostIfCan(listCategories[0], 0)
            }

            isLoadingPostOne.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) showLoadingPostOne() else hideLoadingPostOne()
            }

            isLoadingResponse.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) showDialogResponse() else hideDialogResponse()
            }

            posts.observe(viewLifecycleOwner) { listPosts ->
                setNewDataPost(listPosts)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.apply {
            removeEventListenerCategories()
            removeEventListenerPosts()
        }
    }

    private fun initCategoryAdapter() {
        categoryAdapter = CategoryAdapter(selectedCategory = { category, index ->
            val indexBefore = categoryAdapter.itemSelected
            categoryAdapter.itemSelected = index
            if (indexBefore != -1) categoryAdapter.notifyItemChanged(indexBefore)
        }, unSelectedCategory = { category, index ->
            val indexBefore = categoryAdapter.itemSelected
            categoryAdapter.itemSelected = -1
            categoryAdapter.notifyItemChanged(indexBefore)
        })
    }

    private fun setNewDataCategory(newData: MutableList<CategoryModelDTO>) =
        categoryAdapter.submitData(newData)

    private fun cacheCategoryPostIfCan(objectCategoryDefault: CategoryModelDTO, indexCache: Int) {
        if (isCategoryDefaultNull()) {
            val jsonCategory = convertCategoryToJson(objectCategoryDefault)
            handleCacheCategoryDefault(jsonCategory, indexCache)
        }
    }

    private fun convertCategoryToJson(category: CategoryModelDTO) =
        getContextScreen().convertObjectToJson(category)

    private fun isCategoryDefaultNull() = SharedUtils.jsonCategory == "{}"

    private fun handleCacheCategoryDefault(jsonCategory: String, indexCache: Int) {
        SharedUtils.jsonCategory = jsonCategory
        SharedUtils.indexCategory = indexCache
    }

    private fun initPostAdapterOne() {
        postAdapterOne = PostAdapterTypeOne(likedPost = { post, index ->
            likePost(post, index)
        }, commentPost = { post, index, comment ->
            commentPost(post, index, comment)
        }, sharePost = { _, _ ->
            toastMessageInComing()
        }, showDetailsPost = { post, index ->
            showDetailPost(post, index)
        })
    }

    private fun initDialogResponseLoading() {
        loadingDialog = LoadingResponseDialog(getContextScreen(), false)
    }

    private fun getContextScreen() = mBinding.root.context

    private fun setNewDataPost(newData: MutableList<PostModelDTO>) =
        postAdapterOne.submitData(newData)

    private fun showLoadingCategory() =
        mBinding.loadingCategory.visibleView()

    private fun hideLoadingCategory() =
        mBinding.loadingCategory.goneView()

    private fun showLoadingPostOne() =
        mBinding.loadingPost1.visibleView()

    private fun hideLoadingPostOne() =
        mBinding.loadingPost1.goneView()

    private fun showDialogResponse() = loadingDialog.show()

    private fun hideDialogResponse() = loadingDialog.cancel()

    private fun likePost(post: PostModelDTO, index: Int) =
        lifecycleScope.launch(Dispatchers.IO) {
            val defUpdate = viewModel.handleLikePost(post, getContextScreen())
            val flagAfterUpdate = defUpdate.await()

            defUpdate.cancel()
            when (flagAfterUpdate) {
                FLAG_REQUEST_API_TRUE ->
                    withContext(Dispatchers.Main) { refreshItemAtPositionRcv(index) }

                else -> withContext(Dispatchers.Main) { showMessageSomeThingWentWrong() }
            }
            // cancel coroutine
            cancel()
        }

    private fun showMessageSomeThingWentWrong() =
        getContextScreen().toastMessageRes(R.string.some_thing_went_wrong)

    private fun refreshItemAtPositionRcv(index: Int) =
        postAdapterOne.notifyItemChanged(index)

    private fun commentPost(post: PostModelDTO, index: Int, comment: String) {
        if (isCommentNull(comment)) {
            showMessageNullInput()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val defUpdate = viewModel.handleAddComment(post, comment, getContextScreen())
            val flagAfterUpdate = defUpdate.await()

            defUpdate.cancel()
            when (flagAfterUpdate) {
                FLAG_REQUEST_API_TRUE ->
                    withContext(Dispatchers.Main) {
                        hideInput()
                        refreshItemAtPositionRcv(index)
                    }

                else -> withContext(Dispatchers.Main) { showMessageSomeThingWentWrong() }
            }

            // cancel coroutine
            cancel()
        }
    }

    private fun isCommentNull(comment: String) = comment.isEmpty()

    private fun showMessageNullInput() =
        getContextScreen().toastMessageRes(R.string.this_field_cannot_be_left_blank)

    private fun hideInput() =
        getContextScreen().hideKeyboard(mBinding.root)

    private fun toastMessageInComing() =
        getContextScreen().toastMessageRes(R.string.this_feature_will_be_released_soon)

    private fun showDetailPost(post: PostModelDTO, index: Int) =
        lifecycleScope.launch(Dispatchers.IO) {
            val defUpdate = viewModel.updateViewPostPlusOne(post)
            val flagAfterUpdate = defUpdate.await()

            defUpdate.cancel()
            when (flagAfterUpdate) {
                FLAG_REQUEST_API_TRUE ->
                    withContext(Dispatchers.Main) {
                        handleShowDetailPost(post)
                        refreshItemAtPositionRcv(index)
                    }

                else -> withContext(Dispatchers.Main) { showMessageSomeThingWentWrong() }
            }
        }

    private fun handleShowDetailPost(post: PostModelDTO) {
        val bundle = getBundlePostDetail(post)
        startPostDetailActivity(bundle)
    }

    private fun getBundlePostDetail(post: PostModelDTO) =
        Bundle().apply {
            putSerializable(KEY_SHOW_POST_DETAIL, post)
        }

    private fun startPostDetailActivity(bundle: Bundle) =
        Routes.startPostDetailActivity(requireActivity(), bundle)
}
package com.social.media.blog.ltd.ui.screen.post.all

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.AppConst.FLAG_REQUEST_API_TRUE
import com.social.media.blog.ltd.commons.AppConst.KEY_CATEGORY_FILTER
import com.social.media.blog.ltd.commons.AppConst.KEY_SHOW_POST_DETAIL
import com.social.media.blog.ltd.commons.Routes
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.hideKeyboard
import com.social.media.blog.ltd.commons.extention.toastMessageRes
import com.social.media.blog.ltd.databinding.ActivityAllPostBinding
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.ui.adapter.rcv.PostAdapterTypeOne
import com.social.media.blog.ltd.ui.base.BaseActivity
import com.social.media.blog.ltd.ui.dialog.LoadingResponseDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllPostActivity : BaseActivity<ActivityAllPostBinding>() {

    private val viewModel: AllPostViewModel by viewModels()

    private var categoryFilter: CategoryModelDTO? = null

    private lateinit var listPostCache: MutableList<PostModelDTO>
    private lateinit var postAdapter: PostAdapterTypeOne
    private lateinit var dialogLoading: LoadingResponseDialog

    override fun inflateViewBinding(): ActivityAllPostBinding =
        ActivityAllPostBinding.inflate(layoutInflater)

    override fun initVariable() {
        getCategoryFilterFlag()
        initListPostCache()
        initPostAdapter()
        initDialogLoading()
    }

    override fun initView() = binding.apply {
        rcvPost.apply {
            adapter = postAdapter
            setHasFixedSize(true)
            clipToPadding = false
            setItemViewCacheSize(20)
        }
    }

    override fun fetchDataSource() = viewModel.apply {
        fetchAllPostFromApi()
    }

    override fun observerDataSource() = viewModel.apply {
        isLoading.observe(this@AllPostActivity) { loading ->
            if (loading) showDialogLoading() else hideDialogLoading()
        }

        listPost.observe(this@AllPostActivity) { posts ->
            submitDataToAdapter(posts, categoryFilter)
            cacheListPost(posts)
        }
    }

    override fun clickViews() = binding.apply {
        iconBack.click {
            removeEventListenerPosts()
            finish()
        }

        inputSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleFilterPostByName(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) = Unit
        })
    }

    private fun handleFilterPostByName(q: String) {
        val listAfterFilterByName =
            if (isKeySearchEmpty(q)) listPostCache
            else listPostCache.filter { it.title.contains(q, ignoreCase = true) }.toMutableList()
        submitDataRcvAfterSearchKey(listAfterFilterByName)
    }

    private fun isKeySearchEmpty(value: String) = value.isEmpty()

    private fun getCategoryFilterFlag() {
        categoryFilter = intent.extras?.getSerializable(KEY_CATEGORY_FILTER) as CategoryModelDTO?
    }

    private fun initListPostCache() {
        listPostCache = mutableListOf()
    }

    private fun showDialogLoading() = dialogLoading.show()

    private fun hideDialogLoading() = dialogLoading.cancel()

    private fun initPostAdapter() {
        postAdapter = PostAdapterTypeOne(likedPost = { post, index ->
            likePost(post, index)
        }, commentPost = { post, index, comment ->
            commentPost(post, index, comment)
        }, sharePost = { _, _ ->
            toastMessageInComing()
        }, showDetailsPost = { post, index ->
            showDetailPost(post, index)
        })
    }

    private fun removeEventListenerPosts() = viewModel.removeEventListenerPosts()

    private fun submitDataToAdapter(posts: MutableList<PostModelDTO>, categoryFlag: CategoryModelDTO?) {
        if (isFilterMode(categoryFlag)) submitDataAfterFilter(posts, categoryFlag!!)
        else postAdapter.submitData(posts)
    }

    private fun isFilterMode(categoryFlag: CategoryModelDTO?) =
        categoryFlag != null

    private fun submitDataAfterFilter(posts: MutableList<PostModelDTO>, category: CategoryModelDTO) {
        val listAfterFilterByCategory =
            getListPostFilter(category, posts)
        postAdapter.submitData(listAfterFilterByCategory)
    }

    private fun getListPostFilter(category: CategoryModelDTO, posts: MutableList<PostModelDTO>) =
        posts.filter { it.category.id == category.id }.toMutableList()

    private fun toastMessageInComing() =
        toastMessageRes(R.string.this_feature_will_be_released_soon)

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

    private fun showMessageSomeThingWentWrong() =
        toastMessageRes(R.string.some_thing_went_wrong)

    private fun handleShowDetailPost(post: PostModelDTO) {
        val bundle = getBundlePostDetail(post)
        startPostDetailActivity(bundle)
    }

    private fun startPostDetailActivity(bundle: Bundle) =
        Routes.startPostDetailActivity(this, bundle)

    private fun getBundlePostDetail(post: PostModelDTO) =
        Bundle().apply {
            putSerializable(KEY_SHOW_POST_DETAIL, post)
        }

    private fun refreshItemAtPositionRcv(index: Int) =
        postAdapter.notifyItemChanged(index)

    private fun commentPost(post: PostModelDTO, index: Int, comment: String) {
        if (isCommentNull(comment)) {
            showMessageNullInput()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val defUpdate = viewModel.handleAddComment(post, comment, this@AllPostActivity)
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
        toastMessageRes(R.string.this_field_cannot_be_left_blank)

    private fun hideInput() =
        hideKeyboard(binding.root)

    private fun likePost(post: PostModelDTO, index: Int) =
        lifecycleScope.launch(Dispatchers.IO) {
            val defUpdate = viewModel.handleLikePost(post, this@AllPostActivity)
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

    private fun initDialogLoading() {
        dialogLoading = LoadingResponseDialog(this, false)
    }

    private fun submitDataRcvAfterSearchKey(list: MutableList<PostModelDTO>) =
        postAdapter.submitData(list)

    private fun cacheListPost(list: MutableList<PostModelDTO>) {
        listPostCache = list
    }
}
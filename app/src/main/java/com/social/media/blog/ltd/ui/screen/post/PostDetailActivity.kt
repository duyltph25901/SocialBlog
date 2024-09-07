package com.social.media.blog.ltd.ui.screen.post

import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.AppConst.FLAG_REQUEST_API_TRUE
import com.social.media.blog.ltd.commons.extention.click
import com.social.media.blog.ltd.commons.extention.formatCurrentTimeTo_dd_MM_yyyy
import com.social.media.blog.ltd.commons.extention.toastMessageRes
import com.social.media.blog.ltd.databinding.ActivityPostDetailBinding
import com.social.media.blog.ltd.model.domain.PostModelDomain
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.ui.adapter.rcv.CommentAdapter
import com.social.media.blog.ltd.ui.base.BaseActivity
import com.social.media.blog.ltd.ui.dialog.LoadingResponseDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostDetailActivity : BaseActivity<ActivityPostDetailBinding>() {

    private val viewModel: PostDetailViewModel by viewModels()

    private lateinit var dialogLoading: LoadingResponseDialog
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var postModelDomain: PostModelDomain

    override fun inflateViewBinding(): ActivityPostDetailBinding =
        ActivityPostDetailBinding.inflate(layoutInflater)

    override fun initVariable() {
        initCommentAdapter()
        initDialogLoading()
    }

    override fun initView() {
        getValuePostModelDtoAndDomain()
    }

    override fun observerDataSource() = viewModel.apply {
        postModelDomain.observe(this@PostDetailActivity) { post ->
            showUiPost(post)
            this@PostDetailActivity.postModelDomain = post
        }

        isLoading.observe(this@PostDetailActivity) { loading ->
            if (loading) this@PostDetailActivity.showLoading() else this@PostDetailActivity.hideLoading()
        }
    }

    override fun clickViews() = binding.apply {
        icBack.click { finish() }
        icFavorite.click { likeOrUnLike(getIdUserCurrent()) }
        inputComment.addTextChangedListener(getTextWatcher())
        iconSend.click { eventSendComment() }
    }

    private fun initDialogLoading() {
        dialogLoading = LoadingResponseDialog(this, false)
    }

    private fun showLoading() = dialogLoading.show()

    private fun hideLoading() = dialogLoading.cancel()

    private fun showUiPost(post: PostModelDomain) = binding.apply {
        textTitlePost.text = post.post.title
        textCategoryName.text = post.post.category.name
        textDateTime.text = getDateTimeByFormatAlreadyExists(post.post.createdAt)
        Glide.with(this@PostDetailActivity).load(post.author.avatar).into(imageAvatarUser)
        textUserName.text = post.author.userName
        Glide.with(this@PostDetailActivity).load(post.post.linkMedia).into(imagePost)
        textContentPost.text = post.post.content
        textViews.text =
            if (isViewsHigherThanOne(post)) "${getViewsOfPost(post)} ${getTextViewsRes()}" else getTextTextOneViewRes()
        icFavorite.isActivated =
            post.post.listIdUserLiked.find { it == getIdUserCurrent() } != null

        submitListCommentAdapter(post.listComment)
    }

    private fun getDateTimeByFormatAlreadyExists(current: Long) =
        formatCurrentTimeTo_dd_MM_yyyy(current)

    private fun initCommentAdapter() = binding.rcvComment.apply {
        commentAdapter = CommentAdapter()

        adapter = commentAdapter
        setHasFixedSize(false)
        setItemViewCacheSize(20)
    }

    private fun getValuePostModelDtoAndDomain() =
        viewModel.getValuePostModelDtoAndDomain(intent = intent)

    private fun submitListCommentAdapter(list: MutableList<PostModelDomain.CommentModelDomain>) =
        commentAdapter.submitData(list)

    private fun convertJsonToUserModelDto(json: String) =
        viewModel.convertJsonToUserModelDto(json, this)

    private fun getJsonUserCurrent() = viewModel.getJsonUserCurrent()

    private fun getIdUserCurrent(): String {
        val json = getJsonUserCurrent()
        val userCurrent = convertJsonToUserModelDto(json)

        return userCurrent.idUser
    }

    private fun getTextViewsRes() = getString(R.string.views)

    private fun getTextTextOneViewRes() = getString(R.string.one_view)

    private fun isViewsHigherThanOne(post: PostModelDomain) =
        post.post.views > 1

    private fun getViewsOfPost(post: PostModelDomain) = post.post.views

    private fun likeOrUnLike(idUser: String) =
        lifecycleScope.launch(Dispatchers.IO) {
            val postModelDto = getPostModelDto()
            val isIdUserAlreadyExist = isIdUserAlreadyExist(postModelDto.listIdUserLiked, idUser)
            if (isIdUserAlreadyExist) postModelDto.listIdUserLiked.remove(idUser)
            else postModelDto.listIdUserLiked.add(idUser)
            postValueShowLoadingVm()
            val responseAfterUpdateDb = updateDatabaseReturnResponse(postModelDto)
            when (responseAfterUpdateDb) {
                FLAG_REQUEST_API_TRUE -> onResponseLikeSuccess(idUser, isIdUserAlreadyExist)

                else -> showMessageWrong()
            }
            postValueHideLoadingVm()
            cancel()
        }

    private fun showMessageWrong() =
        toastMessageRes(R.string.some_thing_went_wrong)

    private fun getPostModelDto() = postModelDomain.post

    private suspend fun updateDatabaseReturnResponse(post: PostModelDTO) =
        viewModel.updateDatabaseReturnResponse(post)

    private fun postValueShowLoadingVm() = viewModel.showLoading()

    private fun postValueHideLoadingVm() = viewModel.hideLoading()

    private fun onResponseLikeSuccess(id: String, isUnLike: Boolean) {
        val postRefresh = addOrRemoveIdToListLiked(id, isUnLike)
        refreshPostModelDomain(postRefresh)
    }

    private fun refreshPostModelDomain(post: PostModelDomain) =
        viewModel.refreshPostModelDomain(post)

    private fun addOrRemoveIdToListLiked(id: String, isUnLike: Boolean): PostModelDomain {
        val postTemp = postModelDomain
        postTemp.post.listIdUserLiked.apply {
            if (isUnLike) remove(id)
            else add(id)
        }
        return postTemp
    }

    private fun isIdUserAlreadyExist(list: MutableList<String>, id: String) =
        list.find { it == id } != null

    private fun isInputNull(q: CharSequence?) =
        q?.isNotEmpty() ?: true

    private fun refreshIconSend(isNull: Boolean) = binding.iconSend.apply {
        isActivated = isNull
        isClickable = isNull
    }

    private fun getTextWatcher() =
        object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                refreshIconSend(isInputNull(p0))
            }

            override fun afterTextChanged(p0: Editable?) = Unit
        }

    private fun eventSendComment() {
        val comment = binding.inputComment.text.toString()
        if (comment.isEmpty()) return
        lifecycleScope.launch {
            val defUpdate =
                viewModel.handleAddComment(getPostModelDto(), comment, this@PostDetailActivity)
            val flagAfterUpdate = defUpdate.await()
            defUpdate.cancel()

            when (flagAfterUpdate) {
                FLAG_REQUEST_API_TRUE -> onResponseCommentSuccess()

                else -> showMessageWrong()
            }

            cancel()
        }
    }

    private fun onResponseCommentSuccess() =
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.refreshPostModelDomainAfterComment(postModelDomain)
            withContext(Dispatchers.Main) { binding.inputComment.text.clear() }
        }
}
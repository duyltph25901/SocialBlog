package com.social.media.blog.ltd.ui.screen.post.detail

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
import kotlinx.coroutines.Deferred
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
        getValuePostModelDomain()
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
        textLikes.text =
            if (isLikesHigherThanOne(post)) "${getLikesOfPost(post)} ${getTextLikesRes()}" else getTextLike(post.post.listIdUserLiked.size)
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

    private fun getValuePostModelDomain() =
        viewModel.getValuePostModelDomain(intent = intent)

    private fun submitListCommentAdapter(list: MutableList<PostModelDomain.CommentModelDomain>) =
        commentAdapter.submitData(list)

    private fun convertJsonToUserModelDto(json: String) =
        viewModel.convertJsonToUserModelDto(json, this)

    private fun getJsonUserCurrent() = viewModel.getJsonUserCurrent()

    private fun getIdUserCurrent(): String = getUserCurrent().idUser

    private fun getUserCurrent() =
        convertJsonToUserModelDto(getJsonUserCurrent())

    private fun getTextViewsRes() = getString(R.string.views)

    private fun getTextTextOneViewRes() = getString(R.string.one_view)

    private fun getTextLikesRes() = getString(R.string.likes)

    private fun getTextLike(like: Int) = getString(R.string.like, like)

    private fun isViewsHigherThanOne(post: PostModelDomain) =
        post.post.views > 1

    private fun isLikesHigherThanOne(post: PostModelDomain) =
        post.post.listIdUserLiked.size > 1

    private fun getViewsOfPost(post: PostModelDomain) = post.post.views

    private fun getLikesOfPost(post: PostModelDomain) = post.post.listIdUserLiked.size

    private fun likeOrUnLike(idUser: String) =
        lifecycleScope.launch(Dispatchers.IO) {
            postValueShowLoadingVm()
            val postModelDto = getPostModelDtoFromPostModelDomain()
            val isIdUserAlreadyExist = isIdUserAlreadyExist(postModelDto.listIdUserLiked, idUser)
            if (isIdUserAlreadyExist) postModelDto.listIdUserLiked.remove(idUser)
            else postModelDto.listIdUserLiked.add(idUser)
            val responseAfterUpdateDb = updateDatabaseReturnResponse(postModelDto)
            when (responseAfterUpdateDb) {
                FLAG_REQUEST_API_TRUE -> onResponseLikeOrUnLikeSuccess(postModelDto) //refreshPostModelDomain(postModelDomain)

                else -> showMessageWrong()
            }
            postValueHideLoadingVm()
            cancel()
        }

    private fun getListIdUserLikedFromPostModelDomain() =
        postModelDomain.post.listIdUserLiked

    private suspend fun onResponseLikeOrUnLikeSuccess(postModelDto: PostModelDTO) {
        refreshPostModelDomainLikeOrUnLike(postModelDto)
        val listIdUserLiked =
            getListIdUserLikedFromPostModelDomain()
        changeStateIconFavorite(listIdUserLiked)
        withContext(Dispatchers.Main) { changeTextLikes(listIdUserLiked) }
    }

    private fun refreshPostModelDomainLikeOrUnLike(postModelDto: PostModelDTO) =
        viewModel.refreshPostModelDomainLikeOrUnLike(postModelDto)

    private fun changeStateIconFavorite(listId: MutableList<String>) {
        val isUserLiked = listId.find { it == getIdUserCurrent() } != null
        binding.icFavorite.isActivated = isUserLiked
    }

    private fun changeTextLikes(list: MutableList<String>) {
        binding.textLikes.text =
            if (list.size > 1) "${list.size} ${getTextLikesRes()}" else getTextLike(list.size)
    }

    private fun showMessageWrong() =
        toastMessageRes(R.string.some_thing_went_wrong)

    private fun getPostModelDtoFromPostModelDomain() = postModelDomain.post

    private suspend fun updateDatabaseReturnResponse(post: PostModelDTO) =
        viewModel.updateDatabaseReturnResponse(post)

    private fun postValueShowLoadingVm() = viewModel.showLoading()

    private fun postValueHideLoadingVm() = viewModel.hideLoading()

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
        val comment = getCommentFromInput()
        if (isCommentNotNull(comment)) handleSendComment(comment)
    }

    private fun getCommentFromInput() = binding.inputComment.text.toString()

    private fun isCommentNotNull(comment: String) = comment.isNotEmpty()

    private fun handleSendComment(comment: String) =
        lifecycleScope.launch {
            val defUpdate = getDeferredComment(comment)
            val flagAfterUpdate = getValueFromDeferred(defUpdate)
            cancelDeferred(defUpdate)
            when (flagAfterUpdate) {
                FLAG_REQUEST_API_TRUE -> {
                    val commentModelDto = getCommentModelDtoAfterComment()
                    val commentModelDomain = getCommentModelDomainAfterComment(commentModelDto)
                    onResponseCommentSuccess(commentModelDomain)
                }

                else -> showMessageWrong()
            }

            cancel()
        }

    private suspend fun getValueFromDeferred(def: Deferred<Int>) = def.await()

    private fun cancelDeferred(def: Deferred<Int>) = def.cancel()

    private fun getCommentModelDtoAfterComment() =
        getListCommentFromPostModelDomain()[getLastIndexListComment()]

    private fun getListCommentFromPostModelDomain() =
        postModelDomain.post.listComments

    private fun getLastIndexListComment() = getListCommentFromPostModelDomain().size - 1

    private fun getDeferredComment(comment: String) =
        viewModel.handleAddComment(getPostModelDtoFromPostModelDomain(), comment, this@PostDetailActivity)

    private fun getCommentModelDomainAfterComment(comment: PostModelDTO.Comment) =
        PostModelDomain.CommentModelDomain(
            comment = comment,
            userName = getUserCurrent().userName,
            avatarUser = getUserCurrent().avatar
        )

    private fun onResponseCommentSuccess(comment: PostModelDomain.CommentModelDomain) {
        refreshPostModelDomainComment(comment)
        clearInputComment()
        showCommentAfterPush(comment)
    }

    private fun refreshPostModelDomainComment(comment: PostModelDomain.CommentModelDomain) =
        viewModel.refreshPostModelDomainComment(comment)

    private fun clearInputComment() =
        binding.inputComment.text.clear()

    private fun showCommentAfterPush(comment: PostModelDomain.CommentModelDomain) =
        commentAdapter.addToFirstListComment(comment)

}
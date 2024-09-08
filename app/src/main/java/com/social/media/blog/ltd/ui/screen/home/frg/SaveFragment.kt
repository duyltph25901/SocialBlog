package com.social.media.blog.ltd.ui.screen.home.frg

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.AppConst.KEY_SHOW_POST_DETAIL
import com.social.media.blog.ltd.commons.Routes
import com.social.media.blog.ltd.commons.extention.convertJsonToObject
import com.social.media.blog.ltd.database.AppDatabase
import com.social.media.blog.ltd.database.repository.PostRepository
import com.social.media.blog.ltd.databinding.FragmentSaveBinding
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.model.dto.PostModelDTO
import com.social.media.blog.ltd.model.entities.PostSaveEntity
import com.social.media.blog.ltd.ui.adapter.rcv.PostSaveAdapter
import com.social.media.blog.ltd.ui.base.BaseFragment
import com.social.media.blog.ltd.ui.screen.home.frg.vm.SaveViewModel
import com.social.media.blog.ltd.ui.screen.home.frg.vm.ViewModelSaveFactory

class SaveFragment : BaseFragment<FragmentSaveBinding>() {

    private lateinit var postSaveAdapter: PostSaveAdapter
    private lateinit var viewModel: SaveViewModel
    private lateinit var listPostCache: MutableList<PostSaveEntity>

    override fun getLayoutFragment(): Int = R.layout.fragment_save

    override fun initVariables() {
        initViewModel()
        initAdapter()
        initListPostCache()
    }

    override fun initViews() {
        mBinding.apply {
            rcv.apply {
                adapter = postSaveAdapter
                setItemViewCacheSize(20)
                setHasFixedSize(true)
            }
        }
    }

    override fun onClickViews() {
        mBinding.apply {
            inputSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    handleFilterPostByName(p0.toString())
                }

                override fun afterTextChanged(p0: Editable?) = Unit
            })
        }
    }

    override fun observerData() {
        viewModel.apply {
            listPost.observe(viewLifecycleOwner) { listData ->
                submitData(listData)
                cacheListPost(listData)
            }
        }
    }

    private fun getScreenContext() = mBinding.root.context

    private fun initViewModel() {
        val dao = AppDatabase.getInstance(getScreenContext()).postDao()
        val repository = PostRepository(dao)
        val viewModelFactory = ViewModelSaveFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[SaveViewModel::class.java]
    }

    private fun initAdapter() {
        postSaveAdapter = PostSaveAdapter { postEntity, _ ->
            val postModelDto = convertPostEntityToPostModelDto(postEntity)
            val bundle = getBundlePostDetail(postModelDto)
            startPostDetailActivity(bundle)
        }
    }

    private fun initListPostCache() {
        listPostCache = mutableListOf()
    }

    private fun submitData(post: MutableList<PostSaveEntity>) =
        postSaveAdapter.submitData(post)

    private fun handleFilterPostByName(q: String) {
        val listAfterFilterByName =
            if (isKeySearchEmpty(q)) listPostCache
            else listPostCache.filter { it.title.contains(q, ignoreCase = true) }.toMutableList()
        submitData(listAfterFilterByName)
    }

    private fun isKeySearchEmpty(value: String) = value.isEmpty()

    private fun cacheListPost(list: MutableList<PostSaveEntity>) {
        listPostCache = list
    }

    private fun convertPostEntityToPostModelDto(postEntity: PostSaveEntity) =
        PostModelDTO(
            id = postEntity.id,
            idAuthor = postEntity.idAuthor,
            title = postEntity.title,
            content = postEntity.content,
            linkMedia = postEntity.linkMedia,
            createdAt = postEntity.createdAt,
            updateAt = postEntity.updateAt,
            views = postEntity.views,
            isPublic = postEntity.isPublic,
            category = convertJsonToObject(postEntity.category, CategoryModelDTO::class.java),
            mediaLocation = postEntity.mediaLocation,
            listIdUserLiked = convertStringToListIdUserLike(postEntity.listIdUserLiked),
            listComments = convertStringToListComment(postEntity.listComments)
        )

    private fun getBundlePostDetail(post: PostModelDTO) =
        Bundle().apply {
            putSerializable(KEY_SHOW_POST_DETAIL, post)
        }

    private fun startPostDetailActivity(bundle: Bundle) =
        Routes.startPostDetailActivity(requireActivity(), bundle)

    private fun <T> convertJsonToObject(json: String, classOfJson: Class<T>): T =
        getScreenContext().convertJsonToObject(json, classOfJson)

    private fun convertStringToListIdUserLike(str: String): MutableList<String> =
        if (str.isEmpty()) mutableListOf()
        else str.split(",")
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()

    private fun convertStringToListComment(str: String): MutableList<PostModelDTO.Comment> {
        val listComments: MutableList<PostModelDTO.Comment> = mutableListOf()
        val arrTemp = str.split("|||")

        arrTemp.forEach { commentJsonType ->
            if (commentJsonType.isNotEmpty()) {
                val comment = convertJsonToObject(commentJsonType, PostModelDTO.Comment::class.java)
                listComments.add(comment)
            }
        }

        return listComments
    }


}
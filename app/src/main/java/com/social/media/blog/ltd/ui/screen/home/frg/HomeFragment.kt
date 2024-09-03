package com.social.media.blog.ltd.ui.screen.home.frg

import androidx.fragment.app.viewModels
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.commons.SharedUtils
import com.social.media.blog.ltd.commons.extention.convertObjectToJson
import com.social.media.blog.ltd.commons.extention.goneView
import com.social.media.blog.ltd.commons.extention.visibleView
import com.social.media.blog.ltd.databinding.FragmentHomeBinding
import com.social.media.blog.ltd.model.dto.CategoryModelDTO
import com.social.media.blog.ltd.ui.adapter.rcv.CategoryAdapter
import com.social.media.blog.ltd.ui.base.BaseFragment
import com.social.media.blog.ltd.ui.screen.home.frg.vm.HomeViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter

    override fun getLayoutFragment(): Int = R.layout.fragment_home

    override fun initVariables() {
        initCategoryAdapter()
    }

    override fun initViews() {
        mBinding.apply {
            rcvCategory.apply {
                adapter = categoryAdapter
                setHasFixedSize(true)
                clipToPadding = false
                setItemViewCacheSize(20)
            }
        }
    }

    override fun fetchDataSrc() {
        viewModel.apply {
            fetchCategories()
        }
    }

    override fun observerData() {
        viewModel.apply {
            isLoadingCategory.observe(viewLifecycleOwner) { loading ->
                if (loading) mBinding.loadingCategory.visibleView() else mBinding.loadingCategory.goneView()
            }

            categories.observe(viewLifecycleOwner) { listCategories ->
                setNewDataCategory(listCategories)
                cacheCategoryPostIfCan(listCategories[0], 0)
            }
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
        mBinding.root.context.convertObjectToJson(category)
    
    private fun isCategoryDefaultNull() = SharedUtils.jsonCategory == "{}"
    
    private fun handleCacheCategoryDefault(jsonCategory: String, indexCache: Int) {
        SharedUtils.jsonCategory = jsonCategory
        SharedUtils.indexCategory = indexCache
    }
}
package com.social.media.blog.ltd.ui.screen.intro

import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.media.blog.ltd.R
import com.social.media.blog.ltd.model.domain.IntroModelDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntroViewModel : ViewModel() {

    private val _introList = MutableLiveData<MutableList<IntroModelDomain>>()

    val introList: LiveData<MutableList<IntroModelDomain>> = _introList

    fun fetchDataIntro() = viewModelScope.launch(Dispatchers.IO) {
        _introList.postValue(mutableListOf<IntroModelDomain>().apply {
            add(IntroModelDomain(R.drawable.img_ob_1, R.string.title_ob_1))
            add(IntroModelDomain(R.drawable.img_ob_2, R.string.title_ob_2))
            add(IntroModelDomain(R.drawable.img_ob_3, R.string.title_ob_3))
            add(IntroModelDomain(R.drawable.img_ob_4, R.string.title_ob_4))
        })
    }

    fun updateIndicatorView(indexSelected: Int, indicators: MutableList<ImageView>) =
        indicators.forEachIndexed { index, imageView ->
            imageView.setImageResource(if (index == indexSelected) R.drawable.ic_ob_selected else R.drawable.ic_ob_un_selected)
        }

}
package com.social.media.blog.ltd.ui.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.social.media.blog.ltd.commons.NetWorkUtils

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var binding: VB

    abstract fun inflateViewBinding(): VB
    open fun initVariable(): Any? = null
    open fun initView(): Any? = null
    open fun clickViews(): Any? = null
    open fun fetchDataSource(): Any? = null
    open fun observerDataSource(): Any? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.apply {
            val background: Drawable = ColorDrawable(Color.parseColor("#F9FCFF"))
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = resources.getColor(android.R.color.transparent)
            setBackgroundDrawable(background)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            val uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            decorView.systemUiVisibility = uiOptions
        }

        binding = inflateViewBinding()
        setContentView(binding.root)
        initVariable()
        initView()
        fetchDataSource()
        clickViews()
        observerDataSource()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            fullScreenImmersive(window)
        }
    }

    open fun fullScreenImmersive(window: Window?) {
        if (window != null) {
            fullScreenImmersive(window.decorView)
        }
    }

    open fun fullScreenImmersive(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = uiOptions
        } else {
            // Gọi phương thức hỗ trợ cho phiên bản Android thấp hơn ở đây
            setLegacyFullScreen(view)
        }
    }

    private fun setLegacyFullScreen(view: View) {
        val uiOptions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // Đối với các phiên bản Android thấp hơn, sử dụng các cờ hệ thống cũ hơn
            View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            // Đối với các phiên bản Android KitKat (API 19) và mới hơn nhưng thấp hơn Android M
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE
        }
        view.systemUiVisibility = uiOptions
    }


    fun isNetworkAvailable(context: Context): Boolean = NetWorkUtils.isNetworkAvailable(baseContext)

}
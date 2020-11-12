package com.lollipop.now.ui

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * @author lollipop
 * @date 11/12/20 17:36
 */
open class BaseActivity: AppCompatActivity() {

    protected fun initWindowFlag() {
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
        var viewFlag = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewFlag = (viewFlag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewFlag = (viewFlag or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
        window.decorView.systemUiVisibility = viewFlag

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    protected fun initRootGroup(group: View) {
        group.fitsSystemWindows = true
        group.setOnApplyWindowInsetsListener { _, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val systemInsets = insets.getInsets(WindowInsets.Type.systemBars())
                onWindowInsetsChange(
                    systemInsets.left, systemInsets.top,
                    systemInsets.right, systemInsets.bottom
                )
                WindowInsets.CONSUMED
            } else {
                onWindowInsetsChange(
                    insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom
                )
                insets.consumeSystemWindowInsets()
            }
        }
    }

    protected open fun onWindowInsetsChange(left: Int, top: Int, right: Int, bottom: Int) {

    }

}
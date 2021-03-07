package com.lollipop.now.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewPropertyAnimator
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * @author lollipop
 * @date 2019-07-23 21:58
 * 揭示动画辅助类
 */

object AnimatorHelper {

    /**
     * 通过勾股定理
     * 计算一个矩形的对角线
     */
    fun getDiam(width:Int,height:Int):Float{
        return sqrt(1.0*width*width+height*height).toFloat()
    }

    /**
     * 默认的动画时长
     */
    var animatorDuration = 300L

}

/**
 * 一个简化后的动画监听类
 * 它主要用于某些时候，只需要监听某个状态的场景
 */
class AnimatorListenerImpl: AnimatorListenerAdapter() {

    var cancelListener: ((Animator?) -> Unit)? = null
    var repeatListener: ((Animator?) -> Unit)? = null
    var startListener: ((Animator?) -> Unit)? = null
    var pauseListener: ((Animator?) -> Unit)? = null
    var resumeListener: ((Animator?) -> Unit)? = null
    var endListener: ((Animator?) -> Unit)? = null

    override fun onAnimationCancel(animation: Animator?) {
        cancelListener?.invoke(animation)
    }

    override fun onAnimationRepeat(animation: Animator?) {
        repeatListener?.invoke(animation)
    }

    override fun onAnimationStart(animation: Animator?) {
        startListener?.invoke(animation)
    }

    override fun onAnimationPause(animation: Animator?) {
        pauseListener?.invoke(animation)
    }

    override fun onAnimationResume(animation: Animator?) {
        resumeListener?.invoke(animation)
    }

    override fun onAnimationEnd(animation: Animator?) {
        super.onAnimationEnd(animation)
        endListener?.invoke(animation)
    }

    fun removeThis(animation: Animator?) {
        animation?.removeListener(this)
    }
}

fun AnimatorListenerImpl.onCancel(run: (Animator?) -> Unit) {
    this.cancelListener = run
}

fun AnimatorListenerImpl.onRepeat(run: (Animator?) -> Unit) {
    this.repeatListener = run
}

fun AnimatorListenerImpl.onStart(run: (Animator?) -> Unit) {
    this.startListener = run
}

fun AnimatorListenerImpl.onPause(run: (Animator?) -> Unit) {
    this.pauseListener = run
}

fun AnimatorListenerImpl.onResume(run: (Animator?) -> Unit) {
    this.resumeListener = run
}

fun AnimatorListenerImpl.onEnd(run: (Animator?) -> Unit) {
    this.endListener = run
}

/**
 * 对一个动画对象绑定状态监听器
 */
fun Animator.lifecycleBinding(listener: (AnimatorListenerImpl.() -> Unit)?): Animator {
    listener?:return this
    this.addListener(AnimatorListenerImpl().apply { listener.invoke(this) })
    return this
}

/**
 * 对一个动画对象绑定状态监听器
 */
fun ViewPropertyAnimator.lifecycleBinding(listener: (AnimatorListenerImpl.() -> Unit)): ViewPropertyAnimator {
    this.setListener(AnimatorListenerImpl().apply { listener.invoke(this) })
    return this
}

/**
 * 对一个View使用展开的揭示动画，它会要求提供一个锚点
 * 动画会以锚点为中心来展开自身
 */
fun View.revealOpenWith(anchorView: View, listener: (AnimatorListenerImpl.() -> Unit)? = null): Animator {
    val myLoc = IntArray(2)
    this.getLocationInWindow(myLoc)
    val anchorLoc = IntArray(2)
    anchorView.getLocationInWindow(anchorLoc)

    val centerX = min(max(anchorLoc[0] + anchorView.width / 2 - myLoc[0], 0), this.width)
    val centerY = min(max(anchorLoc[1] + anchorView.height / 2 - myLoc[1], 0), this.height)
    val endRadius = AnimatorHelper.getDiam(this.width, this.height)
    val reveal = ViewAnimationUtils.createCircularReveal(this, centerX, centerY, 0F,endRadius)
    reveal.lifecycleBinding(listener)
    reveal.duration = AnimatorHelper.animatorDuration
    reveal.start()
    return reveal
}

/**
 * 对一个View使用关闭的揭示动画，它会要求提供一个锚点
 * 动画会以锚点为中心来关闭自身
 */
fun View.revealCloseWith(anchorView: View, listener: (AnimatorListenerImpl.() -> Unit)? = null): Animator {
    val myLoc = IntArray(2)
    this.getLocationInWindow(myLoc)
    val anchorLoc = IntArray(2)
    anchorView.getLocationInWindow(anchorLoc)

    val centerX = min(max(anchorLoc[0] + anchorView.width / 2 - myLoc[0], 0), this.width)
    val centerY = min(max(anchorLoc[1] + anchorView.height / 2 - myLoc[1], 0), this.height)
    val endRadius = AnimatorHelper.getDiam(this.width, this.height)
    val reveal = ViewAnimationUtils.createCircularReveal(this, centerX, centerY, endRadius, 0F)
    reveal.lifecycleBinding(listener)
    reveal.duration = AnimatorHelper.animatorDuration
    reveal.start()
    return reveal
}
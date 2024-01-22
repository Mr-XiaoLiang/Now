package com.lollipop.now.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.lollipop.base.util.closeBoard
import com.lollipop.now.data.SiteInfo

/**
 * @author lollipop
 * @date 11/11/20 18:14
 * 编辑用的dialog
 */

class EditDialog(
    private val dialogRoot: View,
    private val backgroundView: View,
    private val contentGroup: View,
    private val iconView: TextView,
    private val titleInput: EditText,
    private val urlInput: EditText,
    submitBtn: View,
    cancelBtn: View,
    private val onSubmitCallback: (String, String, Int) -> Unit
) :
    ValueAnimator.AnimatorUpdateListener,
    Animator.AnimatorListener {

    companion object {
        private const val DURATION = 300L
    }

    private var progress = 0F
    private var pullCurtain = false
    private val valueAnimator = ValueAnimator()
    private var tag = -1

    private var statusListener: ((Boolean) -> Unit)? = null

    init {
        valueAnimator.addUpdateListener(this)
        valueAnimator.addListener(this)
        backgroundView.setOnTouchListener(EmptyTouchCallback())
        dialogRoot.visibility = View.INVISIBLE

        titleInput.doOnTextChanged { text, _, _, _ ->
            iconView.text = text ?: ""
        }

        submitBtn.setOnClickListener {
            submit()
        }
        cancelBtn.setOnClickListener {
            dismiss()
        }
    }

    fun setShownStatusChangeListener(listener: (Boolean) -> Unit) {
        this.statusListener = listener
    }

    val isShown: Boolean
        get() {
            return pullCurtain && dialogRoot.isShown
        }

    private fun submit() {
        val name = titleInput.text?.toString() ?: ""
        val url = urlInput.text?.toString() ?: ""
        onSubmitCallback(name, url, tag)
        dismiss()
    }

    fun dismiss() {
        statusListener?.invoke(false)
        if (!dialogRoot.isAttachedToWindow
            || dialogRoot.parent == null
            || !dialogRoot.isShown
        ) {
            return
        }
        titleInput.closeBoard()
        doAnimation(false)
    }

    fun show(info: SiteInfo, tag: Int) {
        statusListener?.invoke(true)
        this.tag = tag
        iconView.text = info.name
        titleInput.setText(info.name)
        urlInput.setText(info.url)
        dialogRoot.post {
            doAnimation(true)
        }
    }

    private fun doAnimation(open: Boolean) {
        pullCurtain = open
        valueAnimator.cancel()
        val endValue = if (open) {
            1F
        } else {
            0F
        }
        valueAnimator.duration = if (open) {
            (endValue - progress) * DURATION
        } else {
            (progress - endValue) * DURATION
        }.toLong()
        valueAnimator.setFloatValues(progress, endValue)
        valueAnimator.start()
    }

    private fun onProgressChange() {
        if (progress > 1F) {
            progress = 1F
        } else if (progress < 0F) {
            progress = 0F
        }
        backgroundView.alpha = progress
        contentGroup.translationY = (contentGroup.top + contentGroup.height) * (progress - 1)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        if (animation == valueAnimator) {
            progress = animation.animatedValue as Float
            onProgressChange()
        }
    }

    override fun onAnimationRepeat(animation: Animator) {}

    override fun onAnimationEnd(animation: Animator) {
        if (!pullCurtain) {
            dialogRoot.visibility = View.INVISIBLE
        }
    }

    override fun onAnimationCancel(animation: Animator) {}

    override fun onAnimationStart(animation: Animator) {
        if (pullCurtain) {
            dialogRoot.visibility = View.VISIBLE
        }
    }

    private class EmptyTouchCallback : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            return true
        }
    }

}
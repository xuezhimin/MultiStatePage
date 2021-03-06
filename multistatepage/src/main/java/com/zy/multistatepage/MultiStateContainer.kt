package com.zy.multistatepage

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.zy.multistatepage.state.SuccessState

/**
 * @ProjectName: MultiStatePage
 * @Author: 赵岩
 * @Email: 17635289240@163.com
 * @Description: TODO
 * @CreateDate: 2020/9/17 11:54
 */
@SuppressLint("ViewConstructor")
class MultiStateContainer(
    context: Context,
    val originTargetView: View,
    val retryListener: (multiStateContainer: MultiStateContainer) -> Unit
) : FrameLayout(context) {

    var animator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
        duration = 500
    }

    fun <T : MultiState> show(clazz: Class<T>, notify: (T) -> Unit = {}) {
        MultiStatePage.getDefault()[clazz]?.let { multiState ->
            removeAllViews()
            if (multiState is SuccessState) {
                addView(originTargetView)
                originTargetView.doAnimator()
                val targetViewLayoutParams = originTargetView.layoutParams
                if (targetViewLayoutParams is ViewGroup.MarginLayoutParams) {
                    targetViewLayoutParams.setMargins(0, 0, 0, 0)
                    originTargetView.layoutParams = targetViewLayoutParams
                }
            } else {
                val view =
                    multiState.onCreateMultiStateView(context, LayoutInflater.from(context), this)
                multiState.onMultiStateViewCreate(view)
                if (multiState.enableReload()) {
                    view.setOnClickListener {
                        retryListener.invoke(this)
                    }
                }
                addView(view)
                view.doAnimator()
                notify.invoke(multiState as T)
            }
        }
    }

    inline fun <reified T : MultiState> show(noinline notify: (T) -> Unit = {}) {
        show(T::class.java, notify)
    }

    fun View.doAnimator() {
        this.clearAnimation()
        animator.addUpdateListener {
            this.alpha = it.animatedValue as Float
        }
        animator.start()
    }

}
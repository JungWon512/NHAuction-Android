package com.nh.cowauction.widget.visual

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

/**
 * Description : 잠시 보였다 사라지는 TextView
 *
 * Created by hmju on 2021-06-25
 */
class SlowlyDisappearsTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var isAni = false

    init {

        isSelected = true

        if(isInEditMode) {
            text = "Hello"
        } else {
            visibility = View.INVISIBLE
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (!text.isNullOrEmpty()) {
            if (!isAni) {
                this@SlowlyDisappearsTextView.visibility = View.VISIBLE
                postDelayed({
                    ObjectAnimator.ofFloat(this, View.ALPHA, 1.0F, 0.0F).apply {
                        duration = 500
                        interpolator = DecelerateInterpolator()
                        doOnStart {
                            isAni = true
                        }
                        doOnEnd {
                            isAni = false
                            this@SlowlyDisappearsTextView.visibility = View.INVISIBLE
                            this@SlowlyDisappearsTextView.alpha = 1.0F
                        }
                        start()
                    }
                }, 1500)
            }
        }
        super.setText(text, type)
    }
}
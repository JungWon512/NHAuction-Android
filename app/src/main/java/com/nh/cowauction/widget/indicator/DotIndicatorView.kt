package com.nh.cowauction.widget.indicator

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.viewpager2.widget.ViewPager2
import com.nh.cowauction.R
import com.nh.cowauction.extension.dp

/**
 * Description : ViewPager2 Dot Indicator View.
 *
 * Created by hmju on 2021-01-06
 */
class DotIndicatorView @JvmOverloads constructor(
        ctx: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : BaseIndicatorView(ctx, attrs, defStyleAttr) {

    private var dotSize: Float = 0F
    private var dotGap: Float = 0F
    private var rootWidth: Int = 0
    private var rootHeight: Int = 0
    private var indicatorStartPoint: Float = 0F

    init {
        attrs?.run {
            val attr: TypedArray = ctx.obtainStyledAttributes(this, R.styleable.DotIndicatorView)

            try {
                dotSize = attr.getDimension(R.styleable.DotIndicatorView_indicator_dot_size, 0F)
                if (dotSize == 0F) {
                    check(dotSize == 0F)
                }
                dotGap = attr.getDimension(R.styleable.DotIndicatorView_indicator_dot_gap, 5F.dp)
            } finally {
                attr.recycle()
            }
        }
    }

    override fun updateIndicator() {
    }

    override fun onPageSelect(pos: Int) {
        position = pos
        invalidate()
    }

    override fun onPageScroll(pos: Int, offset: Float) {}

    override fun onPageScrollState(@ViewPager2.ScrollState state: Int) {}

    override fun onIndicatorDraw(canvas: Canvas) {
        // Indicator Setting
        backgroundRect.left = indicatorStartPoint
        backgroundRect.right = backgroundRect.left + dotSize
        val middlePoint: Float = rootHeight / 2F
        backgroundRect.top = middlePoint - (dotSize / 2F)
        backgroundRect.bottom = middlePoint + (dotSize / 2F)
        indicatorRect.top = backgroundRect.top
        indicatorRect.bottom = backgroundRect.bottom

        val indicatorIndex = computeFindPos()
        for (i in 0 until dataCnt) {

            // Indicator Draw
            if (i == indicatorIndex) {
                indicatorRect.left = backgroundRect.left
                indicatorRect.right = backgroundRect.right
                if (radius == -1F) {
                    canvas.drawRect(indicatorRect, indicatorPaint)
                } else {
                    canvas.drawRoundRect(indicatorRect, radius, radius, indicatorPaint)
                }
            } else {
                // Background Draw
                if (radius == -1F) {
                    canvas.drawRect(backgroundRect, backgroundPaint)
                } else {
                    canvas.drawRoundRect(backgroundRect, radius, radius, backgroundPaint)
                }
            }

            backgroundRect.left = (backgroundRect.right + dotGap)
            backgroundRect.right = backgroundRect.left + dotSize
        }
    }

    private fun computeFindPos(): Int {
        return if (isLoop) {
            when (position) {
                0 -> {
                    // Fake Last Item
                    dataCnt - 1
                }
                dataCnt + 1 -> {
                    // Fake First Item
                    0
                }
                else -> {
                    // Other
                    position - 1
                }
            }
        } else {
            position
        }
    }

    override fun onIndicatorMeasure(measureWidth: Int, measureHeight: Int) {
        rootWidth = measureWidth
        rootHeight = measureHeight
        computeStartPoint()
    }

    /**
     * Gravity 상태에 따라서
     * Indicator Start Point 계산 하는 함수.
     */
    private fun computeStartPoint() {
        // 불필요한 타입인 경우 리턴.
        if (rootWidth == 0 || dataCnt == 0 || gravity == Gravity.LEFT) return

        val indicatorRootWidth = (dotSize * dataCnt.toFloat()) + (dotGap * (dataCnt.toFloat() - 1F).coerceAtLeast(0F))
        if (gravity == Gravity.CENTER) {
            indicatorStartPoint = (rootWidth.toFloat() / 2F) - (indicatorRootWidth / 2F)
        } else if (gravity == Gravity.RIGHT) {
            indicatorStartPoint = rootWidth - indicatorRootWidth
        }
    }
}
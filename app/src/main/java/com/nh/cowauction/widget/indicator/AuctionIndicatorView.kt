package com.nh.cowauction.widget.indicator

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.nh.cowauction.extension.dp

/**
 * Description : 응찰 페이지 전용 인디게이터 View
 *
 * Created by juhongmin on 6/28/21
 */
class AuctionIndicatorView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseIndicatorView(ctx, attrs, defStyleAttr) {

    private val dotSize: Float = 8F.dp
    private val dotGap: Float = 10F.dp
    private var rootWidth: Int = 0
    private var rootHeight: Int = 0
    private var indicatorStartPoint: Float = 0F


    override fun updateIndicator() {

    }

    override fun onPageSelect(pos: Int) {
        position = pos
        invalidate()
    }

    override fun onPageScroll(pos: Int, offset: Float) {}

    override fun onPageScrollState(state: Int) {}

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

    private fun computeFindPos() = position.minus(1)

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

        val indicatorRootWidth =
            (dotSize * dataCnt.toFloat()) + (dotGap * (dataCnt.toFloat() - 1F).coerceAtLeast(0F))
        if (gravity == Gravity.CENTER) {
            indicatorStartPoint = (rootWidth.toFloat() / 2F) - (indicatorRootWidth / 2F)
        } else if (gravity == Gravity.RIGHT) {
            indicatorStartPoint = rootWidth - indicatorRootWidth
        }
    }
}
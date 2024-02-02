package com.nh.cowauction.widget.pagertablayout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Dimension
import androidx.appcompat.widget.LinearLayoutCompat
import com.nh.cowauction.R
import com.nh.cowauction.databinding.ItemLineTabLayoutBinding
import com.nh.cowauction.databinding.ViewLineTabStripBinding
import com.nh.cowauction.extension.currentItem
import com.nh.cowauction.extension.initBinding
import com.nh.cowauction.utility.DLogger

/**
 * Description : ViewPager2 LineTabLayout
 *
 * Created by hmju on 2021-01-15
 */
class LinePagerTabLayout @JvmOverloads constructor(
        ctx: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : BaseTabLayout(ctx, attrs, defStyleAttr) {

    private val tabListener = object : Listener {
        override fun onTabClick(pos: Int) {
            if (!isEnabled) {
                return
            }
            if (currentPos != pos) {
                currentPos = pos
                viewPager?.currentItem(pos, false)
                invalidate()

                updateTab(pos)
            }
        }
    }

    @Dimension
    private var indicatorHeight: Float = -1F
    private var scrollingOffset = 0

    private val indicatorPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    private var _binding: ViewLineTabStripBinding? = null
    val tabContainer: ViewLineTabStripBinding get() = _binding!!

    var fixedSize: Int = -1

    // setDataList
    var dataList = mutableListOf<PagerTabItem>()
        set(value) {
            tabCount = value.size
            tabContainer.linerLayout.removeAllViews()
            val layoutParams = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT
            )

            if (type == Type.FIXED) {
                if (fixedSize != -1) {
                    tabContainer.linerLayout.weightSum = fixedSize.toFloat()
                } else {
                    tabContainer.linerLayout.weightSum = tabCount.toFloat()
                }
                layoutParams.width = 0
                layoutParams.weight = 1F
            }

            // 데이터 리스트에 맞게 아이템 Binding 한다.
            value.forEachIndexed { index, data ->
                data.pos = index
                data.isSelected?.value = index == currentPos
                data.txtColor = enableTxtColor
                data.disableTxtColor = disableTxtColor

                data.txtSize = textSize
                data.isChangeTextStyle = isChangeTextStyle

                val itemBinding = initBinding<ItemLineTabLayoutBinding>(R.layout.item_line_tab_layout, this, false) {
                    listener = tabListener
                    item = data
                }

                (itemBinding.root as LinearLayoutCompat).layoutParams = layoutParams
                tabContainer.linerLayout.addView(itemBinding.root)

                // Redraw
                invalidate()
            }

            field = value
        }

    private var tabCount: Int = 0
    private var currentPos: Int = 0 // 현재 위치값.
    private var posScrollOffset: Float = -1F // Scroll Offset.
    private var lastScrollX = 0

    init {
            setWillNotDraw(false)

            // 속성 값 세팅
            attrs?.run {
                val attr: TypedArray =
                        ctx.obtainStyledAttributes(this, R.styleable.LinePagerTabLayout)
                try {
                    val color = attr.getColor(
                            R.styleable.LinePagerTabLayout_tab_indicator_color,
                            NO_ID
                    )
                    if (color != NO_ID) {
                        indicatorPaint.color = color
                    }
                    indicatorHeight =
                            attr.getDimension(R.styleable.LinePagerTabLayout_tab_indicator_height, -1F)
                    scrollingOffset = attr.getDimensionPixelOffset(
                            R.styleable.LinePagerTabLayout_tab_scroll_offset,
                            0
                    )
                } finally {
                    attr.recycle()
                }
            }
            _binding = initBinding(R.layout.view_line_tab_strip, this)

            if (bottomLineHeight == NO_ID) {
                tabContainer.isLine = false
            } else {
                tabContainer.isLine = true
                tabContainer.lineHeight = bottomLineHeight
            }
            setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (isInEditMode || tabCount == 0) return

        // Draw Indicator Line
        if (indicatorHeight == -1F) return

        // RootView 에서 Draw 해서 표현..
        with(tabContainer.linerLayout) {
            val currentTab: View? =
                    if (this.childCount > currentPos) getChildAt(currentPos) else null
            var lineLeft: Float = currentTab?.left?.toFloat() ?: 0F
            var lineRight: Float = currentTab?.right?.toFloat() ?: 0F

            // Scroll 하는 경우 Indicator 자연스럽게 넘어가기 위한 로직.
            if (posScrollOffset > 0F && currentPos < tabCount - 1) {
                val nextTab = getChildAt(currentPos + 1)
                lineLeft = (posScrollOffset * nextTab.left + (1F - posScrollOffset) * lineLeft)
                lineRight = (posScrollOffset * nextTab.right + (1F - posScrollOffset) * lineRight)
            }

            canvas?.drawRect(
                    lineLeft,
                    height - indicatorHeight,
                    lineRight,
                    height.toFloat(),
                    indicatorPaint
            )
        }
    }

    /**
     * Update Tab Style.
     */
    fun updateTab(pos: Int) {
        DLogger.d("Update Tab $pos")
        dataList.forEach { data ->
            data.isSelected?.value = data.pos == pos
        }
    }

    /**
     * 한 화면에 보여지는 Tab 이 Over 되는 경우
     * Scrolling 처리 함수.
     * @param pos Current Pos
     * @param offset Scrolling Offset
     */
    private fun scrollToChild(pos: Int, offset: Int) {
        if (tabCount == 0) return

        var newScrollX = tabContainer.linerLayout.getChildAt(pos).left + offset

        if (pos > 0 || offset > 0) {
            newScrollX -= scrollingOffset
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX
            scrollTo(newScrollX, 0)
        }
    }

    override fun onPageSelect(pos: Int) {
        currentPos = pos
        posScrollOffset = 0F
        updateTab(currentPos)
        scrollToChild(pos, (tabContainer.linerLayout).getChildAt(pos).width)

        // ReDraw
        invalidate()
    }

    override fun onPageScroll(pos: Int, offset: Float) {
        currentPos = pos
        posScrollOffset = offset
        scrollToChild(pos, (offset * (tabContainer.linerLayout).getChildAt(pos).width).toInt())

        // ReDraw
        invalidate()
    }

    override fun onPageScrollStated(state: Int) {
//        if (ViewPager2.SCROLL_STATE_IDLE == state) {
////            updateTab(currentPos)
//        }
    }
}
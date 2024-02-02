package com.nh.cowauction.widget.pagertablayout

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.lifecycle.*
import androidx.viewpager2.widget.ViewPager2
import com.nh.cowauction.R
import com.nh.cowauction.extension.dp
import com.nh.cowauction.extension.getFragmentAct
import com.nh.cowauction.utility.DLogger

/**
 * Description : Base TabLayout
 *
 * Created by hmju on 2021-01-15
 */
abstract class BaseTabLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr), LifecycleOwner, LifecycleObserver {

    interface Listener {
        fun onTabClick(pos: Int)
    }

    abstract fun onPageSelect(pos: Int)
    abstract fun onPageScroll(pos: Int, offset: Float)
    abstract fun onPageScrollStated(@ViewPager2.ScrollState state: Int)

    enum class Type {
        FIXED, SCROLLABLE
    }

    /**
     * ViewPager2 PagerListener
     */
    private val pageListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onPageSelect(position)
        }

        override fun onPageScrolled(
                pos: Int,
                offset: Float,
                offsetPixel: Int
        ) {
            // Offset 튀는 현상 방지
            if (offset < 1F) {
                onPageScroll(pos, offset)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            onPageScrollStated(state)
        }
    }

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    var viewPager: ViewPager2? = null
        set(value) {
            value?.unregisterOnPageChangeCallback(pageListener)
            value?.registerOnPageChangeCallback(pageListener)

            field = value
        }

    // [s] Attribute Set Variable
    protected var type: Type = Type.FIXED
    protected var textSize = 18.dp

    @ColorInt
    protected var enableTxtColor = Color.BLACK

    @ColorInt
    protected var disableTxtColor = Color.BLACK

    @Dimension
    protected var bottomLineHeight = NO_ID
    protected var isChangeTextStyle: Boolean = true // 선택된 Tab Text Style Bold 로 할건지 Flag 값
    // [e] Attribute Set Variable

    init {
            isFillViewport = true
            isHorizontalScrollBarEnabled = false
            context.obtainStyledAttributes(attrs, R.styleable.BaseTabLayout).run {
                try {
                    type = Type.values()[getInt(R.styleable.BaseTabLayout_tab_type, 0)]
                    textSize =
                            getDimensionPixelSize(R.styleable.BaseTabLayout_tab_txt_size, 16.dp)
                    isChangeTextStyle =
                            getBoolean(R.styleable.BaseTabLayout_tab_is_change_txt_style, true)
                    enableTxtColor =
                            getColor(R.styleable.BaseTabLayout_tab_txt_color, Color.BLACK)
                    disableTxtColor =
                            getColor(R.styleable.BaseTabLayout_tab_disable_txt_color, Color.BLACK)
                    bottomLineHeight = getDimensionPixelSize(
                            R.styleable.BaseTabLayout_tab_bottom_line_height,
                            NO_ID
                    )
                } catch (ex: RuntimeException) {
                    DLogger.e(ex.message.toString())
                } catch (ex : UnsupportedOperationException) {
                    DLogger.e(ex.message.toString())
                }
                recycle()
            }

            context.getFragmentAct()?.lifecycle?.addObserver(this)
    }

    open fun onCreate() {}
    open fun onResume() {}
    open fun onStop() {}
    open fun onDestroy() {}

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onStateEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                onCreate()
            }
            Lifecycle.Event.ON_RESUME -> {
                onResume()
            }
            Lifecycle.Event.ON_STOP -> {
                onStop()
            }
            Lifecycle.Event.ON_DESTROY -> {
                onDestroy()
            }
            else -> {
            }
        }
    }

    override fun getLifecycle() = lifecycleRegistry
}
package com.nh.cowauction.ui.bindingadapter

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.viewpager2.widget.ViewPager2

/**
 * Description : ViewPager2 전용 Binding Adapter
 *
 * Created by juhongmin on 6/9/21
 */
object ViewPagerBindingAdapter {

    interface ViewPagerScrolled {
        fun onPageScrolled(pos: Int, offset: Float, offsetPixels: Int)
    }

    interface ViewPagerState {
        fun onPageStated(state: Int)
    }

    /**
     * ViewPager2 스와이프 활/비활성화 처리 함수.
     * @param isSwiped true -> HandSwipe On, HandSwipe Off
     */
    @JvmStatic
    @BindingAdapter("swipeEnable")
    fun setViewPagerHandSwipeEnable(
        viewPager: ViewPager2,
        isSwiped: Boolean
    ) {
        viewPager.isUserInputEnabled = isSwiped
    }

    @JvmStatic
    @BindingAdapter("limitSize")
    fun setViewPagerLimitSize(
        viewPager: ViewPager2,
        limitSize: Int
    ) {
        if (limitSize > 0) {
            viewPager.offscreenPageLimit = limitSize
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["onPageScrolled", "onPageState", "onPageSelected"], requireAll = false)
    fun setViewPagerListener(
        viewPager: ViewPager2,
        pageScroll: ViewPagerScrolled?,
        pageState: ViewPagerState?,
        listener: InverseBindingListener
    ) {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                pageScroll?.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(pos: Int) {
                listener.onChange()
            }

            override fun onPageScrollStateChanged(state: Int) {
                pageState?.onPageStated(state)
            }
        })
    }

    @JvmStatic
    @BindingAdapter(value = ["position", "isAni"], requireAll = false)
    fun setCurrentPos(
        viewPager: ViewPager2,
        pos: Int,
        isAni: Boolean?
    ) {
        val _isAnimation = isAni ?: true
        if (pos != viewPager.currentItem) {
            viewPager.post {
                if (viewPager.isFakeDragging) {
                    viewPager.endFakeDrag()
                }
                viewPager.setCurrentItem(pos, _isAnimation)
            }
        }
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "position", event = "onPageSelected")
    fun getViewPagerPosition(viewPager: ViewPager2) = viewPager.currentItem
}
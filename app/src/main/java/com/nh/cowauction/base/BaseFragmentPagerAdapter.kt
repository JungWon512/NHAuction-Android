package com.nh.cowauction.base

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nh.cowauction.extension.getFragmentAct

/**
 * Description : ViewPager2
 * Fragment 전용 Base PagerAdapter Class
 * Created by hmju on 2021-06-07
 */
abstract class BaseFragmentPagerAdapter<T>(ctx: Context) : FragmentStateAdapter(
    ctx.getFragmentAct() ?: throw IllegalArgumentException("Fragment Activity 여야 합니다.")
) {

    abstract fun onCreateFragment(pos: Int): Fragment

    val dataList: MutableList<T> by lazy { mutableListOf() }
    fun setDataList(list: MutableList<T>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount() = dataList.size

    override fun createFragment(position: Int): Fragment {
        return onCreateFragment(position)
    }
}
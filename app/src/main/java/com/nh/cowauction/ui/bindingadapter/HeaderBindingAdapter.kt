package com.nh.cowauction.ui.bindingadapter

import androidx.databinding.BindingAdapter
import com.nh.cowauction.contants.HeaderType
import com.nh.cowauction.ui.header.HeaderView

/**
 * Description :
 *
 * Created by hmju on 2021-06-01
 */
object HeaderBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = [
        "headerTitle",
        "headerUserName",
        "headerWishPrice",
        "headerPartNum"], requireAll = false)
    fun setHeaderView(
            headerView: HeaderView,
            title: String?,
            userName: String?,
            wishPrice: Int?,
            partNum: Int?
    ) {
        headerView.setHeaderTitle(title)
        headerView.setHeaderUserName(userName)
        headerView.setWishPrice(wishPrice)
        headerView.setPartNum(partNum)
    }
}
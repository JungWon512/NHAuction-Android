package com.nh.cowauction.contants

import androidx.annotation.LayoutRes
import com.nh.cowauction.R

enum class HeaderType(@LayoutRes val id: Int) {
    NONE(R.layout.view_header_none),
    AUCTION(R.layout.view_header_auction),
    WEB(R.layout.view_header_web)
}

/**
 * ViewPager2 Fragment Adapter
 */
enum class FragmentType(val uniqueId: Int = 0) {
    AUCTION_TOP(10) // 경매 > 계체 정보, LiveCam
}


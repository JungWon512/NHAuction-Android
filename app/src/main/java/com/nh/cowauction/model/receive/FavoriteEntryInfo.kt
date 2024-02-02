package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 관심 출품 상품 여부 전송
 * 조합구분코드 |
 * 출품번호 |
 * 관심 출품 상품 여부(Y/N)
 *
 * Created by hmju on 2021-06-14
 */
data class FavoriteEntryInfo(
        @Order(1)
        val auctionHouseCode: String = "",
        @Order(2)
        val entryNum: String = "",
        @Order(3)
        val favoriteYn: String = ""
) : BaseData(Type.FAVORITE_ENTRY_INFO)
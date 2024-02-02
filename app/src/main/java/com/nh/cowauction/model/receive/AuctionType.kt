package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 경매 유형 정보 전송 기능
 * 조합구분코드 |
 * 경매유형(단일/일괄) |
 *
 *
 * Created by hmju on 2021-09-29
 */
data class AuctionType(
        @Order(1) // 조합구분코드
        val auctionHouseCode: String = "",
        @Order(2)
        val auctionType: String = "" // 10 -> 일괄 경매 20 -> 단일 경매
) : BaseData(Type.AUCTION_TYPE)
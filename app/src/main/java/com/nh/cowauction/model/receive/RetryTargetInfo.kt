package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 재경매 대상 전송 기능
 * 조합구분코드 |
 * 출품번호 |
 * 대상자참여번호1,대상자참여번호2,대상자참여번호3….대상자참여번호
 *
 * Created by hmju on 2021-08-19
 */
data class RetryTargetInfo(
        @Order(1) // 조합구분코드
        val auctionHouseCode: String = "",
        @Order(2) // 출품 번호
        val num: String = "",
        @Order(3)
        val retryBidders: String = ""
) : BaseData(Type.RETRY_BIDDING_INFO)
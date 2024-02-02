package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 출품 정보 응답 전송
 * 조합구분코드 |
 * 거래인관리번호 |
 * 출품번호 |
 * 응찰금액(만원) |
 * 응찰시간(yyyyMMddhhmmssSSS)
 *
 * Created by hmju on 2021-08-10
 */
data class ResponseBiddingInfo(
        @Order(1)
        val auctionHouseCode: String = "",
        @Order(2)
        val traderMngNum: String = "",
        @Order(3) // 출품 번호
        val entryNum: String = "",
        @Order(4) // 응찰금액
        val biddingPrice: String = "",
        @Order(5)
        val biddingTime: String = ""
) : BaseData(Type.RECV_BIDDING_INFO)
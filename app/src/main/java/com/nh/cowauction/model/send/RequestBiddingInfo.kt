package com.nh.cowauction.model.send

import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 응찰 정보 조회 요청
 * 조합구분코드 |
 * 거래인관리번호 |
 * 경매참여번호 |
 * 출품번호
 *
 * Created by hmju on 2021-08-10
 */
data class RequestBiddingInfo(
        @Order(1)
        val auctionHouseCode: String,
        @Order(2) // 거래인 관리 번호
        val traderMngNum: String,
        @Order(3) // 경매 참여 번호
        val userNum: String,
        @Order(4)
        val entryNum : String
) : BaseData(Type.REQ_BIDDING_INFO)
package com.nh.cowauction.model.send

import com.nh.cowauction.contants.Type
import com.nh.cowauction.extension.osCode
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 경매 응찰 처리 기능
 * 조합구분코드 |
 * 접속채널(ANDROID/IOS/WEB) |
 * 경매회원번호(거래인번호) |
 * 경매참가번호 |
 * 출품번호 |
 * 응찰금액(만원) |
 * 신규응찰여부(Y/N) |
 * 응찰시간(yyyyMMddhhmmssSSS)
 *
 * Created by hmju on 2021-06-14
 */
data class Bidding(
        @Order(1) // 조합구분코드
        val auctionHouseCode: String = "",
        @Order(2) // 접속 채널
        val osType: String = osCode,
        @Order(3) // 거래 경매회원번호(거래인번호)
        val traderMngNum : String = "",
        @Order(4) // 경매참가번호
        val userNum: String = "",
        @Order(5) // 출품 번호
        val entryNum: String = "",
        @Order(6) // 응찰 금액
        val price: Int = 0,
        @Order(7) // 신규 응찰 여부
        val newBiddingYn: String = "Y",
        @Order(8) // 시간
        val time : Int = 0
) : BaseData(Type.BIDDING)
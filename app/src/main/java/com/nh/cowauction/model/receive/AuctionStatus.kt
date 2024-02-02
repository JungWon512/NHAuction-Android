package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.AuctionState
import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 경매 상태 정보 전송 기능
 * 조합구분코드 |
 * 출품번호 |
 * 경매회차 |
 * 시작가 |
 * 현재응찰자수 |
 * 경매상태(NONE / READY / START / PROGRESS / PASS / COMPLETED / FINISH) |
 * 1순위회원번호 |
 * 2순위회원번호 |
 * 3순위회원번호 |
 * 경매진행완료출품수 |
 * 경매잔여출품수
 * Created by hmju on 2021-06-14
 */
data class AuctionStatus(
        @Order(1) // 조합구분코드
        val auctionHouseCode: String = "",
        @Order(2) // 출품 번호
        val num: String = "",
        @Order(3) // 경매회차
        val auctionRound : String ="",
        @Order(4) // 시작가
        val currentPrice: Int = 0,
        @Order(5) // 현재응찰자수
        val biddingCnt: Int = 0,
        @Order(6) // 경매상태(NONE / READY / START / PROGRESS / PASS / COMPLETED / FINISH)
        val status: Int = 0,
        @Order(7) // 1순위회원번호
        val firstRankNum: Int = 0,
        @Order(8) // 2순위회원번호
        val secondRankNum: Int = 0,
        @Order(9)  // 3순위회원번호
        val thirdRankNum: Int = 0,
        @Order(10) // 경매진행완료출품수
        val auctionCompletedCnt: Int = 0,
        @Order(11) // 경매잔여출품수
        val auctionResidualCnt: Int = 0
) : BaseData(Type.AUCTION_STATUS) {
    var auctionState: AuctionState? = null
        get() {
            if (field == null) {
                field = AuctionState.values().find { it.code == status } ?: AuctionState.UNKNOWN
            }
            return field
        }
}
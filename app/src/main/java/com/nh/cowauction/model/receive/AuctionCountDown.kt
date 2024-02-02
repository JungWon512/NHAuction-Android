package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.AuctionState
import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : AUCTION_COUNT_DOWN
 * 조합구분코드 |
 * 상태구분(R : 준비 / C : 카운트다운 / F : 카운트다운 완료) |
 * 카운트다운 시간(second)
 *
 * Created by hmju on 2021-06-14
 */
data class AuctionCountDown(
        @Order(1) // 조합구분코드
        val auctionHouseCode: String = "",
        @Order(2)  // R 준비, C 카운트 다운, F 완료
        val status: String = "",
        @Order(3)
        val sec: Int = 0 // 카운트 다운 시간
) : BaseData(Type.AUCTION_COUNT_DOWN) {

    var auctionCountDownType: AuctionState? = null
        get() {
            if (field == null) {
                field = when (status) {
                    "F" -> AuctionState.UNKNOWN
                    "R" -> AuctionState.READY
                    else -> {
                        // 카운트 다운
                        if(sec == -1) {
                            AuctionState.UNKNOWN
                        } else {
                            AuctionState.COUNT_DOWN.apply {
                                etc = intArrayOf(sec)
                            }
                        }
                    }
                }
            }
            return field
        }
}
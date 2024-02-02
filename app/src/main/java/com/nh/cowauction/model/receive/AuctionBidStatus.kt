package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.BidStatusType
import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 조합구분코드 | 경매번호 | 응찰상태코드(F/P)
 *
 * Created by hmju on 2021-10-13
 */
data class AuctionBidStatus(
        @Order(1) // 조합구분코드
        val auctionHouseCode: String = "",
        @Order(2) // 출품번호
        val entryNum: String = "",
        @Order(3) // 응찰 상태 (F 응찰 종료, P 응찰 진행, N Skip)
        val biddingStatus: String = ""
) : BaseData(Type.BID_STATUS) {
    var biddingType: BidStatusType? = null
        get() {
            if (field == null) {
                field = when (biddingStatus) {
                    "F" -> BidStatusType.END
                    "P" -> BidStatusType.PROGRESS
                    else -> BidStatusType.SKIP
                }
            }
            return field
        }

    var isBiddingEnd: Boolean? = null
        get() {
            if (field == null) {
                field = biddingStatus == "F"
            }
            return field
        }
}
package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.BiddingResult
import com.nh.cowauction.contants.Type
import com.nh.cowauction.extension.toIntOrDef
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 출품 건에 대한 최종 결과 전송
 * 조합구분코드 |
 * 출품번호 |
 * 결과 코드 22(낙찰), 23(보류), 24(취소)
 * 낙찰자회원번호(거래인번호) |
 * 낙찰자경매참가번호 |
 * 낙찰금액
 *
 * Created by hmju on 2021-06-24
 */
data class AuctionResult(
    @Order(1) // 조합구분코드
    val auctionHouseCode: String = "",
    @Order(2) // 출품 번호
    val entryNum: Int = 0,
    @Order(3) // 낙/유찰결과코드
    val auctionResultCode: String = "",
    @Order(4) //  낙찰자회원번호(거래인번호)
    val traderMngNum: String = "",
    @Order(5) // 낙찰자경매참가번호
    val userNum: String = "",
    @Order(6)
    val price: String = ""
) : BaseData(Type.AUCTION_RESULT) {

    var result: BiddingResult? = null
        get() {
            if (field == null) {
                field = when (auctionResultCode) {
                    "22" -> BiddingResult.SUCCESS
                    "23" -> BiddingResult.HOLD
                    "24" -> BiddingResult.CANCEL
                    else -> null
                }
            }
            return field
        }

    var manPrice: Int? = null
        get() {
            if (field == null) {
                field = price.toIntOrDef(0) / 10000
            }
            return field
        }
}
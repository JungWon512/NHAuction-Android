package com.nh.cowauction.model.send

import com.nh.cowauction.contants.Type
import com.nh.cowauction.extension.osCode
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 응찰 취소 요청
 * 조합구분코드 |
 * 출품번호 |
 * 경매회원번호(거래인번호) |
 * 응찰자경매참가번호 |
 * 접속채널(ANDROID/IOS/WEB) |
 * 취소요청시간(yyyyMMddHHmmssSSS) -> 0
 *
 * Created by hmju on 2021-06-24
 */
data class CancelBidding(
    @Order(1)
    val auctionHouseCode: String = "", // 조합구분코드
    @Order(2)
    val entryNum: String = "",
    @Order(3)
    val traderMngNum : String = "",
    @Order(4)
    val userNum: String = "",
    @Order(5)
    val osType: String = osCode,
    @Order(6)
    val time: Int = 0
) : BaseData(Type.CANCEL_BIDDING)
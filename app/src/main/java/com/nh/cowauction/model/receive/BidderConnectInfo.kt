package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 응찰자 접속 정보 전송 처리
 * 조합구분코드 |
 * 회원번호 |
 * 접속요청채널(6001/6002/6003/6004/6005) |
 * 사용채널(ANDROID/IOS/WEB) |
 * 상태(N : 미응찰 / B : 응찰 / C : 응찰취소 / 접속해제 : L) |
 * 응찰가격
 *
 * Created by hmju on 2021-07-20
 */
data class BidderConnectInfo(
    @Order(1) // 조합구분코드
    val auctionHouseCode: String = "",
    @Order(2) // 회원번호
    val userNum: String = "",
    @Order(3) // 접속요청채널
    val channel: String = "",
    @Order(4) // 사용채널
    val osType: String = "",
    @Order(5) // 상태
    val status: String = "",
    @Order(6) // 응찰 가격
    val biddingPrice: String
) : BaseData(Type.BIDDER_CONNECT_INFO)
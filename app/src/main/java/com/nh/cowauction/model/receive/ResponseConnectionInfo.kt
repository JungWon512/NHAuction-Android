package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 접속 정보 인증 결과 응답 처리 기능
 * 조합구분코드 |
 * 접속결과코드 | (2000: 인증 성공, 2001: 인증 실패, 2002: 중복 접속, 2003: 기타 장애)
 * 거래인관리번호 |
 * 경매참가번호
 *
 * Created by hmju on 2021-06-14
 */
data class ResponseConnectionInfo(
        @Order(1)
        val auctionHouseCode: String = "",   // 조합구분코드
        @Order(2)
        val connectionCode: Int = 2004, // 접속결과코드
        @Order(3)
        val traderMngNum: String = "",
        @Order(4)
        val userNum: String = ""
) : BaseData(Type.RECV_CONNECTION_INFO)
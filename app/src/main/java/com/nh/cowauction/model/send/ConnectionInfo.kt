package com.nh.cowauction.model.send

import com.nh.cowauction.contants.Type
import com.nh.cowauction.extension.osCode
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 접속자 정보 인증 처리 요청 처리
 * 조합구분코드 |
 * 거래인관리번호 |
 * 인증토큰 |
 * 접속요청채널 |
 * 사용채널(ANDROID/IOS/WEB/MANAGE)
 * Created by hmju on 2021-06-14
 */
data class ConnectionInfo(
        @Order(1)
        val auctionHouseCode: String,
        @Order(2)
        val traderMngNum: String,
        @Order(3)
        val token: String,
        @Order(4)
        val channel: Int,
        @Order(5)
        val osType: String = osCode
) : BaseData(Type.CONNECTION_INFO)
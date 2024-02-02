package com.nh.cowauction.model.send

import com.nh.cowauction.contants.Type
import com.nh.cowauction.extension.osCode
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 접속 상태 응답
 * 회원번호 |
 * 접속요청채널(6001/6002/6003/6004/6005) |
 * 사용채널(ANDROID/IOS/WEB/MANAGE)
 *
 * Created by hmju on 2021-06-14
 */
data class AuctionResponseSession(
    @Order(1)
    val userNum : String = "",
    @Order(2)
    val channel: Int = 6001,
    @Order(3)
    val osType : String = osCode
) : BaseData(Type.SEND_AUCTION_SESSION)
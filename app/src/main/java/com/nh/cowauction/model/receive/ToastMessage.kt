package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 메시지 전송 처리 기능
 * 경매거점코드 |
 * 메시지 내용
 * Created by hmju on 2021-06-14
 */
data class ToastMessage(
        @Order(1)
        val auctionCode: String = "",
        @Order(2)
        val msg: String = ""
) : BaseData(Type.RECV_MESSAGE)
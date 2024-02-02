package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.ResponseCodeType
import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 예외 상황 전송 처리 기능
 * 조합구분코드 |
 * 예외 상황 코드
 * Created by hmju on 2021-06-14
 */
data class ResponseCode(
        @Order(1)
        val auctionHouseCode: String = "",
        @Order(2)
        val code: Int = 0 // 예외 상황 코드
) : BaseData(Type.RES_CODE) {
    var codeType: ResponseCodeType? = null
        get() {
            if (field == null) {
                field = ResponseCodeType.values().find { it.code == code } ?: ResponseCodeType.NONE
            }
            return field
        }
}
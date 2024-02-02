package com.nh.cowauction.model.send

import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 출품 정보 전송 요청
 * 조합구분코드 |
 * 거래인관리번호 |
 * 출품번호
 * Created by hmju on 2021-08-10
 */
data class RequestEntryInfo(
        @Order(1)
        val auctionHouseCode: String,
        @Order(2)
        val traderMngNum: String,
        @Order(3)
        val entryNum: String,
) : BaseData(Type.REQ_ENTRY_INFO)
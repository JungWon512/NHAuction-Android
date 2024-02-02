package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.EntryTitleType
import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 출품 정보 노출 설정 전송
 *  - 수신 받은 정보와 같이 응찰 화면에 노출 처리한다.
 *  1. 출품번호
 *  2. 출하주
 *  3. 성별
 *  4. 중량
 *  5. 어미
 *  6. 계대
 *  7. 산차
 *  8. KPN
 *  9. 지역명
 *  10. 비고
 *  11. 최처가
 *  12. 친자여부
 * Created by hmju on 2021-07-23
 */
data class ShowEntryInfo(
        @Order(1) // 조합구분코드
        val auctionHouseCode: String = "",
        @Order(2)
        val one: Int = 0,
        @Order(3)
        val two: Int = 0,
        @Order(4)
        val three: Int = 0,
        @Order(5)
        val four: Int = 0,
        @Order(6)
        val five: Int = 0,
        @Order(7)
        val six: Int = 0,
        @Order(8)
        val seven: Int = 0,
        @Order(9)
        val eight: Int = 0,
        @Order(10)
        val nine: Int = 0,
        @Order(11)
        val ten: Int = 0,
        @Order(12)
        val divisionPrice2: Int = 1
) : BaseData(Type.SHOW_ENTRY_INFO)

/**
 * 개체 정보 제목에 대한 데이터 모델
 */
data class CurrentEntryTitle(
        val one: EntryTitleType,
        val two: EntryTitleType,
        val three: EntryTitleType,
        val four: EntryTitleType,
        val five: EntryTitleType,
        val six: EntryTitleType,
        val seven: EntryTitleType,
        val eight: EntryTitleType,
        val nine: EntryTitleType,
        val ten: EntryTitleType
)

fun Int.toEntryTitleType(): EntryTitleType {
    return when (this) {
            1 -> EntryTitleType.NUM
            2 -> EntryTitleType.SHIPPER
            3 -> EntryTitleType.GENDER
            4 -> EntryTitleType.WEIGHT
            5 -> EntryTitleType.MOTHER
            6 -> EntryTitleType.PASG_QCN
            7 -> EntryTitleType.CAVING_NUM
            8 -> EntryTitleType.KPN
            9 -> EntryTitleType.AREA
            10 -> EntryTitleType.NOTE
            11 -> EntryTitleType.LOW_PRICE
            12 -> EntryTitleType.IS_PATERNAL_EXAMINATION
        else -> EntryTitleType.NO_ID
    }
}
package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.EntryTitleType
import com.nh.cowauction.contants.Type
import com.nh.cowauction.extension.comma
import com.nh.cowauction.extension.toEmptyStr
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.base.Order

/**
 * Description : 현재 출품 정보 전송
 * 조합구분코드 |
 * 출품번호 |
 * 경매회차 |
 * 경매대상구분코드 |
 * 축산개체관리번호 |
 * 축산축종구분코드 |
 * 농가식별번호 |
 * 농장관리번호 |
 * 농가명 |
 * 브랜드명 |
 * 생년월일 |
 * KPN번호 |
 * 개체성별코드 |
 * 어미소구분코드 |
 * 어미소축산개체관리번호 |
 * 산차 |
 * 임신 개월수 |
 * 계대 |
 * 계체식별번호 |
 * 축산개체종축등록번호 |
 * 등록구분번호 |
 * 출하생산지역 |
 * 친자검사결과여부 |
 * 신규여부 |
 * 우출하중량 |
 * 최초최저낙찰한도금액 |
 * 최저낙찰한도금액 |
 * 비고내용 |
 * 낙유찰결과 |
 * 낙찰자 |
 * 낙찰금액 |
 * 응찰일시 |
 * 마지막출품여부
 *
 * Created by hmju on 2021-06-14
 */
data class CurrentEntryInfo(
        @Order(1) // 조합구분코드
        val auctionHouseCode: String = "",
        @Order(2) // 출품번호
        val entryNum: String = "",
        @Order(3) // 경매회차
        val auctionRound : String ="",
        @Order(4)// 경매대상구분코드 (1 : 송아지 / 2 : 비육우 / 3 : 번식우)
        val entryType: String = "",
        @Order(5) // 축산개체관리번호
        val indNum: String = "",
        @Order(6)// 축산축종구분코드
        val indMngCd: String = "",
        @Order(7) // 농가식별번호
        val fhsNum: String = "",
        @Order(8) // 농장관리번호
        val farmMngNum: String = "",
        @Order(9) // 농가명
        val exhibitor: String = "",
        @Order(10) // 브랜드명
        val brandName: String = "",
        @Order(11) // 생년월일
        val entryBirth: String = "",
        @Order(12) // KPN번호
        val entryKpn: String = "",
        @Order(13)// 개체성별코드
        val entryGender: String = "",
        @Order(14) // 어미소구분코드
        val motherTypeCode: String = "",
        @Order(15) // 어미소축산개체관리번호
        val motherObjNum: String = "",
        @Order(16)// 산차
        val cavingNum: String = "",
        @Order(17) // 임신 개월수
        val pregnancyMonths : String = "",
        @Order(18) // 계대
        val pasgQcn: String = "",
        @Order(19) // 계체식별번호
        val objIdNum: String = "",
        @Order(20) // 축산개체종축등록번호
        val objRegNum: String = "",
        @Order(21) // 등록구분번호
        val objRegTypeNum: String = "",
        @Order(22) // 출하생산지역
        val productionArea: String = "",
        @Order(23) // 친자검사결과여부
        val isPaternalExamination: String = "",
        @Order(24) //신규여부
        val IsNew: String = "",
        @Order(25) // 우출하중량
        val weight: String = "",
        @Order(26) // 최초최저낙찰한도금액
        val initPrice: Int = 0,
        @Order(27) // 최저낙찰한도금액
        val lowPrice: Int = 0,
        @Order(28) // 비고내용
        val note: String = "",
        @Order(29) // 낙유찰결과
        val biddingResult: String = "",
        @Order(30) // 낙찰자
        val succBidder: String = "",
        @Order(31)  // 낙찰금액
        val succBidPrice: String = "",
        @Order(32) // 응찰일시
        val biddingDate: String = "",
        @Order(33) // 마지막출품여부
        val isLastEntry: String = "",
        @Order(34) // 계류대번호
        val mooringNum : String = "",
        @Order(35) // 초과 출장우 여부
        val overEntryYn : String = ""
) : BaseData(Type.ENTRY_INFO)

/**
 * 개채 정보 내용에 대한 데이터 모델.
 */
data class CurrentEntryContents(
        val one: String? = null,
        val two: String? = null,
        val three: String? = null,
        val four: String? = null,
        val five: String? = null,
        val six: String? = null,
        val seven: String? = null,
        val eight: String? = null,
        val nine : String? = null,
        val ten : String? = null
)

fun CurrentEntryInfo.toEntryContents(type: EntryTitleType): String {
    return when (type) {
            EntryTitleType.NUM -> entryNum.toEmptyStr("-")
            EntryTitleType.SHIPPER -> exhibitor.toEmptyStr("-")
            EntryTitleType.GENDER -> entryGender.toEmptyStr("-")
            EntryTitleType.WEIGHT -> weight.toEmptyStr("-")
            EntryTitleType.MOTHER -> motherTypeCode.toEmptyStr("-")
            EntryTitleType.PASG_QCN -> pasgQcn.toEmptyStr("-")
            EntryTitleType.CAVING_NUM -> cavingNum.toEmptyStr("-")
            EntryTitleType.KPN -> entryKpn.replace("KPN","").toEmptyStr("-")
            EntryTitleType.AREA -> productionArea.toEmptyStr("-")
            EntryTitleType.NOTE -> note.toEmptyStr("-")
            EntryTitleType.LOW_PRICE -> lowPrice.comma()
            EntryTitleType.IS_PATERNAL_EXAMINATION -> isPaternalExamination.toEmptyStr("-")
        else -> ""
    }
}
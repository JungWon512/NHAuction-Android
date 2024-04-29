package com.nh.cowauction.contants

import androidx.annotation.StringRes
import com.nh.cowauction.R

object Config {

    const val IS_DEBUG = true

    const val LOCAL_AUCTION_IP = "192.168.0.23"
    const val DEV_AUCTION_IP = "115.41.222.25" /*LOCAL_AUCTION_IP*/
    const val PRD_AUCTION_IP = "1.201.161.58"

    var AUCTION_IP = if (IS_DEBUG) DEV_AUCTION_IP else PRD_AUCTION_IP
    var AUCTION_PORT = 5001
    const val DEV_DOMAIN =   "https://xn--e20bw05b.kr" /*"http://192.168.0.86:18080"*/ /*"https://www.가축시장.kr"*/ /*"http://nhlva.nonghyup.com"*/
    const val PROD_DOMAIN = "https://xn--o39an74b9ldx9g.kr" /*"http://nhlva.nonghyup.com"*/
    val BASE_DOMAIN = if (IS_DEBUG) DEV_DOMAIN else PROD_DOMAIN

    const val DEV_AUCTION_DOMAIN =   "xn--e20bw05b.kr" /*난장 or localhost*/
    const val PROD_AUCTION_DOMAIN = "cowauction.kr" /*cowauction*/
    val AUCTION_DOMAIN = if (IS_DEBUG) DEV_AUCTION_DOMAIN else PROD_AUCTION_DOMAIN

    const val INVALID_CHANNEL_ID = "none"

    val BASE_FETCH_FMT_WEB_URL = "$BASE_DOMAIN/auction/api/entryListApi?naBzplc=%s&loginNo=%s"
    val DYNAMIClINK = "https://nhauction.page.link/link"
    val DYNAMICLINK_PAJU = "https://nhauction.page.link/paju"

}

object SocketNetwork {
    const val MAX_BUF = 2048
}

/**
 * 패킷 구분자 Type.
 */
enum class Type(val key: String) {
    CONNECTION_INFO("AI"),          // 접속자 정보 인증 처리 요청 처리
    ENTRY_INFO("SC"),               // 현재 출품 정보 전송
    RECV_CONNECTION_INFO("AR"),     // 접속 정보 인증 결과 응답 처리 기능
    BIDDING("AB"),                  // 경매 응찰 처리 기능
    AUCTION_STATUS("AS"),           // 경매 상태 정보 전송 기능
    FAVORITE_ENTRY_INFO("SF"),      // 관심 출품 상품 여부 전송
    AUCTION_COUNT_DOWN("SD"),       // 경매 시작/종료 카운트 다운 정보
    RECV_MESSAGE("ST"),             // 메시지
    RES_CODE("SE"),                 // 예외 상황 코드
    RECV_AUCTION_SESSION("SS"),     // 경매 세션 체크
    SEND_AUCTION_SESSION("AA"),     // 접속 상태 응답 기능
    CANCEL_BIDDING("AC"),           // 응찰 취소
    AUCTION_RESULT("AF"),           // 낙/유찰 결과
    BIDDER_CONNECT_INFO("SI"),      // 응찰자 접속 정보 전송 처리
    SHOW_ENTRY_INFO("SH"),          // 응찰 개체 화면 순서 처리
    REQ_ENTRY_INFO("AE"),           // 출품 정보 요청
    REQ_BIDDING_INFO("AD"),         // 응찰 정보 조회 요청
    RECV_BIDDING_INFO("AP"),         // 응찰 정보 응답
    RETRY_BIDDING_INFO("AN"),          // 재경매 대상 전송 기능
    AUCTION_TYPE("AT"),              // 경매 타입
    BID_STATUS("AY")                //  경매 응찰 종료 상태
}


/**
 * 경매 접속 타입
 */
enum class AuctionConnectionTypeCode(val code: Int) {
    BIDDING(6001), // 응찰
    WATCH(6003) // 관전
}


/**
 * 접속 상태 코드
 */
enum class ConnectionCode(val code: Int) {
    SUCC(2000), // 인증 성공
    FAIL(2001), // 인증 실패
    DUPLICATE(2002), // 중복 접속
    BEFORE_AUCTION(2003) // 제어접속하기 전.
}

/**
 * Error Code 값
 */
enum class ResponseCodeType(@StringRes val id: Int, val code: Int) {
    NONE(-1, -1), // 서버에서 정의되지 않는 코드가 오는 경우.
    UNKNOWN(-1, 4001), // 요청 결과 미존재
    FAIL(-1, 4002), // 요청 처리 실패
    NOT_ENOUGH_BIDDING(R.string.str_auction_response_price_not_enough, 4003), // 시작가 이하 응찰 시도
    AUCTION_BEFORE_START(-1, 4004), // 출품 이관 전 상태
    BIDDING_CANCEL_FAIL(-1, 4005), // 응찰 취소 불가
    BIDDING_SUCCESS(R.string.str_auction_bidding_success, 4006), // 응찰 성공
    BIDDING_CANCEL(R.string.str_auction_bidding_cancel, 4007) // 응찰 취소
}

/**
 * 경매 상태에 따른 메시지 EmumClass
 * @param code 경매 상태 Code 값 없는 경우도 있음
 * @param msg 경매 상태에 따른 메시지 값
 * @param etc Etc.
 */
enum class AuctionState(val code: Int = -1, @StringRes val msg: Int = -1, vararg var etc: Int) {
    UNKNOWN,
    NONE(code = 8001, msg = R.string.str_auction_invalid), // 금일 경매 X
    READY(code = 8002, msg = R.string.str_auction_state_ready), // 금일 경매 대기
    START(code = 8003, msg = R.string.str_auction_state_start), // 현 출품 정보 경매 시작
    PROGRESS(code = 8004, msg = R.string.str_auction_state_progress), // 현 출품 정보 경매 진행
    HOLD(code = 8005, msg = R.string.str_auction_state_bid_miscarry), // 현 출품 경매 보류
    COMPLETED(code = 8006, msg = R.string.str_auction_state_ready), // 현 경매 완료
    BATCH_COMPLETED(code = 8006, msg = R.string.str_auction_state_finish_red), // 현 경매 완료 - 일괄
    FINISH(code = 8007, msg = R.string.str_auction_finish), // 금일 경매 종료
    COUNT_DOWN(msg = R.string.str_auction_state_count_down, etc = intArrayOf(5)), // 경매 종료 카운트 다운
    SUCCESS_BID(msg = R.string.str_auction_state_bid_success), // 낙찰
    SUCCESS_BID_WON(msg = R.string.str_auction_state_bid_success_won), // 낙찰 - 원단위
    OTHER_SUCCESS_BID(msg = R.string.str_auction_state_other_bid_success), // 타인 낙찰
    OTHER_SUCCESS_BID_WON(msg = R.string.str_auction_state_other_bid_success_won), // 타인 낙찰 - 원단위
    RETRY_BID(msg = R.string.str_auction_retry_bidding), // 재경매
    RETRY_NOT_BID(msg = R.string.str_auction_retry_not_bidding), // 재경매 미대상
    BATCH_SELECTION(msg = R.string.str_auction_batch_selection_info), // 일괄 경매 번호 입력
    BATCH_BIDDING(msg = R.string.str_auction_batch_bidding_info), // 일괄 경매 응찰 금액 입력
    BIDDING_SUCCESS(msg = R.string.str_auction_bidding_success), // 응찰 성공
    BIDDING_END(msg = R.string.str_auction_bidding_end) // 응찰 종료
    , SUCCESS_BID_DEFAULT(msg = R.string.str_auction_state_bid_success_default) // 낙찰 - 단위없음
    , OTHER_SUCCESS_BID_DEFAULT(msg = R.string.str_auction_state_other_bid_success_default)  // 타인낙찰 - 단위없음
}

/**
 * 입찰 결과 값
 * 성공, 보류, 취소
 */
enum class BiddingResult {
    SUCCESS,
    HOLD,
    CANCEL
}

/**
 * Remon Cast 상태
 * 중지, 접속, 시작
 */
enum class CastState {
    STOP,
    JOIN,
    PLAYING,
    ERROR
}

enum class LoadingDialogState {
    NONE,
    SHOW,
    DISMISS
}

/**
 * 경매 개체 정보 Enum Class
 * Index 는 1부터 시작
 */
enum class EntryTitleType(@StringRes val id: Int) {
    NO_ID(-1),
    NUM(R.string.str_auction_entry_title_1), // 출품 번호
    SHIPPER(R.string.str_auction_entry_title_2), // 출하주
    GENDER(R.string.str_auction_entry_title_3), // 성별
    WEIGHT(R.string.str_auction_entry_title_4), // 중량
    MOTHER(R.string.str_auction_entry_title_5), // 어미
    PASG_QCN(R.string.str_auction_entry_title_6), // 계대
    CAVING_NUM(R.string.str_auction_entry_title_7), // 산차
    KPN(R.string.str_auction_entry_title_8), // KPN
    AREA(R.string.str_auction_entry_title_9), // 지역명
    NOTE(R.string.str_auction_entry_title_10), // 비고
    LOW_PRICE(R.string.str_auction_entry_title_11), // 최저가
    IS_PATERNAL_EXAMINATION(R.string.str_auction_entry_title_12) // 친자 여부
}

/**
 * Extra Code
 */
object ExtraCode {
    const val AUCTION_CAM_POS = "a"
    const val AUCTION_CAM_CHANNEL_ID = "b"
    const val WATCH_AUCTION_URL = "c"
    const val DEEP_LINK_TARGET_URL = "a"
    const val DYNAMIC_LINK_TARGET_URL = "d"
}

/**
 * 경매 운영 Enum Class
 * 순차 경매,
 * 일괄 경매, (경매 번호 선택)
 * 일괄 경매 (경매 응찰)
 */
enum class AuctionOperation {
    SEQ, // 순차 경매 단계
    BATCH_SELECTION, // 일괄 경매 선택
    BATCH_BIDDING // 일괄 경매 응찰
}

enum class MainEntryType {
    VERSION,
    PERMISSION,
    WEB_PAGE_LOADED
}

enum class NetworkAuctionConnection(@StringRes val msg: Int) {
    DEFAULT(R.string.str_not_open_auction),
    DUPLICATE(R.string.str_auction_invalid_duplicate),
    FAIL(R.string.str_auction_fail),
    RECONNECT(R.string.str_message_auction_network_reconnection),
    NONE(R.string.str_auction_invalid),
    FINISH(R.string.str_auction_finish)
}

enum class BidStatusType {
    SKIP,
    PROGRESS,
    END
}

enum class AuctionSnackType(@StringRes val msg: Int) {
    GONE(-1),
    BIDDING_SUCCESS(R.string.str_auction_bidding_normal_success),
    READY(R.string.str_auction_state_ready_green),
    COMPLETE(R.string.str_auction_state_finish_red)
}

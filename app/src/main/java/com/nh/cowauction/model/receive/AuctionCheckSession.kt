package com.nh.cowauction.model.receive

import com.nh.cowauction.contants.Type
import com.nh.cowauction.model.base.BaseData

/**
 * Description : 접속 상태 확인 기능
 * 경매 서버 접속 정보 유효 확인
 * 경매 서버에서 설정된 시간 간격으로 응답이 없는 클라이언트에게만 접속 유효 확인 처리를 실행한다.
 * Created by hmju on 2021-06-14
 */
class AuctionCheckSession : BaseData(Type.RECV_AUCTION_SESSION)
package com.nh.cowauction.repository.tcp.login

import com.nh.cowauction.extension.toIntOrDef
import com.nh.cowauction.utility.DLogger
import javax.inject.Inject

/**
 * Description : 단순 사용자 이름이랑 토큰 등을 관리하는 클래스
 *
 * Created by hmju on 2021-06-11
 */
interface LoginManager {
    fun isLogin(): Boolean
    fun isAuctionAvailable(): Boolean // 경매 응찰 가능 상태 유무
    fun setUserName(name: String?)
    fun getUserName(): String
    fun setUserToken(token: String?)
    fun getUserToken(): String
    fun setUserNum(num: String?)
    fun getUserNum(): String
    fun setAuctionName(name: String)
    fun getAuctionName(): String
    fun setWatchAuctionName(name: String)
    fun getWatchAuctionName(): String
    fun setAuctionCode(code: String)
    fun getAuctionCode(): String
    fun setTraderMngNum(num: String)
    fun getTraderMngNum(): String
    fun setNearAuctionCode(code: String)
    fun isNearAuction(): Boolean
    fun isWatchMode(): Boolean
    fun setUserWatchToken(token: String?)
    fun getUserWatchToken(): String
}

class LoginManagerImpl @Inject constructor() : LoginManager {
    private var userName: String = ""
    private var userToken: String = "" // 토큰
    private var userNum: String = "" // 참가 번호
    private var auctionCode: String = "" // 지역 번호
    private var auctionName: String = "" // 조합명
    private var watchAuctionName: String = "" // 관전
    private var traderMngNum: String = "" // 거래 관리자 번호
    private var nearAuctionCode: String = ""
    private var userWatchToken: String = "" // 관전 토큰

    //  사용자의 정보가 유효한상태 -> 로그인 상태.
    override fun isLogin() = userToken.isNotEmpty() && traderMngNum.isNotEmpty()

    override fun setUserName(name: String?) {
        userName = name ?: ""
    }

    override fun setUserToken(token: String?) {
        userToken = token?.trim() ?: ""
    }

    override fun getUserName() = userName

    override fun getUserToken() = userToken

    override fun setUserNum(num: String?) {
        userNum = num ?: ""
    }

    override fun getUserNum() = if(userNum.trim().isEmpty()){
        ""
    }else{
        userNum.toIntOrDef(1).toString()
    }

    override fun setAuctionCode(code: String) {
        auctionCode = code
    }

    override fun getAuctionCode() = auctionCode

    override fun setAuctionName(name: String) {
        auctionName = name
    }

    override fun getAuctionName() = auctionName

    override fun setWatchAuctionName(name: String) {
        watchAuctionName = name
    }
    override fun getWatchAuctionName() = watchAuctionName

    override fun isAuctionAvailable(): Boolean {
        // 로그인 상태이고, 경매 지역을 선택한 경우.
        return isLogin() && getAuctionCode().isNotEmpty()
    }

    override fun setTraderMngNum(num: String) {
        traderMngNum = num
    }

    override fun getTraderMngNum() = traderMngNum

    override fun setNearAuctionCode(code: String) {
        nearAuctionCode = code
    }

    /**
     * 경매장과 가까이 있는 유무 처리 함수
     * @return true 가까이 있다. false 가까이 없다.
     */
    override fun isNearAuction(): Boolean {
        return if (auctionCode.isEmpty() || nearAuctionCode.isEmpty()) {
            false
        } else {
            auctionCode == nearAuctionCode
        }
    }

    override fun isWatchMode() = userWatchToken.isNotEmpty()

    override fun setUserWatchToken(token: String?) {
        userWatchToken = token?.trim() ?: ""
    }

    override fun getUserWatchToken() = userWatchToken
}
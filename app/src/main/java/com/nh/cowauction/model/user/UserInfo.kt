package com.nh.cowauction.model.user

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
        val success: Boolean = false,
        val userNum: String = "", // 거래인 관리 번호
        val auctionCode: String = "", // 조합 코드
        val userToken: String = "", // 토큰
        val userName: String = "", // 사용자 이름
        val nearestBranch: String = "", // 가장 가까운 경매장 코드 번호
        val auctionCodeName: String = ""   // 조합명
)
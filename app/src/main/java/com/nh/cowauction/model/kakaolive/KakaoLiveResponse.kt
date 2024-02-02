package com.nh.cowauction.model.kakaolive

import com.nh.cowauction.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class KakaoLiveResponse(
        @SerialName("kkoInfo")
        val info: KakaoLiveInfo = KakaoLiveInfo()
) : BaseResponse()

@Serializable
data class KakaoLiveInfo(
        @SerialName("KKO_SVC_ID")
        val serviceId: String = "",
        @SerialName("KKO_SVC_KEY")
        val serviceKey: String = "",
        @SerialName("KKO_SVC_CNT")
        val roomCnt : Int = 1
)
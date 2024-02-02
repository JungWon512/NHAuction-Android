package com.nh.cowauction.model.favorite

import com.nh.cowauction.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description : 찜 가격 조회 데이터 모델
 *
 * Created by hmju on 2021-09-29
 */
@Serializable
data class FavoriteEntryResponse(
        val info: EntryInfo = EntryInfo()
) : BaseResponse()

@Serializable
data class EntryInfo(
        @SerialName("SBID_UPR")
        val price: Int = 0
)

@Serializable
data class BiddingInfoResponse(
        @SerialName("data")
        val info: BiddingInfo = BiddingInfo()
) : BaseResponse()

@Serializable
data class BiddingInfo(
        val bidPrice : Int = 0, // 이전 응찰가
        val zimPrice : Int = 0 // 찜가격
)
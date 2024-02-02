package com.nh.cowauction.model.auction

import kotlinx.serialization.Serializable

@Serializable
data class WatchInfo(
    val url: String = "",                   // 관전 페이지 url
    val watch_token: String = ""    // 관전 토큰
)

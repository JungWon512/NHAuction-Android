package com.nh.cowauction.model.version

import com.nh.cowauction.model.base.BaseResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VersionResponse(
        val info: VersionData = VersionData()
) : BaseResponse()

@Serializable
data class VersionData(
        @SerialName("APP_VERSION_ID")
        val versionId: String = "",
        @SerialName("MAX_VERSION")
        val maxVersion: String = "",
        @SerialName("MIN_VERSION")
        val minVersion: String = "",
        @SerialName("NET_HOST")
        val tcpHost: String = "",
        @SerialName("NET_PORT")
        val tcpPort: String = ""
)
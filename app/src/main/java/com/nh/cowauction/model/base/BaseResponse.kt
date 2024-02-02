package com.nh.cowauction.model.base

import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by hmju on 2021-09-07
 */
@Serializable
open class BaseResponse {
    val success = false
    val message: String = ""
}
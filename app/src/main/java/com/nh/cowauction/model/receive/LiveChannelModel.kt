package com.nh.cowauction.model.receive

import androidx.lifecycle.MutableLiveData
import com.nh.cowauction.contants.CastState
import com.nh.cowauction.livedata.NonNullLiveData

/**
 * Description : 라이브 방송 송출 데이터 모델
 *
 * Created by hmju on 2021-06-29
 */
data class LiveChannelModel(
        val channelId: String,
        val castState: MutableLiveData<CastState> = MutableLiveData(),
        val isSound : MutableLiveData<Boolean> = NonNullLiveData(true)
)
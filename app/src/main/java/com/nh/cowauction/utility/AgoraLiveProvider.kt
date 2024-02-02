package com.nh.cowauction.utility

import android.content.Context
import com.nh.cowauction.contants.CastState
import com.nh.cowauction.extension.applyApiScheduler
import com.nh.cowauction.repository.http.ApiService
import com.nh.cowauction.repository.tcp.login.LoginManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject


/**
 * Description : 실시간 아고라 스트리밍 관련 제공자
 */

interface AgoraLiveProvider {
    fun initAgora(appId: String): RtcEngine

    fun clearServiceInfo()

    fun fetchServiceInfo(): Single<Triple<String, Int, ArrayList<String>?>>

    fun getList(): Single<ArrayList<String>>

    fun getAgoraInit(): Single<Pair<String, ArrayList<String>?>>

    fun disableVideo()

    fun enableVideo()
}

class AgoraLiveProviderImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val loginManager: LoginManager,
    private val apiService: ApiService
) : AgoraLiveProvider {

    // agora
    private var agoraAppId: String = "" // App ID
    private var maxRoomCnt: Int = 0 // 방 개수 (max 4)
    private var agoraChannelName: String = "" // Channel Name
    private var channelList: ArrayList<String>? = arrayListOf()

    // agora

    var engine: RtcEngine? = null

    override fun fetchServiceInfo(): Single<Triple<String, Int,ArrayList<String>?>> {

        if (agoraAppId.isEmpty()) {

            return apiService.fetchKakaoLiveService(loginManager.getAuctionCode())
                .applyApiScheduler()
                .flatMap { res ->
                    DLogger.d("### API res $res")
                    if (res.success) {
                        if (res.info.serviceId.isNotEmpty() && res.info.serviceKey.isNotEmpty()) {
                            val auctionHouseCode = loginManager.getAuctionCode() //조합코드

                            agoraAppId = res.info.serviceKey
                            maxRoomCnt = res.info.roomCnt

                            for (idx in 0 until maxRoomCnt) {
                                val rooms = auctionHouseCode.plus("-remoteVideo").plus(idx+1)
                                channelList?.add(rooms)
                            }

                            DLogger.d("### channelList $channelList")
                            // id, cnt, channel List 3개 값 리턴
                            return@flatMap Single.just(Triple(agoraAppId, maxRoomCnt, channelList))

                        } else {
                            return@flatMap Single.error(Throwable("Network Fail"))
                        }
                    } else {
                        return@flatMap Single.error(Throwable("Network Fail"))
                    }
                }

        } else {
            // id, cnt, channel List 3개 값 리턴
            return Single.just(Triple(agoraAppId, maxRoomCnt, channelList))
        }
    }

    // channel List 리턴
    override fun getList():Single<ArrayList<String>> =
        fetchServiceInfo().map { info ->
            return@map info.third
        }

    // app id, channel List 리턴
    override fun getAgoraInit(): Single<Pair<String, ArrayList<String>?>> =
        fetchServiceInfo().map { info ->
            return@map info.first to info.third
        }

    // provider clear 로직
    override fun clearServiceInfo() {
        agoraAppId = ""
        maxRoomCnt = 1
        channelList = arrayListOf()
        leaveChannel()
    }

    private fun leaveChannel() {
        engine!!.leaveChannel()
        RtcEngine.destroy()
        engine = null
    }

    override fun disableVideo() {
        engine!!.disableVideo()
        engine!!.disableAudio()
    }

    override fun enableVideo() {
        engine!!.enableAudio()
        engine!!.enableVideo()
    }


    override fun initAgora(appId: String): RtcEngine {
        DLogger.d("### initAgora 탐 appID : $appId")

        val config = RtcEngineConfig().apply {
            mContext = ctx
            mAppId = appId
            mEventHandler = rtcEventHandler
        }

        return RtcEngine.create(config).apply {
            DLogger.d("### enableVideo 탐")
            engine = this
            enableVideo()
        }
    }

    /**
     * Agora engine handler
     */
    private val rtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

        // onUserJoined에서 콜백받은 uid 넘겨줘야함
        override fun onUserJoined(uid: Int, elapsed: Int) {
            DLogger.d("### onUserJoined $uid")
            // 1. 채널 생성 성공했을 경우
            RxBus.publish(RxBusEvent.AgoraCallbackEvent(CastState.JOIN, engine, uid))
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            agoraChannelName = channel
            DLogger.d("### onJoinChannelSuccess $channel")
        }

        // 서버에서 끊었을때
        override fun onUserOffline(uid: Int, reason: Int) {
            DLogger.d("### onUserOffline")
            RxBus.publish(RxBusEvent.AgoraCallbackEvent(CastState.STOP, engine, uid))
        }

        //서버에서 사이즈 변경했을때
        override fun onVideoSizeChanged(
            source: Constants.VideoSourceType?,
            uid: Int,
            width: Int,
            height: Int,
            rotation: Int
        ) {
            super.onVideoSizeChanged(source, uid, width, height, rotation)
            DLogger.d("### onVideoSizeChanged")
        }

    }

}
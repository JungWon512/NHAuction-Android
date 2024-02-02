package com.nh.cowauction.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.nh.cowauction.base.BaseViewModel
import com.nh.cowauction.contants.CastState
import com.nh.cowauction.contants.Config
import com.nh.cowauction.contants.ExtraCode
import com.nh.cowauction.livedata.NonNullLiveData
import com.nh.cowauction.livedata.SingleLiveEvent
import com.nh.cowauction.repository.http.ApiService
import com.nh.cowauction.repository.tcp.login.LoginManager
import com.nh.cowauction.utility.AgoraLiveProvider
import com.nh.cowauction.utility.DLogger
import com.nh.cowauction.utility.DeviceProvider
import com.nh.cowauction.utility.RxBus
import com.nh.cowauction.utility.RxBusEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.base.internal.SurfaceViewRenderer
import io.agora.rtc2.RtcEngine
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import javax.inject.Inject


/**
 * Description :
 *
 * Created by hmju on 2021-06-29
 */
@HiltViewModel
class AuctionCamViewModel @Inject constructor(
    private val arguments: SavedStateHandle,
    private val deviceProvider: DeviceProvider,
    val agoraLiveProvider: AgoraLiveProvider,
) : BaseViewModel() {

    // 1 부터 시작
    val position: Int = arguments.get<Int>(ExtraCode.AUCTION_CAM_POS) ?: 1
    private var channelId: String =
        arguments.get<String>(ExtraCode.AUCTION_CAM_CHANNEL_ID) ?: Config.INVALID_CHANNEL_ID

    val _liveCastState: NonNullLiveData<CastState> by lazy { NonNullLiveData(CastState.STOP) }
    val liveCastState: LiveData<CastState> get() = _liveCastState
    val _liveSoundState: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    val liveSoundState: NonNullLiveData<Boolean> get() = _liveSoundState
    val playPosition: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }     // 재생 시켜야할 포지션

    val thumbnailState: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    // agora
    var engine: RtcEngine? = null
    var agoraChannelUid: Int = 0 // Channel uid (기본값 0)
    var channellist = arrayListOf<String>()
    val playHandler: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) } // 핸들러 play
    private val curSoundstate: LiveData<Boolean> get() = _curSoundstate
    private val _curSoundstate: NonNullLiveData<Boolean> by lazy { NonNullLiveData(false) }
    //agora

    var isJoined : Boolean = false    // KIH 추가


    fun start() {
        val list = agoraLiveProvider.getList()

        list.subscribe { list ->
            channellist = list
            DLogger.d("### channellist $channellist")
        }

        // activity -> 방송 재생 처리 관련해서 listen 받는 rxbus
        // 이벤트가 와야 탐
        RxBus.listen(RxBusEvent.LiveCastEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ event ->
                DLogger.d("### RxBus 탐 LiveEvent State  Event Position ${event.position} CurrPosition $position 사운드상태 ${liveSoundState.value}")

                // 0, 1 위치값
                if (event.position == 0 && position == 1) {

                    if(!liveSoundState.value ) {
                        DLogger.d("### _liveCastState.value = CastState.STOP")
                        _liveCastState.value = CastState.STOP
                    }

                } else if (event.position == position) {

                    // ViewPager Position 이랑 카메라 포지션과 같은 경우 실행
                    if (position != 1 || event.position != 1 || liveCastState.value != CastState.PLAYING) {
                        DLogger.d("### _liveCastState.value = CastState.JOIN")
                        _liveCastState.value = CastState.JOIN
                    }

                } else {
                    // 이외케이스 멈춤.
                    DLogger.d("### _liveCastState.value = CastState.STOP")
                    _liveCastState.value = CastState.STOP
                }

                engine = event.engine // activity에서 미리 초기화 한 엔진 세팅
                playPosition.value = event.position // 영상 플레이 해야 하는 위치값 처리

            }, {}).addTo(compositeDisposable)

        // 영상 사운드 관련 rxbus 처리
        RxBus.listen(RxBusEvent.LiveSoundEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                DLogger.d("### BusSoundEvent CamPos $position Play Position ${playPosition.value} $it")

                if (it.isBackGround) {
                    // background 진입 시 소리 off
                    DLogger.d("### BusSoundEvent isBackGround")
                    setLiveVolume(false)

                } else if (it.isForeGround) {
                    DLogger.d("### BusSoundEvent isForeGround")

                    if (curSoundstate.value == true) {

                        _liveSoundState.value = true

                        if (deviceProvider.getVolume() == 0) {
                            deviceProvider.setVolume(deviceProvider.getMaxVolume() / 2)
                        }

                    } else {
                        _liveSoundState.value = false
                    }

                } else {

                    if (it.isSoundOn) {
                        DLogger.d("### it.isSoundOn true")

                        if (deviceProvider.getVolume() == 0) {
                            deviceProvider.setVolume(deviceProvider.getMaxVolume() / 2)
                        }

                        _curSoundstate.value = true

                    } else {
                        _curSoundstate.value = false
                    }

                    _liveSoundState.value = it.isSoundOn

                }

            }, {}).addTo(compositeDisposable)
    }

    /**
     * 액티비티에서 아고라 채널 전부 Join 후 제일 처음 받는 콜백
     */
    fun agoraLiveEventCallback() {
        RxBus.listen(RxBusEvent.AgoraCallbackEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                DLogger.d("### AgoraCallbackEvent state ${it.state} / playPosition.value ${playPosition.value} / position $position / agoraChannelUid ${it.uid} / engine : ${it.engine} / liveSoundState : ${liveSoundState.value}")

                // KIH 추가 - 아고라 join 상태 update
                isJoined = it.state == CastState.JOIN

                engine = it.engine
                agoraChannelUid = it.uid

                // 디폴트 소리 값 무조건 false
                setLiveVolume(liveSoundState.value)

                if (playPosition.value == 0 && position == 1) {
                    videoStateChange(it.state)

                } else {

                    if (position == playPosition.value) {
                        videoStateChange(it.state)
                    }
                }

            }, {}).addTo(compositeDisposable)
    }

    /**
     * 라이브 방송 볼륨 On / Off
     */
    fun setLiveVolume(isSoundOn: Boolean) {
        engine?.run {
            this.adjustAudioMixingPublishVolume(if (isSoundOn) 100 else 0)
            this.adjustPlaybackSignalVolume(if (isSoundOn) 100 else 0)
        }
    }


    /**
     *  CastState.JOIN : playHandler가 true일때만 뷰 그리기
     *  CastState.STOP : stop일 경우 썸네일 노출
     */
    fun videoStateChange(castState : CastState){

        DLogger.d("### called videoStateChange : $castState ")

        if (castState == CastState.JOIN) {
            playHandler.value = true
        } else if(castState == CastState.STOP) {
            thumbnailState.call()
        }
    }

    fun stopAgoraLive() {
        // 아고라 초기화 전에 clear되어버리는 시점 이슈가 간헐적으로 발생하여 프래그먼트 뷰 모델 내부에서 처리함
//        agoraLiveProvider.clearServiceInfo()
        DLogger.d("### called stopAgoraLive() - position : $position  ")
        engine?.leaveChannel()
        agoraChannelUid = 0
        isJoined = false    // KIH 추가
    }

    fun onClickThumbNail(view: SurfaceViewRenderer) {
        if (view == null) return

        DLogger.d("onClick $view $channelId ${liveCastState.value}")

        if (channelId == Config.INVALID_CHANNEL_ID) {
            _liveCastState.value = CastState.JOIN

            // CastState.ERROR 일 경우 썸네일 노출됨
        } else if (channelId == Config.INVALID_CHANNEL_ID && liveCastState.value == CastState.JOIN) {
            _liveCastState.value = CastState.ERROR
        }
    }

    override fun onCleared() {
        DLogger.d("### called onCleared() - AuctionCamViewModel")
        stopAgoraLive()
        super.onCleared()
    }
}
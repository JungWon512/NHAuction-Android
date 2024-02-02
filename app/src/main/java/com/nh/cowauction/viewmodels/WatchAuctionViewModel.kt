package com.nh.cowauction.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.nh.cowauction.base.BaseViewModel
import com.nh.cowauction.contants.*
import com.nh.cowauction.extension.multiNullCheck
import com.nh.cowauction.livedata.ListLiveData
import com.nh.cowauction.livedata.NonNullLiveData
import com.nh.cowauction.livedata.SingleLiveEvent
import com.nh.cowauction.model.receive.*
import com.nh.cowauction.repository.http.ApiService
import com.nh.cowauction.repository.tcp.NettyClient
import com.nh.cowauction.repository.tcp.SimpleOnReceiveMessage
import com.nh.cowauction.repository.tcp.login.LoginManager
import com.nh.cowauction.utility.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

/**
 * Description : 경매 관전 TCP 데이터 처리 관련 ViewModel
 *
 * Created by jhlee on 2022-01-20
 */
@HiltViewModel
class WatchAuctionViewModel @Inject constructor(
        private val loginManager: LoginManager,
        private val nettyClient: NettyClient,
        val deviceProvider: DeviceProvider,
        val agoraLiveProvider: AgoraLiveProvider,
        savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val _webUrl = savedStateHandle.getLiveData<String>(ExtraCode.WATCH_AUCTION_URL)
    val webUrl: LiveData<String> get() = _webUrl

    // SEQ-> 순차 경매, BATCH_SELECTION -> 일괄 경매 선택 , BATCH_BIDDING -> 일괄 경매 응찰
    private val _auctionOperation: NonNullLiveData<AuctionOperation> by lazy {
        NonNullLiveData(
                AuctionOperation.SEQ
        )
    }

    val auctionOperation: LiveData<AuctionOperation> get() = _auctionOperation

    // 경매 종료 팝업
    val startAuctionFinish: MutableLiveData<NetworkAuctionConnection> by lazy { MutableLiveData() }
    val startFetchAuction: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startReadPhonePermissions: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    val startOneButtonPopup: MutableLiveData<String> by lazy { MutableLiveData() }

    // [s] 상단 ViewPager
    val topCurrentPos: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }
    val topPrevPos: NonNullLiveData<Int> by lazy { NonNullLiveData(0) } // TopViewPager 이전 포지션 값
    val topCurrentState: MutableLiveData<Int> by lazy { MutableLiveData() }
    private val _countDownNum: MutableLiveData<Int> by lazy { MutableLiveData() }
    val countDownNum: LiveData<Int> get() = _countDownNum // 카운트 다운 숫자.
    // [e] 상단 ViewPager


    private val _liveChannelList: ListLiveData<String> by lazy { ListLiveData() }
    val liveChannelList: ListLiveData<String> get() = _liveChannelList // 라이브 방송 채널 목록

    // 경매 상태값
    private val auctionState: NonNullLiveData<AuctionState> by lazy { NonNullLiveData(AuctionState.READY) }

    // 현재 경매 마감하는 중인지 유무 상태. true 마감중, false 마감 X
    private val _isAuctionCountDown = NonNullLiveData(false)
    val isAuctionCountDown: LiveData<Boolean> get() = _isAuctionCountDown

    // Toast 메시지
    private val _toastMessage: MutableLiveData<String> by lazy { MutableLiveData() }
    val toastMessage: LiveData<String> get() = _toastMessage

    // 출품 Origin 정보
    private val currentEntryInfo: MutableLiveData<CurrentEntryInfo> by lazy { MutableLiveData() }

    private val _currentEntryTitle: MutableLiveData<CurrentEntryTitle> by lazy { MutableLiveData() }
    val currentEntryTitle: LiveData<CurrentEntryTitle> get() = _currentEntryTitle
    private val _currentEntryContents: MutableLiveData<CurrentEntryContents> by lazy { MutableLiveData() }
    val currentEntryContents: LiveData<CurrentEntryContents> get() = _currentEntryContents

    private val auctionType: MutableLiveData<AuctionType> by lazy { MutableLiveData() }

    val currentEntryInfoMap = mutableMapOf<String,CurrentEntryInfo>()

    val isWatch: NonNullLiveData<Boolean> by lazy {
        NonNullLiveData(
            loginManager.isWatchMode()
        )
    }

    // agora
    var engine: RtcEngine? = null
    var list = arrayListOf<String>()
    var agoraInitData: Single<Pair<String, ArrayList<String>?>>? = null
    //agora

    // [e] 서버에서 받은 데이터 정보들

    // 서버로 부터 받은 메시지 CallBack Listener
     val onReceiveMessage = object : SimpleOnReceiveMessage() {

        override fun onAuctionType(data: AuctionType) {
            auctionType.value = data
            DLogger.d("경매 타입 :  " + data.auctionType)
        }

        override fun onConnectionInfo(data: ResponseConnectionInfo) {
            onLoadingDismiss()
            if (data.connectionCode != ConnectionCode.SUCC.code) {
                DLogger.d("onConnectionInfo $data")
                nettyClient.clearListener()
                when (data.connectionCode) {
                    ConnectionCode.DUPLICATE.code -> {
                        startAuctionFinish.postValue(NetworkAuctionConnection.DUPLICATE)
                    }
                    ConnectionCode.FAIL.code -> {
                        startAuctionFinish.postValue(NetworkAuctionConnection.FAIL)
                    }
                    else -> {
                        startAuctionFinish.postValue(NetworkAuctionConnection.DEFAULT)
                    }
                }
            }
        }

        override fun onCurrentEntryTitle(data: CurrentEntryTitle) {
            _currentEntryTitle.postValue(data)
            multiNullCheck(currentEntryInfo.value, data) { contents, title ->
                _currentEntryContents.postValue(parseCurrentContents(contents, title))
            }
        }

        override fun onCurrentEntryInfo(data: CurrentEntryInfo) {

                currentEntryInfoMap[data.entryNum] = data

                currentEntryInfo.postValue(data)
                multiNullCheck(data, currentEntryTitle.value) { contents, title ->
                    _currentEntryContents.postValue(parseCurrentContents(contents, title))
                }

        }

        private fun setCurrentEntryContents(number : String){

            val data = currentEntryInfoMap[number]

            currentEntryInfo.postValue(data)

            multiNullCheck(data, currentEntryTitle.value) { contents, title ->
                _currentEntryContents.postValue(parseCurrentContents(contents, title))
            }

        }

        override fun onMessage(data: ToastMessage) {
            // 일반 메시지
            if (data.msg.isEmpty()) return

            _toastMessage.postValue(data.msg)
        }

        override fun onCountDown(data: AuctionCountDown) {
            // 경매 상태 및 카운트 다운.
            if (data.auctionCountDownType == null) return

            // 카운트 다운 종료시 카운트 다운 ui 숨김 처리
            if (data.auctionCountDownType == AuctionState.UNKNOWN) {
                _isAuctionCountDown.postValue(false)
//                _auctionStateMessage.postValue(AuctionState.READY)
                return
            }

            if (data.auctionCountDownType == AuctionState.COUNT_DOWN) {
                _isAuctionCountDown.postValue(true)
                _countDownNum.postValue(data.auctionCountDownType?.etc?.get(0))
            } else {
                _isAuctionCountDown.postValue(false)
            }
        }

        override fun onStatus(data: AuctionStatus) {
            DLogger.d("onStatus $data")
            // 경매 타입에 따라 분기 처리
                handleAuctionState(data.auctionState)
                if (data.auctionState == AuctionState.PROGRESS) {
                    //cowInfo
                    setCurrentEntryContents(data.num)
                    //clear cow info map
                    currentEntryInfoMap.clear();
                }
        }


        /**
         * 서버에서 받은 경매 상태 코드 값에 따라서 메시지 처리.
         * @param state 서버에서 받은 경매 코드 값
         */
        private fun handleAuctionState(state: AuctionState?, isMainThread: Boolean = false) {
            if (state == null) return
            when (state) {
                AuctionState.NONE -> {
                    if (isMainThread) {
                        startAuctionFinish.value = NetworkAuctionConnection.NONE
                    } else {
                        startAuctionFinish.postValue(NetworkAuctionConnection.NONE)
                    }
                }
                AuctionState.FINISH -> {
                    if (isMainThread) {
                        startAuctionFinish.value = NetworkAuctionConnection.FINISH
                    } else {
                        startAuctionFinish.postValue(NetworkAuctionConnection.FINISH)
                    }

                }
            }
        }

        override fun onResponseCode(data: ResponseCode) {
            DLogger.d("onResponseCode $data")
        }

        override fun onDisconnected() {
            onLoadingDismiss()
            startAuctionFinish.postValue(NetworkAuctionConnection.RECONNECT)
        }

        override fun onAuctionResult(data: AuctionResult) {
            DLogger.d("onAuctionResult $data")
        }

        override fun onRetryTargetInfo(data: RetryTargetInfo) {
            DLogger.d("onRetryTargetInfo $data")
        }

        override fun onBiddingStatus(data: AuctionBidStatus) {
            DLogger.d("onBiddingStatus $data")
        }
    }

    /**
     * init Start
     */
    fun start() {

        // 경매중일때 들어온경우 이전 데이터에 대한 처리
        with(nettyClient) {

            prevAuctionType()?.let {
                // 10 일괄 경매, 20 단일 경매.
                if (it.auctionType == "10") {
                    _auctionOperation.value = AuctionOperation.BATCH_SELECTION
                } else {
                    _auctionOperation.value = AuctionOperation.SEQ
                }
            }

            prevResponseCode()?.let {
                // 출품 이관 전상태인경우 튕기기 (관전 제외)
//                if (it.codeType == ResponseCodeType.AUCTION_BEFORE_START) {
//                    startAuctionFinish.value = NetworkAuctionConnection.NONE
//                }
            }

            prevEntryTitle()?.let { _currentEntryTitle.value = it }

            prevEntryInfo()?.let {
                currentEntryInfo.value = it
            }

            multiNullCheck(currentEntryTitle.value, currentEntryInfo.value) { title, data ->
                _currentEntryContents.value = parseCurrentContents(data, title)
            }

            setListener(onReceiveMessage)

            // 전화 권한 승인
            if (deviceProvider.isPermissionsCheck(Manifest.permission.READ_PHONE_STATE)) {
                fetchRooms()
            } else {
                startReadPhonePermissions.call()
            }
        }
    }

    /**
     * 방송 서비스 갱신 처리
     */
    fun refreshRooms() {
        if (deviceProvider.isPermissionsCheck(Manifest.permission.READ_PHONE_STATE)) {
            fetchRooms()
        }
    }

    /**
     * 경매 라이브 룸 조회 처리 함수.
     */

    @SuppressLint("CheckResult")
    fun fetchRooms() {
        DLogger.d("### fetchRooms (채널 개수만큼 뷰페이저에 세팅)")

        agoraInitData = agoraLiveProvider.getAgoraInit()
        if (agoraInitData != null) {
            agoraInitData?.subscribe { it ->
                engine = agoraLiveProvider.initAgora(it.first)
                if (it.second != null) {
                    list = it.second!!
                }

                DLogger.d("### agoraInitData ${it.first} / ${it.second}")

                // list 0번째 아고라에 미리 조인
                //agoraJoin(list[0])        // KIH 2024.01.23 주석처리

                _liveChannelList.clear()
                _liveChannelList.addAll(list)

                // KIH 2024.01.23 추가
                Handler(Looper.getMainLooper()).postDelayed({
                    startAgoraEvent(0)
                }, 300)
            }
        }
    }

    /**
     * 첫번째 영상 미리 Join
     * 출품 화면에서도 소리 미리 들리게 하기 위한 코드
     */
    /*  // KIH 주석처리
    private fun agoraJoin(channelId: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            val options = ChannelMediaOptions()
            options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING

            options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE // 수신자
            engine?.joinChannel("", channelId, 0, options)
        }, 300)
        DLogger.d("### Act handler list ::: $list / engine ::: $engine")
    }
    */

    /**
     * 방송 재생 처리 함수
     * @param pos 재생하고 싶은 포지션 값
     */
    fun startAgoraEvent(pos: Int) {
        DLogger.d("### 카카오 방송 재생 처리 함수 탐 - pos: $pos")
        RxBus.publish(RxBusEvent.LiveCastEvent(pos, CastState.JOIN, engine))
    }

    /**
     * ViewPager <<. >> 이동
     * @param isLeft true -> 왼쪽, false -> 오른쪽
     */
    fun moveTopViewPager(isLeft: Boolean) {
        if (isLeft) {
            topCurrentPos.value = topCurrentPos.value.minus(1)
        } else {
            topCurrentPos.value = topCurrentPos.value.plus(1)
        }
    }

    /**
     * Top ViewPager 스크롤 상태에 대한 값
     * @param state ViewPager2.State
     */
    fun onTopPageState(state: Int) {
        topCurrentState.value = state
    }

    /**
     * 개체 제목 정보와 개체 전체 정보에 따라서 CurrentEntryContents 데이터 모델로
     * 가공 처리 하는 함수.
     * @param info 개체 전체 정보
     * @param title 나타내고자 하는 제목 정보.
     */
    private fun parseCurrentContents(
            info: CurrentEntryInfo,
            title: CurrentEntryTitle
    ): CurrentEntryContents {
        return CurrentEntryContents(
                one = info.toEntryContents(title.one),
                two = info.toEntryContents(title.two),
                three = info.toEntryContents(title.three),
                four = info.toEntryContents(title.four),
                five = info.toEntryContents(title.five),
                six = info.toEntryContents(title.six),
                seven = info.toEntryContents(title.seven),
                eight = info.toEntryContents(title.eight),
                nine = info.toEntryContents(title.nine),
                ten = info.toEntryContents(title.ten)
        )
    }

    /**
     * 응찰 상태값 처리.
     */
    fun setAuctionState(state: AuctionState) {
        DLogger.d("AuctionState ${state}")
        auctionState.value = state
    }

    fun clearAgoraServiceInfo() {
        agoraLiveProvider.clearServiceInfo()
    }

    /**
     * TCP 접속 해제 처리 함수
     */
    fun closeClient() {
        nettyClient.closeClientSingle()
                .doLoading()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    nettyClient.clearListener()
                    clearVariable()
                    onLoadingDismiss()
                    startFinish.call()
                }, {

                }).addTo(compositeDisposable)
    }

    private fun clearVariable(){
        loginManager.setUserWatchToken("")
    }

    /**
     * 재접속 처리 함수.
     */
    fun refreshClient() {
        // 서버가 켜져 있는 경우 끄고 나서 재접속 처리
        if (nettyClient.isServerOn()) {
            DLogger.d("Refresh Client 서버 ON")
            nettyClient.closeClientSingle()
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        DLogger.d("종료 완료 재접속 시작")
                        reConnection()
                        refreshRooms()
                    }, {
                        DLogger.d("Error $it")
                    }).addTo(compositeDisposable)
        } else {
            // 서버가 꺼진경우 그냥 재접속 처리
            reConnection()
            refreshRooms()
        }
    }

    /**
     * 서버 재접속 처리
     */
    private fun reConnection() {
        Thread {
            onLoadingShow()
            nettyClient.reConnect()
        }.start()
    }

    override fun onCleared() {
        nettyClient.clearListener()
        super.onCleared()
    }

}
package com.nh.cowauction.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseViewModel
import com.nh.cowauction.contants.*
import com.nh.cowauction.extension.*
import com.nh.cowauction.livedata.ListLiveData
import com.nh.cowauction.livedata.NonNullLiveData
import com.nh.cowauction.livedata.SingleLiveEvent
import com.nh.cowauction.model.receive.*
import com.nh.cowauction.model.send.Bidding
import com.nh.cowauction.model.send.CancelBidding
import com.nh.cowauction.model.send.RequestEntryInfo
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
 * Description : 경매 TCP 데이터 처리 관련 ViewModel
 *
 * Created by hmju on 2021-05-28
 */
@HiltViewModel
class AuctionViewModel @Inject constructor(
    private val loginManager: LoginManager,
    private val nettyClient: NettyClient,
    private val resProvider: ResourceProvider,
    private val apiService: ApiService,
    private val deviceProvider: DeviceProvider,
    val agoraLiveProvider: AgoraLiveProvider,
) : BaseViewModel() {

    private val MAX_PRICE_RANGE = 5 // 응찰 가격 범위
    private val MAX_ENTRY_NUM_RANGE = 3 // 경매 번호 범위

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
    private val _auctionSnackType: MutableLiveData<AuctionSnackType> by lazy { MutableLiveData() }
    val auctionSnackType: LiveData<AuctionSnackType> get() = _auctionSnackType // 큰 Toast 화면에 대한 데이터

    // [s] 상단 ViewPager
    val topCurrentPos: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }
    val topPrevPos: NonNullLiveData<Int> by lazy { NonNullLiveData(0) } // TopViewPager 이전 포지션 값
    val topCurrentState: MutableLiveData<Int> by lazy { MutableLiveData() }
    private val _countDownNum: MutableLiveData<Int> by lazy { MutableLiveData() }
    val countDownNum: LiveData<Int> get() = _countDownNum // 카운트 다운 숫자.
    // [e] 상단 ViewPager

    // [s] 하단 ViewPager
    val startShake: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    private val _biddingColor: MutableLiveData<Int> by lazy { MutableLiveData(R.color.color_1a1a1a) }
    val biddingColor: LiveData<Int> get() = _biddingColor
    // [e] 하단 ViewPager

    // [s] 서버에서 받은 데이터 정보들
    private val _biddingPrice: NonNullLiveData<Int> by lazy { NonNullLiveData(0) }
    val biddingPrice: NonNullLiveData<Int> get() = _biddingPrice // 응찰 할 가격
    private val _biddingEntryNum: MutableLiveData<Int> by lazy { MutableLiveData() }
    val biddingEntryNum: LiveData<Int> get() = _biddingEntryNum // 경매 번호

    private val _liveChannelList: ListLiveData<String> by lazy { ListLiveData() }
    val liveChannelList: ListLiveData<String> get() = _liveChannelList // 라이브 방송 채널 목록

    // 경매 상태값
    private val _auctionStateMessage = NonNullLiveData(AuctionState.READY)
    val auctionStateMessage: NonNullLiveData<AuctionState> get() = _auctionStateMessage
    private val auctionState: NonNullLiveData<AuctionState> by lazy { NonNullLiveData(AuctionState.READY) }
    private var isBiddingSuccess: Boolean = false // 응찰 시도 후 완료된 상태

    // 현재 경매 마감하는 중인지 유무 상태. true 마감중, false 마감 X
    private val _isAuctionCountDown = NonNullLiveData(false)
    val isAuctionCountDown: LiveData<Boolean> get() = _isAuctionCountDown

    // Toast 메시지
    private val _toastMessage: MutableLiveData<String> by lazy { MutableLiveData() }
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _snackMsg: MutableLiveData<String> by lazy { MutableLiveData() }
    val snackMsg: LiveData<String> get() = _snackMsg

    private val biddingPos: MutableLiveData<String> by lazy { MutableLiveData() } // 응찰한 번호값

    // 출품 Origin 정보
    private val currentEntryInfo: MutableLiveData<CurrentEntryInfo> by lazy { MutableLiveData() }
    private var retryTargetInfo: RetryTargetInfo? = null

    private val _currentEntryTitle: MutableLiveData<CurrentEntryTitle> by lazy { MutableLiveData() }
    val currentEntryTitle: LiveData<CurrentEntryTitle> get() = _currentEntryTitle
    private val _currentEntryContents: MutableLiveData<CurrentEntryContents> by lazy { MutableLiveData() }
    val currentEntryContents: LiveData<CurrentEntryContents> get() = _currentEntryContents

    val currentEntryInfoMap = mutableMapOf<String, CurrentEntryInfo>()

    private val _isWonObjectType2: MutableLiveData<Boolean> by lazy { MutableLiveData(true) }
    val isWonObjectType2: LiveData<Boolean> get() = _isWonObjectType2

    // agora
    var engine: RtcEngine? = null
    var list = arrayListOf<String>()
    var agoraInitData: Single<Pair<String, ArrayList<String>?>>? = null
    //agora

    // [e] 서버에서 받은 데이터 정보들

    // 서버로 부터 받은 메시지 CallBack Listener
    private val onReceiveMessage = object : SimpleOnReceiveMessage() {

        override fun onAuctionType(data: AuctionType) {
            // 10 일괄 경매, 20 단일 경매.
            // 같은 타입인 경우 스킵
            if (data.auctionType == "10" && auctionOperation.value != AuctionOperation.SEQ) {
                DLogger.d("같은 경매 타입입니다. 일괄")
                return
            } else if (data.auctionType == "20" && auctionOperation.value == AuctionOperation.SEQ) {
                DLogger.d("같은 경매 타입입니다. 단일")
                return
            } else {
                if (data.auctionType == "10") {
                    _auctionOperation.postValue(AuctionOperation.BATCH_SELECTION)
                    _auctionStateMessage.postValue(AuctionState.BATCH_SELECTION)
                } else {
                    _auctionOperation.postValue(AuctionOperation.SEQ)
                }
            }
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
            // 단일 경매
            if (auctionOperation.value == AuctionOperation.SEQ) {

                currentEntryInfoMap[data.entryNum] = data

                if (currentEntryInfo != null) {
                    if (data.entryNum == currentEntryInfo.value!!.entryNum) {
                        currentEntryInfo.postValue(data)
                        multiNullCheck(data, currentEntryTitle.value) { contents, title ->
                            _currentEntryContents.postValue(parseCurrentContents(contents, title))
                        }
                        // 재응찰 데이터 초기화
                        retryTargetInfo = null
                    }
                }
            } else {
                // 일괄 경매
                if (auctionState.value == AuctionState.PROGRESS) {
                    currentEntryInfo.postValue(data)
                    multiNullCheck(data, currentEntryTitle.value) { contents, title ->
                        _currentEntryContents.postValue(parseCurrentContents(contents, title))
                    }

                    if (data.entryNum.isNotEmpty()) {
                        apiService.fetchBiddingInfo(
                            houseCode = loginManager.getAuctionCode(),
                            userMngNum = loginManager.getUserNum(),
                            entryType = data.entryType,
                            entryNum = data.entryNum
                        ).observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                DLogger.d("이전가 찜가격 조회 ${response.info}")
                                if (response.success) {
                                    if (response.info.bidPrice > 0) {
                                        _biddingPrice.value = response.info.bidPrice
                                        isBiddingSuccess = true
                                    } else if (response.info.zimPrice > 0) {
                                        _biddingPrice.value = response.info.zimPrice
                                        _biddingColor.value = R.color.color_2d9cff
                                        isBiddingSuccess = true
                                    }
                                }
                            }, {

                            }).addTo(compositeDisposable)

                        _auctionOperation.postValue(AuctionOperation.BATCH_BIDDING)
                        _auctionStateMessage.postValue(AuctionState.BATCH_BIDDING)
                        _biddingEntryNum.postValue(0)
                    }
                }
            }
        }

        private fun setCurrentEntryContents(number: String) {

            val data = currentEntryInfoMap[number]

            currentEntryInfo.postValue(data)

            multiNullCheck(data, currentEntryTitle.value) { contents, title ->
                _currentEntryContents.postValue(parseCurrentContents(contents, title))
            }

            // 재응찰 데이터 초기화
            retryTargetInfo = null
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

            _auctionStateMessage.postValue(data.auctionCountDownType)

            if (data.auctionCountDownType == AuctionState.COUNT_DOWN) {
                _isAuctionCountDown.postValue(true)
                _countDownNum.postValue(data.auctionCountDownType?.etc?.get(0))
            } else {
                _isAuctionCountDown.postValue(false)
            }
        }

        override fun onStatus(data: AuctionStatus) {
            // 경매 타입에 따라 분기 처리
            if (auctionOperation.value == AuctionOperation.SEQ) {
                handleAuctionState(data.auctionState)
                if (data.auctionState == AuctionState.PROGRESS) {

                    //cowInfo
                    setCurrentEntryContents(data.num)

                    // 찜가격 조회
                    currentEntryInfo.value?.let {
                        fetchFavoriteEntry(it, data)
                    }
                    //clear cow info map
                    currentEntryInfoMap.clear();
                }
            } else {
                // 일괄 경매
                handleAuctionState(data.auctionState)
            }
        }

        override fun onResponseCode(data: ResponseCode) {
            // 응찰 취소시 점점 사라지는 화면 4007
            if (data.codeType == ResponseCodeType.BIDDING_CANCEL) {
                _snackMsg.postValue(resProvider.getString(data.codeType!!.id))
                if (auctionOperation.value == AuctionOperation.SEQ) {
                    _auctionStateMessage.postValue(AuctionState.START)
                } else {
                    // 경매번호 상태로
                    _auctionOperation.postValue(AuctionOperation.BATCH_SELECTION)
                    _auctionStateMessage.postValue(AuctionState.BATCH_SELECTION)
                    _biddingPrice.postValue(0)
                    _biddingEntryNum.postValue(0)
                }
            } else if (data.codeType == ResponseCodeType.BIDDING_SUCCESS) {
                // 응찰 성공시 응찰 되었습니다. 상태 메시지 변경 4006
                // 순차일때 응찰 완료 상태값
                if (auctionOperation.value == AuctionOperation.SEQ) {
                    _auctionStateMessage.postValue(AuctionState.BIDDING_SUCCESS)
                    _auctionSnackType.postValue(AuctionSnackType.BIDDING_SUCCESS)
                    isBiddingSuccess = true
                } else {
                    // 응찰 성공후 경매 번호 화면으로 넘기기
                    initBiddingColor()
                    _auctionSnackType.postValue(AuctionSnackType.BIDDING_SUCCESS)
                    _auctionOperation.postValue(AuctionOperation.BATCH_SELECTION)
                    _auctionStateMessage.postValue(AuctionState.BATCH_SELECTION)
                    _biddingPrice.postValue(0)
                    isBiddingSuccess = false
                }
            } else if (data.codeType == ResponseCodeType.UNKNOWN) {
                // 경매 번호가 없습니다. 4001
                DLogger.d("경매 번호가 없습니다. ")
                startOneButtonPopup.postValue(resProvider.getString(R.string.str_auction_invalid_current_entry_info))
                _biddingEntryNum.postValue(0)
            } else if (data.codeType == ResponseCodeType.NOT_ENOUGH_BIDDING) {
                // 최저가 거나 말도안되는 금액이거나 4003
                initBiddingColor()
                _snackMsg.postValue(resProvider.getString(R.string.str_auction_response_price_not_enough))
                _biddingPrice.postValue(0)
            } else if (data.codeType == ResponseCodeType.FAIL) {
                // 현재 응찰 상태가 아닙니다. 4002
                if (auctionOperation.value != AuctionOperation.SEQ) {
                    _snackMsg.postValue(resProvider.getString(R.string.str_invalid_bidding))
                }
            } else if (data.codeType == ResponseCodeType.BIDDING_CANCEL_FAIL) {
                // 일괄에서 취소 불가능상태인경우 안내 문구 노출 4005
                if (auctionOperation.value != AuctionOperation.SEQ) {
                    _snackMsg.postValue(resProvider.getString(R.string.str_auction_invalid_bidding_cancel))
                }
            }
        }

        override fun onDisconnected() {
            onLoadingDismiss()
            startAuctionFinish.postValue(NetworkAuctionConnection.RECONNECT)
        }

        override fun onAuctionResult(data: AuctionResult) {

            val isWon: Boolean = run {
                if (currentEntryInfo.value?.entryType == "2") {
                    _isWonObjectType2.value == true
                } else {
                    false
                }
            }

            when (data.result) {
                BiddingResult.SUCCESS -> {
                    // 본인이 낙찰
                    if (loginManager.getUserNum() == data.userNum.trim() &&
                        loginManager.getTraderMngNum() == data.traderMngNum.trim()
                    ) {

                        DLogger.d("#### $isWon  >>>>>>>  ${data.price} >>>>>>>  ${data.userNum}")

                        if (isWon) {
                            AuctionState.SUCCESS_BID_WON.apply {
                                etc = intArrayOf(data.price.toIntOrDef(0))
                                _auctionStateMessage.postValue(this)
                            }
                        } else {
                            AuctionState.SUCCESS_BID.apply {
                                etc = intArrayOf(data.price.toIntOrDef(0))
                                _auctionStateMessage.postValue(this)
                            }
                        }
                    } else {

                        DLogger.d("#### 타인  $isWon  >>>>>>>  ${data.price} >>>>>>>  ${data.userNum}")

                        if (isWon) {
                            // 타인이 낙찰
                            AuctionState.OTHER_SUCCESS_BID_WON.apply {
                                etc = intArrayOf(
                                    data.price.toIntOrDef(0),
                                    data.userNum.trim().toIntOrDef(0)
                                )
                                _auctionStateMessage.postValue(this)
                            }
                        } else {
                            // 타인이 낙찰
                            AuctionState.OTHER_SUCCESS_BID.apply {
                                etc = intArrayOf(
                                    data.price.toIntOrDef(0),
                                    data.userNum.trim().toIntOrDef(0)
                                )
                                _auctionStateMessage.postValue(this)
                            }
                        }
                    }
                }
                BiddingResult.HOLD -> {
                    _auctionStateMessage.postValue(AuctionState.HOLD)
                }
                BiddingResult.CANCEL -> {
                    _snackMsg.postValue(resProvider.getString(R.string.str_auction_entry_cancel))
                }
            }
        }

        override fun onRetryTargetInfo(data: RetryTargetInfo) {
            currentEntryInfo.value?.run {
                if (entryNum == data.num && data.retryBidders.split(",")
                        .find { it.trim() == loginManager.getUserNum() } != null
                ) {
                    _auctionStateMessage.postValue(AuctionState.RETRY_BID)
                } else {
                    // 재경매 미대상일때 응찰 가격 0 초기화
                    _auctionStateMessage.postValue(AuctionState.RETRY_NOT_BID)
                    _biddingPrice.postValue(0)
                }
            }
            retryTargetInfo = data
        }

        override fun onBiddingStatus(data: AuctionBidStatus) {
            currentEntryInfo.value?.let { entryInfo ->
                if (data.entryNum == entryInfo.entryNum) {
                    // 응찰 종료된 상태
                    if (data.biddingType == BidStatusType.END) {
                        _auctionStateMessage.postValue(AuctionState.BIDDING_END)
                    } else if (data.biddingType == BidStatusType.PROGRESS) {
                        // 재경매 상태가 아닌 경우에만 응찰 시작 문구 노출
                        if (retryTargetInfo == null) {
                            _auctionStateMessage.postValue(AuctionState.PROGRESS)
                        }
                    }
                }
            }
        }

        override fun onDivisionPrice2(data: Int) {
            DLogger.e("비육우 응찰 단위 :  $data")
            _isWonObjectType2.value = data <= 1
        }
    }

    /**
     * init Start
     */
    fun start() {

        //응찰내역(webview) -> 출장우 번호  ->
        RxBus.listen(RxBusEvent.AucPrgSqEvent::class.java)
            .subscribe({
                DLogger.d("AucPrgSq ${it.aucPrgSqNo}")

                if(auctionOperation.value ==  AuctionOperation.SEQ){
                    return@subscribe
                }

                if (auctionOperation.value == AuctionOperation.BATCH_BIDDING) {
                    moveAuctionBatchSelection()
                }

                _biddingEntryNum.value = it.aucPrgSqNo

                sendBidding()

            }, {
                DLogger.d("AucPrgSqEvent Throw ${it}")
            })

        // 경매중일때 들어온경우 이전 데이터에 대한 처리
        with(nettyClient) {
            prevAuctionType()?.let {
                // 10 일괄 경매, 20 단일 경매.
                if (it.auctionType == "10") {
                    _auctionOperation.value = AuctionOperation.BATCH_SELECTION
                    _biddingEntryNum.value = 0
                } else {
                    _auctionOperation.value = AuctionOperation.SEQ
                }
            }

            prevAuctionState()?.run {
                handleAuctionState(auctionState, true)
            }
            prevResponseCode()?.let {
                // 출품 이관 전상태인경우 튕기기
                if (it.codeType == ResponseCodeType.AUCTION_BEFORE_START) {
                    startAuctionFinish.value = NetworkAuctionConnection.NONE
                }
            }

            prevEntryTitle()?.let { _currentEntryTitle.value = it }
            prevEntryInfo()?.let {
                currentEntryInfo.value = it
                fetchFavoriteEntry(it)
            }

            multiNullCheck(currentEntryTitle.value, currentEntryInfo.value) { title, data ->
                _currentEntryContents.value = parseCurrentContents(data, title)
            }

            prevRetryTargetInfo()?.let { retryInfo ->
                multiNullCheck(
                    currentEntryInfo.value,
                    prevAuctionBiddingStatus()
                ) { entryInfo, auctionBidStatus ->
                    // 응찰이 종료된 상태면 응찰 종료되었습니다. 메시지 띄움
                    if (auctionBidStatus.biddingType == BidStatusType.END) {
                        _auctionStateMessage.value = AuctionState.BIDDING_END
                        return@multiNullCheck
                    }

                    // 재경매 대상
                    if (entryInfo.entryNum == retryInfo.num &&
                        retryInfo.retryBidders.split(",")
                            .find { it.trim() == loginManager.getUserNum() } != null
                    ) {
                        if (auctionBidStatus.entryNum == entryInfo.entryNum) {
                            _auctionStateMessage.value = AuctionState.RETRY_BID
                        }
                    } else {
                        // 재경매 미대상일때
                        _auctionStateMessage.value = AuctionState.RETRY_NOT_BID
                    }
                }
            } ?: run {
                multiNullCheck(
                    prevAuctionState(),
                    prevAuctionBiddingStatus(),
                    prevEntryInfo()
                ) { auctionStatus, auctionBidStatus, entryInfo ->
                    if (auctionStatus.auctionState == AuctionState.START || auctionStatus.auctionState == AuctionState.PROGRESS) {

                        if (auctionBidStatus.entryNum == entryInfo.entryNum) {
                            if (auctionBidStatus.biddingType == BidStatusType.END) {
                                _auctionStateMessage.value = AuctionState.BIDDING_END
                            } else if (auctionBidStatus.biddingType == BidStatusType.PROGRESS) {
                                _auctionStateMessage.value = AuctionState.PROGRESS
                            }
                        }
                    }
                }
            }

            setListener(onReceiveMessage)

            // 응찰 페이지 빠져나가는 팝업창이 아닌경우 전화 권한 승인 요청
            if (prevResponseCode() == null || prevAuctionState() != null) {
                prevAuctionState()?.run {
                    if (auctionState != AuctionState.NONE && auctionState != AuctionState.FINISH) {
                        // 전화 권한 승인
                        if (deviceProvider.isPermissionsCheck(Manifest.permission.READ_PHONE_STATE)) {
                            fetchRooms()
                        } else {
                            startReadPhonePermissions.call()
                        }
                    }
                }
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
    private fun fetchRooms() {
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
                //engine?.leaveChannel()      // KIH 2024.01.23 추가

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
     */
    // KIH 24.01.23 추석처리
    /*
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
     * 카카오 방송 재생 처리 함수
     * @param pos 재생하고 싶은 포지션 값
     */
    fun startAgoraEvent(pos: Int) {
        DLogger.d("### 카카오 방송 재생 처리 함수 탐")
        RxBus.publish(RxBusEvent.LiveCastEvent(pos, CastState.JOIN, engine))
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
            AuctionState.READY,
            AuctionState.START,
            AuctionState.PROGRESS,
            AuctionState.COMPLETED -> {
                initBiddingColor()
                if (isMainThread) {
                    // 단일 경매
                    if (auctionOperation.value == AuctionOperation.SEQ) {
                        _auctionStateMessage.value = state
                        _biddingPrice.value = 0
                        isBiddingSuccess = false
                        if (state == AuctionState.READY || state == AuctionState.COMPLETED) {
                            _auctionSnackType.value = AuctionSnackType.READY
                        } else {
                            _auctionSnackType.value = AuctionSnackType.GONE
                        }
                    } else {
                        // 일괄 경매
                        // 경매 대기, 경매 종료일때만
                        if (state == AuctionState.READY) {
                            _auctionStateMessage.value = state
                            _auctionSnackType.value = AuctionSnackType.READY
                        } else if (state == AuctionState.COMPLETED) {
                            _auctionStateMessage.value = AuctionState.BATCH_COMPLETED
                            _auctionSnackType.value = AuctionSnackType.COMPLETE
                        } else if (state == AuctionState.PROGRESS) {
                            // 경매가 시작되는 순간 경매 번호 입력하세요와 출품정보, 응찰가 초기화
                            auctionState.value = state
                            _auctionOperation.value = AuctionOperation.BATCH_SELECTION
                            _auctionStateMessage.value = AuctionState.BATCH_SELECTION
                            _biddingEntryNum.value = 0
                            _biddingPrice.value = 0

                            _auctionSnackType.value = AuctionSnackType.GONE
                        }
                    }
                } else {
                    if (auctionOperation.value == AuctionOperation.SEQ) {
                        _auctionStateMessage.postValue(state)
                        _biddingPrice.postValue(0)
                        isBiddingSuccess = false
                        if (state == AuctionState.READY || state == AuctionState.COMPLETED) {
                            _auctionSnackType.postValue(AuctionSnackType.READY)
                        } else {
                            _auctionSnackType.postValue(AuctionSnackType.GONE)
                        }
                    } else {
                        // 일괄 경매
                        if (state == AuctionState.READY) {
                            _auctionStateMessage.postValue(state)
                            _auctionSnackType.postValue(AuctionSnackType.READY)
                        } else if (state == AuctionState.COMPLETED) {
                            _auctionStateMessage.postValue(AuctionState.BATCH_COMPLETED)
                            _auctionSnackType.postValue(AuctionSnackType.COMPLETE)
                        } else if (state == AuctionState.PROGRESS) {
                            // 경매가 시작되는 순간 경매 번호 입력하세요와 출품정보, 응찰가 초기화
                            auctionState.postValue(state)
                            _auctionOperation.postValue(AuctionOperation.BATCH_SELECTION)
                            _auctionStateMessage.postValue(AuctionState.BATCH_SELECTION)
                            _biddingEntryNum.postValue(0)
                            _biddingPrice.postValue(0)

                            _auctionSnackType.postValue(AuctionSnackType.GONE)
                        }
                    }
                }

            }
            else -> DLogger.e("Invalid Type $state")
        }
    }

    /**
     * 응찰 가격 색상값 초기화
     */
    private fun initBiddingColor() {
        if (biddingColor.value == R.color.color_2d9cff) {
            _biddingColor.postValue(R.color.color_1a1a1a)
        }
    }

    /**
     * 응찰 가능한 상태인지 판단 하는 함수.
     * true -> 응찰 가능, false -> 응찰 불가능
     */
    private fun isBidding(): Boolean {
        return auctionState.value == AuctionState.PROGRESS ||
                auctionState.value == AuctionState.START ||
                auctionState.value == AuctionState.COUNT_DOWN ||
                auctionState.value == AuctionState.RETRY_BID
    }

    /**
     * 응찰 취소
     */
    fun biddingCancel() {
        if (auctionState.value == AuctionState.RETRY_BID ||
            auctionState.value == AuctionState.RETRY_NOT_BID
        ) {
            _snackMsg.value = resProvider.getString(R.string.str_auction_not_entry_cancel)
            return
        }

        // 경배 번호 입력 화면에서는 경배 번호 초기화
        if (auctionOperation.value == AuctionOperation.BATCH_SELECTION) {
            _biddingEntryNum.postValue(0)
            return
        }

        if (!isBidding()) {
            _snackMsg.value = resProvider.getString(R.string.str_auction_not_entry_cancel)
            return
        }

        initBiddingColor()

        _biddingPrice.value = 0
        isBiddingSuccess = false
        currentEntryInfo.value?.let { info ->
            nettyClient.send(
                CancelBidding(
                    auctionHouseCode = loginManager.getAuctionCode(),
                    entryNum = info.entryNum,
                    traderMngNum = loginManager.getTraderMngNum(),
                    userNum = loginManager.getUserNum()
                )
            )
        }
    }

    /**
     * 응찰 금액 Send
     */
    fun sendBidding() {
        when (auctionOperation.value) {
            AuctionOperation.BATCH_SELECTION -> {
                // 경매 번호 입력 화면
                if (biddingEntryNum.value == null || biddingEntryNum.value == 0) {
                    _snackMsg.value = resProvider.getString(R.string.str_invalid_entry_num)
                    return
                }

                // 출품 정보 전송, 응찰 정보 조회
                nettyClient.send(
                    RequestEntryInfo(
                        auctionHouseCode = loginManager.getAuctionCode(),
                        traderMngNum = loginManager.getTraderMngNum(),
                        entryNum = biddingEntryNum.value.toString()
                    )
                )
            }
            else -> {
                // 순차 경매, 일괄 경매

                // 재경매 대상이 아닌 경우
                if (auctionState.value == AuctionState.RETRY_NOT_BID) {
                    _snackMsg.value =
                        resProvider.getString(R.string.str_auction_not_retry_bidder)
                    return
                }

                // 응찰 가능 상태 여부
                if (!isBidding()) {
                    _snackMsg.value = resProvider.getString(R.string.str_invalid_bidding)
                    return
                }

                multiNullCheck(currentEntryInfo.value, biddingPrice.value) { info, price ->
                    runCatching {
                        if (price == 0) {
                            _snackMsg.value =
                                resProvider.getString(R.string.str_alert_auction_bidding_input_price)
                            return@multiNullCheck
                        } else if (info.lowPrice > price) {
                            _biddingPrice.value = 0
                            _snackMsg.value =
                                resProvider.getString(R.string.str_auction_response_price_not_enough)
                            return@multiNullCheck
                        }

                        nettyClient.send(
                            Bidding(
                                auctionHouseCode = loginManager.getAuctionCode(),
                                traderMngNum = loginManager.getTraderMngNum(),
                                userNum = loginManager.getUserNum(),
                                entryNum = info.entryNum,
                                price = price,
                                newBiddingYn = if (biddingPos.value == info.entryNum) "N" else "Y"
                            )
                        )

                        // 해당 개체 신규 응찰인지 판단하기 위한 처리.
                        biddingPos.value = info.entryNum
                    }
                }
            }
        }
    }

    /**
     * 응찰 가격 한자리씩 지우기
     */
    fun onRemove(v: View) {
        runCatching {
            when (auctionOperation.value) {
                AuctionOperation.BATCH_SELECTION -> {
                    biddingEntryNum.value?.let {
                        if (it < 10) {
                            _biddingEntryNum.value = 0
                            return@runCatching
                        }

                        // Click 효과.
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        } else {
                            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        initBiddingColor()

//                        _biddingEntryNum.value = it / 10
                        _biddingEntryNum.value = 0
                        isBiddingSuccess = false
                    }
                }
                else -> {
                    DLogger.d("Remove ${biddingPrice.value}")
                    // 0 이하인경우.
                    if (biddingPrice.value < 10) {
                        _biddingPrice.value = 0
                        isBiddingSuccess = false
                        return@runCatching
                    }

                    // Click 효과.
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    } else {
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    initBiddingColor()

//                    _biddingPrice.value = biddingPrice.value / 10
                    _biddingPrice.value = 0
                    isBiddingSuccess = false
                }
            }
        }
    }

    /**
     * 응찰 가격 or 경매 번호 입력
     * @param num 0~9
     */
    fun onClickNum(num: Int) {
        runCatching {
            when (auctionOperation.value) {
                AuctionOperation.BATCH_SELECTION -> {
                    // 출품 번호 입력
                    biddingEntryNum.value?.let { entryNum ->
                        if (entryNum.toString().length > MAX_ENTRY_NUM_RANGE) {
                            startShake.call()
                            return@runCatching
                        }

                        if (entryNum == 0) {
                            if (num != 0) {
                                _biddingEntryNum.value = num
                            }
                        } else {
                            _biddingEntryNum.value = (entryNum * 10).plus(num)
                        }
                    } ?: run {
                        _biddingEntryNum.value = num
                    }
                }
                else -> {
                    // 응찰 금액 입력
                    // 금액이 파란 색인 경우 검정색으로 변경.
                    initBiddingColor()

                    DLogger.d("Before Click Num $num ${biddingPrice.value}")
                    // 응찰 성공하고 나서 응찰가 입력하는 경우
                    if (biddingPrice.value > 0 && isBiddingSuccess) {
                        _biddingPrice.value = 0
                        isBiddingSuccess = false
                    }

                    DLogger.d("After Click Num $num ${biddingPrice.value}")
                    // 최대 응찰값 제한
                    if ((biddingPrice.value * 10).plus(num).toString().length > MAX_PRICE_RANGE) {
                        // 응찰 범위값 이상인경우.
                        startShake.call()
                        return@runCatching
                    }

                    if (biddingPrice.value == 0) {
                        if (num != 0) {
                            _biddingPrice.value = num
                        }
                    } else {
                        _biddingPrice.value = (biddingPrice.value * 10).plus(num)
                    }
                }
            }
        }
    }

    /**
     * 일괄 경매 응찰 화면에서 경매 번호 입력 하는 하면으로 이동
     */
    fun moveAuctionBatchSelection() {
        if (auctionOperation.value == AuctionOperation.BATCH_BIDDING) {
            // 상태값 초기화
            _auctionOperation.value = AuctionOperation.BATCH_SELECTION
            _auctionStateMessage.value = AuctionState.BATCH_SELECTION
            _biddingPrice.value = 0
            _biddingEntryNum.value = 0
            isBiddingSuccess = false
        } else {
            DLogger.d("경매번호 입력상태가 아닙니다.")
        }
    }

    /**
     * 예정 조회 페이지 이동
     */
    fun moveFetchAuction() {
        startFetchAuction.call()
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

    /**
     * 찜가격 조회
     * @param entryInfo 출품 정보
     */
    private fun fetchFavoriteEntry(
        entryInfo: CurrentEntryInfo? = null,
        auctionStatus: AuctionStatus? = null
    ) {
        if (entryInfo != null) {
            val api = if (auctionStatus == null) {
                apiService.fetchFavoriteEntry(
                    houseCode = loginManager.getAuctionCode(),
                    entryType = entryInfo.entryType,
                    userMngNum = loginManager.getTraderMngNum(),
                    entryNum = entryInfo.entryNum
                )
            } else {
                apiService.fetchFavoriteEntry(
                    houseCode = loginManager.getAuctionCode(),
                    entryType = entryInfo.entryType,
                    userMngNum = loginManager.getTraderMngNum(),
                    entryNum = auctionStatus.num,
                )
            }
            api.observeOn(AndroidSchedulers.mainThread()).subscribe({ response ->
                if (response.success && response.info.price > 0) {
                    _biddingPrice.value = response.info.price
                    _biddingColor.value = R.color.color_2d9cff
                }
            }, {

            }).addTo(compositeDisposable)
        }
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
                onLoadingDismiss()
                startFinish.call()
            }, {

            }).addTo(compositeDisposable)
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
package com.nh.cowauction.viewmodels

//import org.webrtc.NetworkMonitor.init
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseViewModel
import com.nh.cowauction.contants.Config
import com.nh.cowauction.contants.ConnectionCode
import com.nh.cowauction.extension.SimpleDisposableSubscriber
import com.nh.cowauction.livedata.SingleLiveEvent
import com.nh.cowauction.model.receive.ResponseConnectionInfo
import com.nh.cowauction.repository.tcp.NettyClient
import com.nh.cowauction.repository.tcp.SimpleOnReceiveMessage
import com.nh.cowauction.repository.tcp.login.LoginManager
import com.nh.cowauction.utility.DLogger
import com.nh.cowauction.utility.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import javax.inject.Inject

/**
 * Description : Main ViewModel
 *
 * Created by hmju on 2021-05-28
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val nettyClient: NettyClient,
    private val resProvider: ResourceProvider,
    private val loginManager: LoginManager
) : BaseViewModel() {

    private val backButtonSubject: Subject<Long> = BehaviorSubject.createDefault(0L).toSerialized()
    val finish = SingleLiveEvent<Boolean>()
    val isSplashFinish: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    private val _webUrl: MutableLiveData<String> by lazy { MutableLiveData() }
    val webUrl: LiveData<String> get() = _webUrl
    private val _scriptFunction: MutableLiveData<String> by lazy { MutableLiveData() }
    val scriptFunction: LiveData<String> get() = _scriptFunction
    val startToastMessage: MutableLiveData<String> by lazy { MutableLiveData() }
    val startAuctionBidding: MutableLiveData<Unit> by lazy { MutableLiveData() }
    val startWatchAuction: MutableLiveData<Unit> by lazy { MutableLiveData() }
    val _watchAuctionUrl: MutableLiveData<String> by lazy { MutableLiveData() }
    val watchAuctionUrl: LiveData<String> get() = _watchAuctionUrl

    init {
        backButtonSubject.toFlowable(BackpressureStrategy.BUFFER)
            .observeOn(AndroidSchedulers.mainThread())
            .buffer(2, 1)
            .map { it[0] to it[1] }
            .subscribeWith(object : SimpleDisposableSubscriber<Pair<Long, Long>>() {
                override fun onNext(t: Pair<Long, Long>) {
                    finish.value = t.second - t.first < 2000
                }
            }).addTo(compositeDisposable)
        _webUrl.postValue(Config.BASE_DOMAIN)
    }

    fun start() {
        isSplashFinish.postValue(true)
    }

    fun setWebUrl(url: String) {
        _webUrl.postValue(url)
    }

    fun setJavaInterfaceScript(script: String) {
        _scriptFunction.postValue(script)
    }

    fun moveWatchAuction(url: String) {
        _watchAuctionUrl.value = url
        moveAuctionBidding()
    }

    /**
     * 해당 지역으로 경매 응찰
     */
    fun moveAuctionBidding() {

        if (nettyClient.isServerOn()) {
            nettyClient.closeClientSingle().subscribeOn(Schedulers.io()).subscribe({
                DLogger.d("Server Disconnected Success $it")
                onLoadingDismiss()
            }, {
                DLogger.e("moveAuctionBidding Error $it")
                onLoadingDismiss()
            })
            return
        }

        Thread {
            onLoadingShow()
            nettyClient.clearListener()
            nettyClient.setListener(onReceiveMessage)
            nettyClient.start(Config.AUCTION_IP, Config.AUCTION_PORT)
        }.start()
    }

    private val onReceiveMessage = object : SimpleOnReceiveMessage() {
        override fun onConnectionInfo(data: ResponseConnectionInfo) {

            nettyClient.clearListener()

            when (data.connectionCode) {
                ConnectionCode.SUCC.code -> {
                    onLoadingDismiss()
                    loginManager.setUserNum(data.userNum)

                    if (!loginManager.isWatchMode()) {
                        startAuctionBidding.postValue(null)
                    } else {
                        startWatchAuction.postValue(null)
                    }
                }
                ConnectionCode.DUPLICATE.code -> closeClient(ConnectionCode.DUPLICATE)
                ConnectionCode.FAIL.code -> closeClient(ConnectionCode.FAIL)
                else -> {
                    closeClient(null)
                }
            }
        }

        override fun onException(err: Throwable?) {
            onLoadingDismiss()
            DLogger.d("Main onException $err")
            nettyClient.clearListener()
            closeClient(null)
        }

        override fun onDisconnected() {
            onLoadingDismiss()
            nettyClient.clearListener()
        }
    }

    private fun closeClient(type: ConnectionCode?) {
        nettyClient.closeClientSingle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                DLogger.d("CloseClient Success $type")
                startToastMessage.value = when (type) {
                    ConnectionCode.DUPLICATE -> {
                        resProvider.getString(R.string.str_auction_invalid_duplicate)
                    }
                    ConnectionCode.FAIL -> {
                        resProvider.getString(R.string.str_auction_fail)
                    }
                    else -> {
                        resProvider.getString(R.string.str_not_open_auction)
                    }
                }
                onLoadingDismiss()
            }, {
                DLogger.d("CloseClient Error $it")
            }).addTo(compositeDisposable)
    }

    fun onBackPressed() {

        backButtonSubject.onNext(System.currentTimeMillis())
    }
}
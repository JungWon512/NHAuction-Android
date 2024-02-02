package com.nh.cowauction.repository.tcp

import com.nh.cowauction.contants.AuctionConnectionTypeCode
import com.nh.cowauction.contants.EntryTitleType
import com.nh.cowauction.contants.SocketNetwork
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.receive.*
import com.nh.cowauction.model.send.AuctionResponseSession
import com.nh.cowauction.model.send.ConnectionInfo
import com.nh.cowauction.repository.tcp.handler.ClientController
import com.nh.cowauction.repository.tcp.handler.converter.ReceiveMessageConverter
import com.nh.cowauction.repository.tcp.handler.converter.SendMessageConverter
import com.nh.cowauction.repository.tcp.handler.receive.*
import com.nh.cowauction.repository.tcp.login.LoginManager
import com.nh.cowauction.utility.DLogger
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.handler.timeout.IdleStateHandler
import io.reactivex.rxjava3.core.Single
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.SSLException

interface NettyClient {
    fun start(host: String, port: Int)
    fun isServerOn(): Boolean
    fun reConnect()
    fun setListener(listener: OnReceiveMessage)
    fun clearListener() // setListener 사용 완료하면 무조건 clearListener 한다.
    fun send(vararg objs: BaseData)
    fun closeClientSingle(): Single<Boolean>
    fun closeClient()
    fun prevEntryInfo(): CurrentEntryInfo?
    fun prevAuctionState(): AuctionStatus?
    fun prevResponseCode(): ResponseCode?
    fun prevEntryTitle(): CurrentEntryTitle?
    fun prevAuctionType(): AuctionType?
    fun prevRetryTargetInfo(): RetryTargetInfo?
    fun prevAuctionBiddingStatus(): AuctionBidStatus?
}

class NettyClientImpl @Inject constructor(
        private val loginManager: LoginManager
) : NettyClient, OnReceiveMessage {

    private var isSSLCheck = true
    private var host = ""
    private var port = 0
    private var group: EventLoopGroup? = null
    private var channel: Channel? = null
    private var listener: OnReceiveMessage? = null // 리스너 콜백은 한곳에서만 처리 하도록
    private var prevEntryInfo: CurrentEntryInfo? = null
    private var prevAuctionStatus: AuctionStatus? = null
    private var prevResponseCode: ResponseCode? = null
    private var prevEntryTitle: CurrentEntryTitle? = null
        get() {
            if (field == null) {
                field = CurrentEntryTitle(
                        one = EntryTitleType.NUM,
                        two = EntryTitleType.GENDER,
                        three = EntryTitleType.SHIPPER,
                        four = EntryTitleType.WEIGHT,
                        five = EntryTitleType.CAVING_NUM,
                        six = EntryTitleType.MOTHER,
                        seven = EntryTitleType.PASG_QCN,
                        eight = EntryTitleType.KPN,
                        nine = EntryTitleType.LOW_PRICE,
                        ten = EntryTitleType.NOTE
                )
            }
            return field
        }
    private var prevAuctionType: AuctionType? = null
    private var prevRetryTargetInfo: RetryTargetInfo? = null
    private var prevAuctionBidStatus: AuctionBidStatus? = null

    // 클라이언트에서 종료중인지 Flag true -> 클라이언트에서 종료중, false -> 클라이언트에서 종료가 아닌 상태
    private var isClosingConnection: Boolean = false

    override fun start(host: String, port: Int) {
        // 클라이언트 한번만 실행
        if (group != null) {
            DLogger.d("Group Not Null $host")
            return
        }

        runCatching {
            this.host = host
            this.port = port

            group = NioEventLoopGroup()
            Bootstrap().apply {
                group(group)
                channel(NioSocketChannel::class.java)
                option(ChannelOption.TCP_NODELAY, true)
                option(ChannelOption.SO_KEEPALIVE, true)
                option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                option(ChannelOption.SO_SNDBUF, SocketNetwork.MAX_BUF)
                option(ChannelOption.SO_SNDBUF, SocketNetwork.MAX_BUF)
                handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel?) {
                        if (ch == null) return
                        ch.pipeline().apply {
                            if (isSSLCheck) {
                                val sslHandler = initSslHandler(ch.alloc())
                                addLast("first", sslHandler)
                                // addAfter("ssl", "logger", LoggingHandler())

                                sslHandler?.handshakeFuture()?.addListener {
                                    DLogger.d("Handle Shake Success ${it.isSuccess}  ${ch.isWritable}")
                                    if (it.isSuccess) {
                                        if (ch.isWritable) {
                                            sendConnectionInfo()
                                        } else {
                                            Single.timer(500, TimeUnit.MILLISECONDS)
                                                    .doOnSuccess { sendConnectionInfo() }
                                                    .subscribe()
                                        }
                                    } else {
                                        onDisconnected()
                                    }
                                }
                            } else {
                                addLast("first", object : ChannelInboundHandlerAdapter() {
                                    override fun channelActive(ctx: ChannelHandlerContext?) {
                                        super.channelActive(ctx)
                                        sendConnectionInfo()
                                    }
                                })
                                // addLast("test", "logger", LoggingHandler())
                            }
                            addLast(IdleStateHandler(10, 0, 0))
                            addLast(ReceiveDuplexHandler(this@NettyClientImpl))
                            addLast("control", ClientController(this@NettyClientImpl))
                            addLast( "delimiter", DelimiterBasedFrameDecoder(
                                    SocketNetwork.MAX_BUF,
                                    *Delimiters.lineDelimiter()
                            ))

                           addLast( "decode", ReceiveMessageConverter())
                           addLast("session", ReceiveCheckSession(this@NettyClientImpl))
                           addLast("connection", ReceiveConnectionInfo(this@NettyClientImpl))
                           addLast("countdown", ReceiveCountDown(this@NettyClientImpl))
                           addLast("entryInfo", ReceiveCurrentEntryInfo(this@NettyClientImpl))
                           addLast("messageCode", ReceiveMessageCode(this@NettyClientImpl))
                           addLast("favoriteEntry", ReceiveFavoriteEntryInfo(this@NettyClientImpl))
                           addLast("status", ReceiveStatus(this@NettyClientImpl))
                           addLast("toast", ReceiveToastMessage(this@NettyClientImpl))
                           addLast("auctionResult", ReceiveAuctionResult(this@NettyClientImpl))
                           addLast("bidderConnect", ReceiveBidderConnectInfo(this@NettyClientImpl))
                           addLast("showEntryInfo", ReceiveShowEntryInfo(this@NettyClientImpl))
                           addLast("biddingInfo", ReceiveBiddingInfo(this@NettyClientImpl))
                           addLast("retryTargetInfo", ReceiveRetryTargetInfo(this@NettyClientImpl))
                           addLast("auctionType", ReceiveAuctionType(this@NettyClientImpl))
                           addLast("biddingStatus", ReceiveAuctionBidStatus(this@NettyClientImpl))
                            addLast(SendMessageConverter())


                        }
                    }
                })

                channel = connect(host, port).sync().channel()
            }
        }.onFailure { err ->
            DLogger.e("NettyClient Connection Exception $err")
            onException(err)
        }
    }

    /**
     * 서버가 켜져있는지 유무 상태
     */
    override fun isServerOn(): Boolean {
        group?.let { gp ->
            DLogger.d("isServer Check 서버종료 ${gp.isShutdown} 서버 종료중 ${gp.isShuttingDown}")
            return if (gp.isShuttingDown) {
                true
            } else !gp.isShutdown
        } ?: run {
            DLogger.d("그룹이 널입니다.")
            return false
        }
    }

    override fun reConnect() {
        if (host.isNotEmpty() && port > 0) {
            start(host, port)
        }
    }

    override fun setListener(listener: OnReceiveMessage) {
        this.listener = listener
    }

    override fun closeClient() {
        group?.run {
            shutdownGracefully().addListener {
                DLogger.d("closeClient Error $isClosingConnection")
                channel?.closeFuture()
                prevEntryInfo = null
                prevAuctionStatus = null
                prevResponseCode = null
                prevEntryTitle = null
                prevAuctionType = null
                prevRetryTargetInfo = null
                group = null
                channel = null
                onDisconnected()
            }
        }
    }

    override fun closeClientSingle(): Single<Boolean> {
        return Single.create { emitter ->
            isClosingConnection = true
            try {
                group?.run { shutdownGracefully().sync() }
                channel?.run { closeFuture().sync() }
                prevEntryInfo = null
                prevAuctionStatus = null
                prevResponseCode = null
                prevEntryTitle = null
                prevAuctionType = null
                prevRetryTargetInfo = null
                group = null
                channel = null
                isClosingConnection = false
                emitter.onSuccess(true)
            } catch (ex: InterruptedException) {
                isClosingConnection = false
                emitter.onError(ex)
            }
        }
    }

    /**
     * 데이터 전송
     * 데이터 모델 타입
     */
    override fun send(vararg objs: BaseData) {
        channel?.runCatching {
            objs.forEach { write(it) }
            flush()
        }?.onFailure {
            DLogger.e("Send Error $it")
        }
    }

    override fun clearListener() {
        listener = null
    }

    override fun prevEntryInfo() = prevEntryInfo
    override fun prevAuctionState() = prevAuctionStatus
    override fun prevResponseCode() = prevResponseCode
    override fun prevEntryTitle() = prevEntryTitle
    override fun prevAuctionType() = prevAuctionType
    override fun prevRetryTargetInfo() = prevRetryTargetInfo
    override fun prevAuctionBiddingStatus() = prevAuctionBidStatus

    private fun initSslHandler(alloc: ByteBufAllocator): SslHandler? {
        return try {
            val sslContextBuilder = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build()
            return sslContextBuilder.newHandler(alloc)
        } catch (ex: SSLException) {
            DLogger.e("initSslHandler Error ${ex.message}")
            null
        } catch (ex : Error) {
            DLogger.e("initSslHandler Error ${ex.message}")
            null
        }
    }

    private fun sendConnectionInfo() {

        if(!loginManager.isWatchMode()){
            send(
                ConnectionInfo(
                    auctionHouseCode = loginManager.getAuctionCode(),
                    traderMngNum = loginManager.getTraderMngNum(),
                    token = loginManager.getUserToken(),
                    channel = AuctionConnectionTypeCode.BIDDING.code
                )
            )
        }else{
            send(
                ConnectionInfo(
                    auctionHouseCode = loginManager.getAuctionCode(),
                    traderMngNum = "",
                    token = loginManager.getUserWatchToken(),
                    channel = AuctionConnectionTypeCode.WATCH.code,
                )
            )
        }

        onConnected()
    }
    // [s] OnReceiveMessage

    override fun onConnected() {
        listener?.onConnected()
    }

    override fun onDisconnected() {
        listener?.onDisconnected()
    }

    override fun onCheckSession(data: AuctionCheckSession) {
        // Session Check 받은 데이터 그대로 보내기.
        if(!loginManager.isWatchMode()){
            send(AuctionResponseSession(userNum = loginManager.getUserNum(),channel= AuctionConnectionTypeCode.BIDDING.code))
        }else{
            send(AuctionResponseSession(userNum = "",channel= AuctionConnectionTypeCode.WATCH.code))
        }
    }

    override fun onCountDown(data: AuctionCountDown) {
        listener?.onCountDown(data)
    }

    override fun onStatus(data: AuctionStatus) {
        prevAuctionStatus = data
        listener?.onStatus(data)
    }

    override fun onCurrentEntryInfo(data: CurrentEntryInfo) {
        prevEntryInfo = data
        listener?.onCurrentEntryInfo(data)
    }

    override fun onResponseCode(data: ResponseCode) {
        prevResponseCode = data
        listener?.onResponseCode(data)
    }

    override fun onFavoriteEntryInfo(data: FavoriteEntryInfo) {
        listener?.onFavoriteEntryInfo(data)
    }

    override fun onConnectionInfo(data: ResponseConnectionInfo) {
        listener?.onConnectionInfo(data)
    }

    override fun onMessage(data: ToastMessage) {
        listener?.onMessage(data)
    }

    override fun onAuctionResult(data: AuctionResult) {
        listener?.onAuctionResult(data)
    }

    override fun onException(err: Throwable?) {
        listener?.onException(err)
    }

    override fun onBidderConnectInfo(data: BidderConnectInfo) {
        listener?.onBidderConnectInfo(data)
    }

    override fun onBiddingInfo(data: ResponseBiddingInfo) {
        listener?.onBiddingInfo(data)
    }

    override fun onHandleDisconnected() {
        DLogger.d("onHandleDisconnected $isClosingConnection")
        if (!isClosingConnection) {
            closeClient()
        }
    }

    override fun onCurrentEntryTitle(data: CurrentEntryTitle) {
        prevEntryTitle = data
        listener?.onCurrentEntryTitle(data)
    }

    override fun onRetryTargetInfo(data: RetryTargetInfo) {
        prevRetryTargetInfo = data
        listener?.onRetryTargetInfo(data)
    }

    override fun onAuctionType(data: AuctionType) {
        prevAuctionType = data
        listener?.onAuctionType(data)
    }

    override fun onBiddingStatus(data: AuctionBidStatus) {
        prevAuctionBidStatus = data
        listener?.onBiddingStatus(data)
    }

    override fun onDivisionPrice2(data: Int) {
        listener?.onDivisionPrice2(data)
    }
    // [s] OnReceiveMessage
}

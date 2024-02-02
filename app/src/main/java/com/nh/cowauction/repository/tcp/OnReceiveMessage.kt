package com.nh.cowauction.repository.tcp

import com.nh.cowauction.model.receive.*

/**
 * Description : Netty Socket 메시지 처리
 *
 * Created by hmju on 2021-05-28
 */
interface OnReceiveMessage {
    fun onConnected()
    fun onDisconnected()
    fun onCheckSession(data: AuctionCheckSession)
    fun onCountDown(data: AuctionCountDown)
    fun onStatus(data: AuctionStatus)
    fun onCurrentEntryInfo(data: CurrentEntryInfo)
    fun onResponseCode(data: ResponseCode)
    fun onFavoriteEntryInfo(data: FavoriteEntryInfo)
    fun onConnectionInfo(data: ResponseConnectionInfo)
    fun onMessage(data: ToastMessage)
    fun onAuctionResult(data: AuctionResult)
    fun onException(err: Throwable?)
    fun onBidderConnectInfo(data: BidderConnectInfo)
    fun onBiddingInfo(data: ResponseBiddingInfo)
    fun onHandleDisconnected() // 접속이 끊어진경우 클라이언트에서 접속은 끊었는지, 서버에서 끊은건지 처리하기 위한 함수
    fun onCurrentEntryTitle(data: CurrentEntryTitle)
    fun onRetryTargetInfo(data: RetryTargetInfo)
    fun onAuctionType(data: AuctionType)
    fun onBiddingStatus(data: AuctionBidStatus)
    fun onDivisionPrice2(data:Int)
}

/**
 * Simple ReceiveMessage Class
 */
open class SimpleOnReceiveMessage : OnReceiveMessage {
    override fun onDisconnected() {}

    override fun onConnected() {}

    override fun onCheckSession(data: AuctionCheckSession) {}

    override fun onCountDown(data: AuctionCountDown) {}

    override fun onStatus(data: AuctionStatus) {}

    override fun onCurrentEntryInfo(data: CurrentEntryInfo) {}

    override fun onResponseCode(data: ResponseCode) {}

    override fun onFavoriteEntryInfo(data: FavoriteEntryInfo) {}

    override fun onConnectionInfo(data: ResponseConnectionInfo) {}

    override fun onMessage(data: ToastMessage) {}

    override fun onAuctionResult(data: AuctionResult) {}

    override fun onException(err: Throwable?) {}

    override fun onBidderConnectInfo(data: BidderConnectInfo) {}

    override fun onBiddingInfo(data: ResponseBiddingInfo) {}

    override fun onHandleDisconnected() {}

    override fun onCurrentEntryTitle(data: CurrentEntryTitle) {}

    override fun onRetryTargetInfo(data: RetryTargetInfo) {}

    override fun onAuctionType(data: AuctionType) {}

    override fun onBiddingStatus(data: AuctionBidStatus) {}

    override fun onDivisionPrice2(data: Int) {}
}
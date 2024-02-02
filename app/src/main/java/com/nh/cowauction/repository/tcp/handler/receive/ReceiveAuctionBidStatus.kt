package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.AuctionBidStatus
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler


class ReceiveAuctionBidStatus(private val callback : OnReceiveMessage):SimpleChannelInboundHandler<AuctionBidStatus>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: AuctionBidStatus?) {
        if (msg == null) return
        callback.onBiddingStatus(msg)
    }
}
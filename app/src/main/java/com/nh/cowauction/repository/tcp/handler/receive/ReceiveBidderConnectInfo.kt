package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.BidderConnectInfo
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveBidderConnectInfo(private val callback: OnReceiveMessage) :
    SimpleChannelInboundHandler<BidderConnectInfo>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: BidderConnectInfo?) {
        if (msg == null) return
        callback.onBidderConnectInfo(msg)
    }
}
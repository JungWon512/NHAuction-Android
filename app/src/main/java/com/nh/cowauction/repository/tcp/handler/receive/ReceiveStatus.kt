package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.AuctionStatus
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveStatus(private val callback: OnReceiveMessage) :
    SimpleChannelInboundHandler<AuctionStatus>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: AuctionStatus?) {
        if (msg == null) return
        callback.onStatus(msg)
    }
}
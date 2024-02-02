package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.AuctionCountDown
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveCountDown(private val callback: OnReceiveMessage) :
    SimpleChannelInboundHandler<AuctionCountDown>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: AuctionCountDown?) {
        if (msg == null) return
        callback.onCountDown(msg)
    }
}
package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.AuctionCheckSession
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveCheckSession(private val callback: OnReceiveMessage) :
    SimpleChannelInboundHandler<AuctionCheckSession>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: AuctionCheckSession?) {
        if (msg == null) return
        callback.onCheckSession(msg)
    }
}
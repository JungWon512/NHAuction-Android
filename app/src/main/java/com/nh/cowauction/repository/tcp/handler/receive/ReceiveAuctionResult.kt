package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.AuctionResult
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveAuctionResult(private val callback: OnReceiveMessage) :
    SimpleChannelInboundHandler<AuctionResult>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: AuctionResult?) {
        if (msg == null) return
        callback.onAuctionResult(msg)
    }
}
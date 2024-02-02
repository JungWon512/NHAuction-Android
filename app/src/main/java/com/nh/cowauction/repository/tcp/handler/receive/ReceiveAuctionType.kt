package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.AuctionType
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler


class ReceiveAuctionType(private val callback: OnReceiveMessage) :
        SimpleChannelInboundHandler<AuctionType>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: AuctionType?) {
        if (msg == null) return
        callback.onAuctionType(msg)
    }
}
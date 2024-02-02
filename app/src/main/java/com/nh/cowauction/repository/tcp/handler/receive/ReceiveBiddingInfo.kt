package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.ResponseBiddingInfo
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler


class ReceiveBiddingInfo(private val callback: OnReceiveMessage) :
        SimpleChannelInboundHandler<ResponseBiddingInfo>() {

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ResponseBiddingInfo?) {
        if (msg == null) return
        callback.onBiddingInfo(msg)
    }
}
package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.ResponseConnectionInfo
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveConnectionInfo(private val callback: OnReceiveMessage) :
    SimpleChannelInboundHandler<ResponseConnectionInfo>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ResponseConnectionInfo?) {
        if (msg == null) return
        callback.onConnectionInfo(msg)
    }
}
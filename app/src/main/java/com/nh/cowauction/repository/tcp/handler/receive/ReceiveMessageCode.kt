package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.ResponseCode
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveMessageCode(private val callback: OnReceiveMessage) :
    SimpleChannelInboundHandler<ResponseCode>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ResponseCode?) {
        if (msg == null) return
        callback.onResponseCode(msg)
    }
}
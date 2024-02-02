package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.ToastMessage
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveToastMessage(private val callback: OnReceiveMessage) :
    SimpleChannelInboundHandler<ToastMessage>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ToastMessage?) {
        if (msg == null) return
        callback.onMessage(msg)
    }
}
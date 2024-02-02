package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.RetryTargetInfo
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveRetryTargetInfo(private val callback: OnReceiveMessage) : SimpleChannelInboundHandler<RetryTargetInfo>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: RetryTargetInfo?) {
        if (msg == null) return

        callback.onRetryTargetInfo(msg)
    }
}
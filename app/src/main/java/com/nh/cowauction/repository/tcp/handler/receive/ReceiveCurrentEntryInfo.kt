package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.CurrentEntryInfo
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler


class ReceiveCurrentEntryInfo(private val callback: OnReceiveMessage) :
    SimpleChannelInboundHandler<CurrentEntryInfo>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: CurrentEntryInfo?) {
        if (msg == null) return
        callback.onCurrentEntryInfo(msg)
    }
}
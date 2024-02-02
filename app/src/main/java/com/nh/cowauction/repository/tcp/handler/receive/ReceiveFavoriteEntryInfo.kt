package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.FavoriteEntryInfo
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveFavoriteEntryInfo(private val callback: OnReceiveMessage) :
    SimpleChannelInboundHandler<FavoriteEntryInfo>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FavoriteEntryInfo?) {
        if (msg == null) return
        callback.onFavoriteEntryInfo(msg)
    }
}
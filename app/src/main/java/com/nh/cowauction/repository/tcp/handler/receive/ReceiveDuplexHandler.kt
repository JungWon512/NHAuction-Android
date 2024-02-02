package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.timeout.IdleState

import io.netty.handler.timeout.IdleStateEvent

import io.netty.channel.ChannelDuplexHandler
import java.lang.Exception


class ReceiveDuplexHandler(
    private val callback: OnReceiveMessage
) : ChannelDuplexHandler() {
    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            if (evt.state() == IdleState.READER_IDLE) {
                callback.onDisconnected()
            } else if (evt.state() == IdleState.WRITER_IDLE) {
                //empty write
            }
        }
    }
}
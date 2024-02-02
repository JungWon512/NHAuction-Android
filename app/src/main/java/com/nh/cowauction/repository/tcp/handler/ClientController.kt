package com.nh.cowauction.repository.tcp.handler

import com.nh.cowauction.repository.tcp.OnReceiveMessage
import com.nh.cowauction.utility.DLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

/**
 * Description : 서버의 응답 및 여러 제어 클래스
 *
 * Created by juhongmin on 6/10/21
 */
class ClientController(
        private val callback: OnReceiveMessage
) : ChannelInboundHandlerAdapter() {

    override fun channelActive(ctx: ChannelHandlerContext?) {
        super.channelActive(ctx)
        if (ctx == null) return
        DLogger.e("서버와 연결 완료 ${ctx.channel()} ${ctx.channel().isWritable}")
        // Log.e("JLogger","서버와 연결 완료 ${ctx.channel()} ${ctx.channel().isWritable}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        DLogger.d("ClientController 서버와의 연결이 종료 되었습니다")
        // Log.d("JLogger","ClientController 서버와의 연결이 종료 되었습니다")
        callback.onHandleDisconnected()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        DLogger.e("ClientController Error $cause")
        // Log.e("JLogger","ClientController Error $cause")
        callback.onException(cause)
    }
}
package com.nh.cowauction.repository.tcp.handler.converter

import com.nh.cowauction.extension.classToStr
import com.nh.cowauction.model.base.BaseData
import com.nh.cowauction.model.send.AuctionResponseSession
import com.nh.cowauction.utility.DLogger
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import java.nio.CharBuffer

/**
 * Description : 서버로 보내기 직전 BaseData 를 변환해주는 컴퍼터 클래스
 *
 * Created by hmju on 2021-06-11
 */
class SendMessageConverter : MessageToMessageEncoder<BaseData>() {

    private val charset = Charsets.UTF_8

    override fun encode(ctx: ChannelHandlerContext?, msg: BaseData?, out: MutableList<Any>?) {
        if (msg == null || out == null || ctx == null) return

        if (msg !is AuctionResponseSession) {
            DLogger.d("SendMessage ${msg.classToStr()}")
        }

        out.add(ByteBufUtil.encodeString(
                        ctx.alloc(),
                        CharBuffer.wrap(msg.classToStr().plus("\r\n")),
                        charset))
    }
}
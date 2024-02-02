package com.nh.cowauction.repository.tcp.handler.receive

import com.nh.cowauction.model.receive.CurrentEntryTitle
import com.nh.cowauction.model.receive.ShowEntryInfo
import com.nh.cowauction.model.receive.toEntryTitleType
import com.nh.cowauction.repository.tcp.OnReceiveMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ReceiveShowEntryInfo(private val callback: OnReceiveMessage) : SimpleChannelInboundHandler<ShowEntryInfo>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ShowEntryInfo?) {
        if (msg == null) return

        callback.onCurrentEntryTitle(CurrentEntryTitle(
                one = msg.one.toEntryTitleType(),
                two = msg.two.toEntryTitleType(),
                three = msg.three.toEntryTitleType(),
                four = msg.four.toEntryTitleType(),
                five = msg.five.toEntryTitleType(),
                six = msg.six.toEntryTitleType(),
                seven = msg.seven.toEntryTitleType(),
                eight = msg.eight.toEntryTitleType(),
                nine = msg.nine.toEntryTitleType(),
                ten = msg.ten.toEntryTitleType()
        ))

        //비육우 응찰 단위
        callback.onDivisionPrice2(msg.divisionPrice2)
    }
}
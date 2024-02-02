package com.nh.cowauction.repository.tcp.handler.converter

import com.nh.cowauction.contants.Type
import com.nh.cowauction.extension.multiNullCheck
import com.nh.cowauction.extension.strToClass
import com.nh.cowauction.model.receive.*
import com.nh.cowauction.utility.DLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import java.nio.charset.Charset

/**
 * Description : ByteBuf -> BaseData Converter Factory
 *
 * Created by juhongmin on 6/10/21
 */
class ReceiveMessageConverter : MessageToMessageDecoder<ByteBuf>() {
    override fun decode(ctx: ChannelHandlerContext?, _msg: ByteBuf?, _out: MutableList<Any>?) {
        multiNullCheck(_msg, _out) { msg, out ->
            runCatching {
                val str = msg.toString(Charset.defaultCharset())
                if (!str.startsWith(Type.RECV_AUCTION_SESSION.key)) {
                    DLogger.d("=============[S Receive Message]================")
                    DLogger.d("Recv $str")
                    DLogger.d("=============[E Receive Message]================")
                }

                if (str.length > 1) {
                    when ("${str[0]}${str[1]}") {
                        Type.RECV_AUCTION_SESSION.key -> out.add(str.strToClass<AuctionCheckSession>())
                        Type.AUCTION_COUNT_DOWN.key -> out.add(str.strToClass<AuctionCountDown>())
                        Type.AUCTION_STATUS.key -> out.add(str.strToClass<AuctionStatus>())
                        Type.ENTRY_INFO.key -> out.add(str.strToClass<CurrentEntryInfo>())
                        Type.RES_CODE.key -> out.add(str.strToClass<ResponseCode>())
                        Type.FAVORITE_ENTRY_INFO.key -> out.add(str.strToClass<FavoriteEntryInfo>())
                        Type.RECV_CONNECTION_INFO.key -> out.add(str.strToClass<ResponseConnectionInfo>())
                        Type.RECV_MESSAGE.key -> out.add(str.strToClass<ToastMessage>())
                        Type.AUCTION_RESULT.key -> out.add(str.strToClass<AuctionResult>())
                        Type.BIDDER_CONNECT_INFO.key -> out.add(str.strToClass<BidderConnectInfo>())
                        Type.SHOW_ENTRY_INFO.key -> out.add(str.strToClass<ShowEntryInfo>())
                        Type.RECV_BIDDING_INFO.key -> out.add(str.strToClass<ResponseBiddingInfo>())
                        Type.RETRY_BIDDING_INFO.key -> out.add(str.strToClass<RetryTargetInfo>())
                        Type.AUCTION_TYPE.key -> out.add(str.strToClass<AuctionType>())
                        Type.BID_STATUS.key -> out.add(str.strToClass<AuctionBidStatus>())
                        else -> {
                            // Error
                            DLogger.e("Invalid Type $str")
                        }
                    }
                } else {
                    DLogger.e("Invalid Type $str")
                }
            }.onFailure {
                DLogger.e("Decode Converter ${it.message}")
            }
        }
    }
}
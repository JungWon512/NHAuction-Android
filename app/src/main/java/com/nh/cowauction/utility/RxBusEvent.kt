package com.nh.cowauction.utility

import com.nh.cowauction.contants.CastState
import com.nh.cowauction.contants.MainEntryType
import io.agora.rtc2.RtcEngine
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * Description : RxBusEvent
 *
 * Created by hmju on 2021-09-27
 */

object RxBus {
    private val publisher = PublishSubject.create<Any>()
    fun publish(event: Any) {
        publisher.onNext(event)
    }

    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}

class RxBusEvent {

    /**
     * 메인 진입 제어 이벤트
     */
    data class MainEnterEvent(
            val type: MainEntryType,
            val isFlag: Boolean
    )

    /**
     * 방송 뷰어 제어 이벤트
     */
    data class LiveCastEvent(
            val position: Int,
            val state: CastState? = null,
            val engine: RtcEngine? = null
    )

    /**
     * Agora Callback
     */
    data class AgoraCallbackEvent(
        val state: CastState,
        val engine: RtcEngine? = null,
        val uid: Int,
    )


    /**
     * 방송 뷰어 사운드 제어 이벤트
     */
    data class LiveSoundEvent(
            val isSoundOn: Boolean,
            val isBackGround: Boolean = false,
            val isForeGround:Boolean = false,
    )

    /**
     * 일괄 경매 -> 응찰내역 (webview) -> 출장우 번호  -> 출장우 검색 제어 이벤트
     */
    data class AucPrgSqEvent(
        val aucPrgSqNo: Int
    )
}
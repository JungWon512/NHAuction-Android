package com.nh.cowauction

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nh.cowauction.model.receive.ResponseConnectionInfo
import com.nh.cowauction.repository.tcp.NettyClient
import com.nh.cowauction.repository.tcp.login.LoginManager
import com.nh.cowauction.ui.auction.AuctionActivity
import com.nh.cowauction.utility.DLogger
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.rxjava3.core.Flowable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description : 재접속 관련 팝업
 *
 * Created by hmju on 2021-10-08
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TestReConnectionAuction {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var nettyClient : NettyClient


    @Inject
    lateinit var loginManager : LoginManager

    @Before
    fun init(){
        hiltRule.inject()
        loginManager.setAuctionCode("8808990661315")
        loginManager.setAuctionName("화순 축협")
    }

    @Test
    fun reconnectionStart(){
        launchActivity<AuctionActivity>().apply {
            moveToState(Lifecycle.State.RESUMED)
        }

        Flowable.interval(1000,1000,TimeUnit.MILLISECONDS)
            .doOnNext {
                DLogger.d("dddd $it")
//                if (it % 2 == 0L) {
//                    nettyClient.testConnection(ResponseConnectionInfo(
//                        connectionCode = 2001
//                    ))
//                } else {
//                    nettyClient.testConnection(ResponseConnectionInfo(
//                        connectionCode = 2003
//                    ))
//                }

            }.subscribe()

        Thread.sleep(100_000)
    }

    @Test
    fun testKakao(){
//        kakaoLiveProvider.fetchRooms().subscribe({
//            DLogger.d("RES $it")
//        },{
//            DLogger.e("ERROR $it")
//        })
    }
}
package com.nh.cowauction

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nh.cowauction.contants.Config
import com.nh.cowauction.model.receive.ResponseCode
import com.nh.cowauction.repository.tcp.NettyClient
import com.nh.cowauction.repository.tcp.SimpleOnReceiveMessage
import com.nh.cowauction.repository.tcp.login.LoginManager
import com.nh.cowauction.ui.auction.AuctionActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Description : 전화 걸기 페이지 진입
 *
 * Created by hmju on 2021-10-07
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TestPhoneState {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var nettyClient : NettyClient

    @Inject
    lateinit var loginManager : LoginManager

    @Before
    fun init(){
        hiltRule.inject()
        loginManager.setAuctionCode("8808990656656")
        loginManager.setTraderMngNum("14")
        loginManager.setNearAuctionCode("8808990656651")
        loginManager.setUserToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWN0aW9uSG91c2VDb2RlIjoiODgwODk5MDY1NjY1NiIsInVzZXJSb2xlIjoiQklEREVSIiwidXNlck1lbU51bSI6IjE0IiwiZXhwIjoxNjMzNjE4Nzk5fQ.djmNSTEGNavwEu_oocuT8qd7F7VudiAiEvCO7LwbeSo8oOvhgM-RTUMfPwL4M0E4KgmnbEi8JR6GUi3ZaY1e2A")
        nettyClient.start("192.168.0.25", Config.AUCTION_PORT)

    }

    @Test
    fun phoneStateStart(){
        launchActivity<AuctionActivity>().apply {
            moveToState(Lifecycle.State.RESUMED)
        }

        Thread.sleep(100_000)
    }
}
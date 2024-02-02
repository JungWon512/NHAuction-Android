package com.nh.cowauction

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nh.cowauction.contants.Config
import com.nh.cowauction.repository.http.ApiService
import com.nh.cowauction.repository.tcp.NettyClient
import com.nh.cowauction.repository.tcp.login.LoginManager
import com.nh.cowauction.ui.auction.AuctionActivity
import com.nh.cowauction.utility.DLogger
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Description : 경매 페이지 Unit Test
 *
 * Created by hmju on 2021-08-17
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TestAuctionAct {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var nettyClient : NettyClient

    @Inject
    lateinit var apiService : ApiService

    @Inject
    lateinit var loginManager : LoginManager

    @Before
    fun init(){
        hiltRule.inject()
        loginManager.setAuctionCode("8808990656656")
        loginManager.setTraderMngNum("29")
        loginManager.setNearAuctionCode("8808990656651")
        loginManager.setUserToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWN0aW9uSG91c2VDb2RlIjoiODgwODk5MDY1NjY1NiIsInVzZXJSb2xlIjoiQklEREVSIiwidXNlck1lbU51bSI6IjI5IiwiZXhwIjoxNjMzMDEzOTk5fQ.McdfAoYHawwG0eo7DqGyJw8aWt0UTYU_NswgTrWlsXb8nLrduPw6H9QrnHc15dAKYmtZFR35KCdInq7yzGzOMg")
        nettyClient.start("192.168.0.18",Config.AUCTION_PORT)
    }

    @Test
    fun test_auction_countdown(){
        launchActivity<AuctionActivity>().apply {
            moveToState(Lifecycle.State.RESUMED)
        }

        Thread.sleep(100_000)
        DLogger.d("EXIT")
    }
}
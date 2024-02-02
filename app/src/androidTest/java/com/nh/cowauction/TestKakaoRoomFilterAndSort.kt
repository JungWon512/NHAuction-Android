package com.nh.cowauction

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nh.cowauction.repository.tcp.login.LoginManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Description :
 *
 * Created by hmju on 2021-11-01
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TestKakaoRoomFilterAndSort {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)


    @Inject
    lateinit var loginManager: LoginManager

    @Before
    fun init() {
        hiltRule.inject()
        loginManager.setAuctionCode("8808990656656")
    }

    @Test
    fun roomFilterAndSort() {
//        kakaoProvider.fetchRooms()
//            .map { kakaoProvider.filterRooms(it) }
//            .map { kakaoProvider.sortedRooms(it, kakaoProvider.maxRoomCnt()) }
//            .flatMap { kakaoProvider.findRoom(3) }
//            .subscribe({
//                DLogger.d("RoomChannelId $it")
//            }, {
//                DLogger.d("Error $it")
//            })
    }
}
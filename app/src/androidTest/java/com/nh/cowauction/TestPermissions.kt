package com.nh.cowauction

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nh.cowauction.ui.permissions.PermissionsActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Description :
 *
 * Created by hmju on 2021-10-07
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TestPermissions {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init(){
        hiltRule.inject()
    }

    @Test
    fun permissionsStart(){
        launchActivity<PermissionsActivity>().apply {
            moveToState(Lifecycle.State.RESUMED)
        }

        Thread.sleep(100_000)
    }
}
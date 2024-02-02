package com.nh.cowauction

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import androidx.multidex.MultiDexApplication
import com.nh.cowauction.di.*
import com.nh.cowauction.extension.exitApp
import com.nh.cowauction.ui.auction.AuctionActivity
import com.nh.cowauction.ui.auction.WatchAuctionActivity
import com.nh.cowauction.ui.main.MainActivity
import com.nh.cowauction.ui.splash.SplashActivity
import com.nh.cowauction.utility.DLogger
import com.nh.cowauction.utility.RxBus
import com.nh.cowauction.utility.RxBusEvent
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.SocketException

/**
 * Description : MyApplication Class
 *
 * Created by hmju on 2021-05-28
 */
@HiltAndroidApp
class MainApplication : MultiDexApplication() {

    enum class AppStatus {
        BACKGROUND, // 앱 Background 상태
        RETURNED_TO_FOREGROUND, // Background -> ForeGournd 올라온경우
        FOREGROUND
    }

    companion object {
        var gAppStatus = AppStatus.BACKGROUND
    }

    var introActivity: WeakReference<SplashActivity>? = null
    var mainActivity: WeakReference<MainActivity>? = null

    override fun onCreate() {
        super.onCreate()
        initRxJava()
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        activityLifecycleCallbacks.currentActivity?.get()?.run {

            if(this !is WatchAuctionActivity){
                runCatching {
                    exitApp()
                }
            }
        }
    }

    /**
     * reactivex.exceptions.UndeliverableException 처리 함수.
     */
    private fun initRxJava() {
        // reactivex.exceptions.UndeliverableException
        // 참고 링크 https://thdev.tech/android/2019/03/04/RxJava2-Error-handling/
        RxJavaPlugins.setErrorHandler { e ->
            var error = e
            DLogger.d("RxError $error")
            if (error is UndeliverableException) {
                error = e.cause
            }
            if (error is IOException || error is SocketException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (error is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            if (error is NullPointerException || error is IllegalArgumentException) {
                // that's likely a bug in the application
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                        Thread.currentThread(),
                        error
                )
                return@setErrorHandler
            }
            if (error is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                        Thread.currentThread(),
                        error
                )
                return@setErrorHandler
            }
        }
    }

    /**
     * Activity Memory 참조 해제.
     */
    fun unregisterActivity(activity: Activity?) {
        if (activity is MainActivity) {
            mainActivity = null
        } else if (activity is SplashActivity) {
            introActivity = null
        }
    }

    /**
     * Activity 참조.
     */
    fun registerActivity(activity: Activity) {
        if (activity is MainActivity) {
            mainActivity = WeakReference(activity)
        } else if (activity is SplashActivity) {
            introActivity = WeakReference(activity)
        }
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        var running = 0
        var currentActivity: WeakReference<Activity>? = null

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity is MainActivity || activity is SplashActivity) {
                registerActivity(activity)
            }
            currentActivity?.clear()
            currentActivity = WeakReference(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            if (++running == 1) {
                gAppStatus = AppStatus.RETURNED_TO_FOREGROUND
            } else if (running > 1) {
                gAppStatus = AppStatus.FOREGROUND
            }
        }

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            if (--running == 0) {
                gAppStatus = AppStatus.BACKGROUND
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity is SplashActivity) {
                unregisterActivity(activity)
            }
        }
    }
}
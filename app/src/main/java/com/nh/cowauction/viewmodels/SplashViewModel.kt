package com.nh.cowauction.viewmodels

import com.nh.cowauction.base.BaseViewModel
import com.nh.cowauction.contants.Config
import com.nh.cowauction.extension.handleNetworkErrorRetry
import com.nh.cowauction.extension.isUpdate
import com.nh.cowauction.extension.toIntOrDef
import com.nh.cowauction.livedata.SingleLiveEvent
import com.nh.cowauction.repository.http.ApiService
import com.nh.cowauction.utility.DLogger
import com.nh.cowauction.utility.DeviceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Description : Splash ViewModel Class
 *
 * Created by hmju on 2021-06-01
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val apiService: ApiService,
    private val deviceProvider: DeviceProvider
) : BaseViewModel() {

    val startMain = SingleLiveEvent<Unit>()
    val startOptionalUpdateDialog = SingleLiveEvent<Unit>() // 선택 업데이트
    val startRequireUpdateDialog = SingleLiveEvent<Unit>() // 강제 업데이트

    fun startSplash() {
        DLogger.d("startSplash")
        apiService.fetchVersions()
            .delay(500, TimeUnit.MILLISECONDS)
            .compose(handleNetworkErrorRetry())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ res ->
                val currVersion = deviceProvider.getVersionName()
                DLogger.d("VersionCheck $res $currVersion")
                if (currVersion.isUpdate(res.info.minVersion)) {
                    // 최소 버전보다 낮은 경우
                    startRequireUpdateDialog.call()
                } else {
                    if (currVersion.isUpdate(res.info.maxVersion)) {
                        startOptionalUpdateDialog.call()
                    } else {
                        startMain.call()
                    }
                }

                // TCP IP, Port 처리
                if (res.info.tcpHost.isNotEmpty() && res.info.tcpPort.isNotEmpty()) {
                    Config.AUCTION_IP = res.info.tcpHost
                    Config.AUCTION_PORT = res.info.tcpPort.toIntOrDef(5001)
                }
            }, { err ->
                DLogger.d("err")
                startMain.call()
            }).addTo(compositeDisposable)
    }
}
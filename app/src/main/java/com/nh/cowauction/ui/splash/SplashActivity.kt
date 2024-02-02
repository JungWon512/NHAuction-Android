package com.nh.cowauction.ui.splash

import android.os.Bundle
import androidx.activity.viewModels
import com.nh.cowauction.BR
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseActivity
import com.nh.cowauction.contants.Config
import com.nh.cowauction.contants.MainEntryType
import com.nh.cowauction.databinding.ActivitySplashBinding
import com.nh.cowauction.extension.exitApp
import com.nh.cowauction.extension.isRootingDevice
import com.nh.cowauction.extension.movePlayStore
import com.nh.cowauction.ui.dialog.CommonDialog
import com.nh.cowauction.ui.dialog.SelectIpDialog
import com.nh.cowauction.utility.RxBus
import com.nh.cowauction.utility.RxBusEvent
import com.nh.cowauction.viewmodels.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
    override val layoutId = R.layout.activity_splash
    override val viewModel: SplashViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFitsWindows()
        with(viewModel) {

            startMain.observe(this@SplashActivity, {
                RxBus.publish(RxBusEvent.MainEnterEvent(MainEntryType.VERSION, true))
            })

            startOptionalUpdateDialog.observe(this@SplashActivity, {
                CommonDialog(this@SplashActivity)
                        .setContents(R.string.str_app_version_update_optional)
                        .setPositiveButton(R.string.str_app_update)
                        .setNegativeButton(R.string.str_app_update_after)
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == CommonDialog.POSITIVE) {
                                    // 앱 업데이트 링크
                                    this@SplashActivity.movePlayStore()
                                } else {
                                    startMain.call()
                                }
                            }
                        })
                        .show()
            })

            startRequireUpdateDialog.observe(this@SplashActivity, {
                CommonDialog(this@SplashActivity)
                        .setContents(R.string.str_app_version_update_optional)
                        .setPositiveButton(R.string.str_app_update)
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                // 앱 업데이트 링크
                                this@SplashActivity.movePlayStore()
                            }
                        })
                        .show()
            })
        }

        if (Config.IS_DEBUG) {
            SelectIpDialog(this@SplashActivity) { ip, port ->
                Config.AUCTION_IP = ip
                Config.AUCTION_PORT = port.toInt()
                viewModel.startSplash()
            }.show()
        } else {
            if (isRootingDevice()) {
                CommonDialog(this)
                        .setContents(R.string.str_integrity_app)
                        .setPositiveButton(R.string.str_app_close)
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                exitApp()
                            }
                        }).show()
            } else {
                viewModel.startSplash()
            }
        }
    }

    // 뒤로가기 버튼 막기
    override fun onBackPressed() {}

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
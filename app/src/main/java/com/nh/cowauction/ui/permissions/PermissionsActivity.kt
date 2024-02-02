package com.nh.cowauction.ui.permissions

import android.Manifest
import android.os.Bundle
import androidx.activity.viewModels
import com.hmju.permissions.SimplePermissions
import com.nh.cowauction.BR
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseActivity
import com.nh.cowauction.databinding.ActivityPermissionsBinding
import com.nh.cowauction.extension.startAct
import com.nh.cowauction.ui.splash.SplashActivity
import com.nh.cowauction.viewmodels.PermissionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionsActivity : BaseActivity<ActivityPermissionsBinding, PermissionsViewModel>() {

    override val layoutId = R.layout.activity_permissions
    override val viewModel: PermissionsViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(viewModel) {

            startPermissions.observe(this@PermissionsActivity, {
                SimplePermissions(this@PermissionsActivity)
                    .requestPermissions(
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.CHANGE_NETWORK_STATE,
                        Manifest.permission.READ_PHONE_STATE
                    ).build { _, _ ->
                        startAct<SplashActivity>(
                            enterAni = android.R.anim.fade_in,
                            exitAni = android.R.anim.fade_out
                        )
                        finish()
                    }
            })
        }
    }

    override fun onBackPressed() {}

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
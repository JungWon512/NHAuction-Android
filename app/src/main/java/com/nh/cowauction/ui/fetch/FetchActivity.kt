package com.nh.cowauction.ui.fetch

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import com.nh.cowauction.BR
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseActivity
import com.nh.cowauction.contants.MainEntryType
import com.nh.cowauction.databinding.ActivityFetchBinding
import com.nh.cowauction.utility.RxBus
import com.nh.cowauction.utility.RxBusEvent
import com.nh.cowauction.viewmodels.FetchViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 조회 관련 페이지 {경매 예정 조회, 경매 결과 조회,
 * 응찰 내역 조회, 구매 내역 조회, 나의 출장우 조회}
 */
@AndroidEntryPoint
class FetchActivity : BaseActivity<ActivityFetchBinding, FetchViewModel>() {
    override val layoutId = R.layout.activity_fetch
    override val viewModel: FetchViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        with(viewModel) {

            startFinish.observe(this@FetchActivity, {
                aucPrgSq.value?.let {
                    RxBus.publish(RxBusEvent.AucPrgSqEvent(it))
                }
                finish()
            })

            start()
        }
    }
}
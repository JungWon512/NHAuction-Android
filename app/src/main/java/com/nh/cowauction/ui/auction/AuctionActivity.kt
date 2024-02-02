package com.nh.cowauction.ui.auction

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.nh.cowauction.BR
import com.nh.cowauction.MainApplication
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseActivity
import com.nh.cowauction.base.BaseFragmentPagerAdapter
import com.nh.cowauction.base.BaseViewModel
import com.nh.cowauction.contants.AuctionState
import com.nh.cowauction.contants.ExtraCode
import com.nh.cowauction.contants.FragmentType
import com.nh.cowauction.contants.NetworkAuctionConnection
import com.nh.cowauction.databinding.ActivityAuctionBinding
import com.nh.cowauction.extension.initActivityResult
import com.nh.cowauction.extension.initFragment
import com.nh.cowauction.extension.startAct
import com.nh.cowauction.repository.tcp.NettyClient
import com.nh.cowauction.ui.dialog.CommonDialog
import com.nh.cowauction.ui.fetch.FetchActivity
import com.nh.cowauction.utility.DLogger
import com.nh.cowauction.utility.RxBus
import com.nh.cowauction.utility.RxBusEvent
import com.nh.cowauction.viewmodels.AuctionViewModel
import dagger.hilt.android.AndroidEntryPoint
import okio.ArrayIndexOutOfBoundsException
import javax.inject.Inject

@AndroidEntryPoint
class AuctionActivity : BaseActivity<ActivityAuctionBinding, AuctionViewModel>() {
    override val layoutId = R.layout.activity_auction
    override val viewModel: AuctionViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    private val permissionsSettingResult = initActivityResult {
        viewModel.refreshRooms()
    }

    private var finishDialog: CommonDialog? = null

    @Inject
    lateinit var nettyClient: NettyClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        with(viewModel) {

            topCurrentState.observe(this@AuctionActivity) { state ->
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (topCurrentPos.value != topPrevPos.value) {
                        startAgoraEvent(topCurrentPos.value)
                    }

                    if (topCurrentPos.value != topPrevPos.value) {
                        topPrevPos.value = topCurrentPos.value
                    }
                }
            }

            toastMessage.observe(this@AuctionActivity) { msg ->
                this@AuctionActivity.showToast(msg)
            }

            startAuctionFinish.observe(this@AuctionActivity) { state ->
                finishDialog?.dismiss()
                finishDialog = null
                if (state == NetworkAuctionConnection.RECONNECT) {
                    finishDialog = CommonDialog(this@AuctionActivity)
                        .setContents(state.msg)
                        .setPositiveButton(R.string.str_reconnect)
                        .setNegativeButton(R.string.str_cancel)
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == CommonDialog.POSITIVE) {
                                    viewModel.refreshClient()
                                } else {
                                    viewModel.closeClient()
                                }
                            }
                        })

                } else {
                    finishDialog = CommonDialog(this@AuctionActivity)
                        .setContents(state.msg)
                        .setPositiveButton(R.string.str_confirm)
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                viewModel.closeClient()
                            }
                        })
                }

                finishDialog?.show()
            }

            startShake.observe(this@AuctionActivity) {
                binding.layoutBidding.clPrice
                    .startAnimation(
                        AnimationUtils.loadAnimation(
                            this@AuctionActivity, R.anim.shake
                        )
                    )
            }

            startFetchAuction.observe(this@AuctionActivity) {
                startAct<FetchActivity>()
            }

            auctionStateMessage.observe(this@AuctionActivity) {
                DLogger.d("AuctionStatusMessage $it")
                // 응찰 성공 및 카운트 다운 제외한 나머지
                if (it != AuctionState.BIDDING_SUCCESS &&
                    it != AuctionState.COUNT_DOWN &&
                    it != AuctionState.BATCH_SELECTION &&
                    it != AuctionState.BATCH_BIDDING
                ) {
                    setAuctionState(it)
                }
            }

            startFinish.observe(this@AuctionActivity) {
                finish()
            }

            startReadPhonePermissions.observe(this@AuctionActivity) {
                CommonDialog(this@AuctionActivity)
                    .setContents(R.string.str_streaming_service_permissions)
                    .setPositiveButton(R.string.str_permissions_setting)
                    .setNegativeButton(R.string.str_cancel)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            if (which == CommonDialog.POSITIVE) {
                                // 전화 권한 설정 페이지 이동
                                permissionsSettingResult.launch(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:$packageName")
                                    ).apply {
                                        addCategory(Intent.CATEGORY_DEFAULT)
                                    }
                                )
                            }
                        }
                    })
                    .show()
            }

            startOneButtonPopup.observe(this@AuctionActivity) {
                CommonDialog(this@AuctionActivity)
                    .setContents(it)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            }

            start()
        }
    }

    override fun onResume() {
        super.onResume()
        if (MainApplication.gAppStatus == MainApplication.AppStatus.RETURNED_TO_FOREGROUND) {
            DLogger.d("### RETURNED_TO_FOREGROUND")
            viewModel.agoraLiveProvider.enableVideo()
            // 비동기 통신이 아닌 engine 자체의 오디오 / 비디오 활성화 처리하는 로직으로 변경함
//            RxBus.publish(RxBusEvent.LiveSoundEvent(isSoundOn = true, isForeGround = true))
            if (!nettyClient.isServerOn()) {
                finishDialog?.dismiss()
                finishDialog = null
                CommonDialog(this@AuctionActivity)
                        .setContents(R.string.str_message_auction_network_reconnection)
                        .setPositiveButton(R.string.str_reconnect)
                        .setNegativeButton(R.string.str_cancel)
                        .setListener(object : CommonDialog.Listener {
                            override fun onClick(which: Int) {
                                if (which == CommonDialog.POSITIVE) {
                                    viewModel.refreshClient()
                                } else {
                                    viewModel.closeClient()
                                }
                            }
                        })
                        .show()
            }
        }
    }

    override fun onBackPressed() {
        CommonDialog(this)
                .setContents(R.string.str_auction_exit_contents)
                .setPositiveButton(R.string.str_exit)
                .setNegativeButton(R.string.str_cancel)
                .setListener(object : CommonDialog.Listener {
                    override fun onClick(which: Int) {
                        if (which == CommonDialog.POSITIVE) {
                            viewModel.clearAgoraServiceInfo()
                            viewModel.closeClient()
                        }
                    }
                })
                .show()
    }

    /**
     * 상단 ViewPager
     */
    class AuctionTopFragmentPagerAdapter(ctx: Context, private val viewModel: BaseViewModel?) :
            BaseFragmentPagerAdapter<String>(ctx) {
        override fun onCreateFragment(pos: Int) = if (pos == 0) {
            initFragment<AuctionInfoFragment>()
        } else {
            initFragment<AuctionCamFragment> {
                putInt(ExtraCode.AUCTION_CAM_POS, pos)
                try {
                    if (viewModel is AuctionViewModel) {
                        putString(
                                ExtraCode.AUCTION_CAM_CHANNEL_ID,
                                viewModel.liveChannelList[pos - 1]
                        )
                    }
                } catch (ex: ArrayIndexOutOfBoundsException) {
                    DLogger.e("initAuctionCamFragment Error $ex")
                }
            }
        }

        override fun getItemCount() = super.getItemCount() + 1

        override fun containsItem(itemId: Long): Boolean {
            // DLogger.d("containsItem $itemId \tCount$itemCount")
            val tmpCount = itemCount.plus(FragmentType.AUCTION_TOP.uniqueId)
            return itemId < tmpCount && itemId >= FragmentType.AUCTION_TOP.uniqueId
        }

        override fun getItemId(pos: Int) = (FragmentType.AUCTION_TOP.uniqueId + pos).toLong()
    }

    /**
     * Viewpager View
     * Full Screen Toggle
     */
    fun resizingFullScreen(state: Boolean){
        if(state){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val params = binding.topViewPager.layoutParams
            params.height  = ViewGroup.LayoutParams.MATCH_PARENT
        }else{
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val params = binding.topViewPager.layoutParams
            params.height  = 0
        }
    }

    override fun onStop() {
        super.onStop()
        DLogger.d("###onstop ${MainApplication.gAppStatus}")
        // 홈 버튼 눌러서 나가면 소리 off
        if (MainApplication.gAppStatus == MainApplication.AppStatus.BACKGROUND) {
            viewModel.agoraLiveProvider.disableVideo()
            // 비동기 통신이 아닌 engine 자체의 오디오 / 비디오 활성화 처리하는 로직으로 변경함
//            RxBus.publish(RxBusEvent.LiveSoundEvent(isSoundOn = false, isBackGround = true))
        }
    }

}
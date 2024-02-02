package com.nh.cowauction.ui.auction

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import com.nh.cowauction.BR
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseFragment
import com.nh.cowauction.contants.CastState
import com.nh.cowauction.databinding.FragmentAuctionCamBinding
import com.nh.cowauction.utility.DLogger
import com.nh.cowauction.viewmodels.AuctionCamViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.video.VideoCanvas

/**
 * Description : 경매 라이브 방송 Fragment
 *
 * Created by hmju on 2021-06-09
 */
@AndroidEntryPoint
class AuctionCamFragment : BaseFragment<FragmentAuctionCamBinding, AuctionCamViewModel>() {

    override val layoutId = R.layout.fragment_auction_cam
    override val viewModel: AuctionCamViewModel by viewModels()
    override val bindingVariable = BR.viewModel

    private lateinit var container: FrameLayout
    private var surfaceView: SurfaceView? = null
    //private var channelId: String = ""
    //private var roomCnt: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view.findViewById(R.id.viewerContainer)

        with(viewModel) {

            playPosition.observe(viewLifecycleOwner) {
                DLogger.d("### playPosition.observe > playPosition :: $it position $position")

                /* 원본코드
                // playPosition.value == 0 일때 list index 0 조인
                if (playPosition.value == 0) {
                    agoraJoin(channellist[playPosition.value])

                }else {
                    // playPosition.value == 0 일때 list index playPosition.value-1 조인
                    if (playPosition.value == position) {
                        agoraJoin(channellist[playPosition.value-1])
                    }
                }
                */

                // KIH liveSoundState.value 조건추가
                if (playPosition.value == 0 && position == 1) {
                    if (liveSoundState.value  && !isJoined) {
                        agoraJoin(channellist[playPosition.value])
                    }
                }else {
                    // playPosition.value == 1 일때 list index playPosition.value-1 조인
                    if (playPosition.value == position) {
                        DLogger.d("### channellist[playPosition.value-1 > playPosition :: $it position $position")
                        agoraJoin(channellist[playPosition.value-1])
                    }
                }
            }

            // rxbus로 agora callback 받고 썸네일 클릭 시 뷰 그리는 처리
            playHandler.observe(viewLifecycleOwner) {
                if (it) {
                    setupRemoteVideo()
                }
            }

            // castState가 playing이 아닌 다른걸로 변경하여 썸네일이 노출되도록함
            thumbnailState.observe(viewLifecycleOwner) {
                DLogger.d("### STOP 썸네일 노출")
                //viewModel._liveCastState.postValue(CastState.JOIN)    // KIH 주석 처리
                viewModel._liveCastState.postValue(CastState.STOP)      // KIH 변경 함.
            }

            liveCastState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    CastState.JOIN -> {
                        DLogger.d("### liveCastState.observe > CastState.JOIN")
                    }
                    // agora stop하는 구문
                    CastState.STOP -> {
                        DLogger.d("### liveCastState.observe > CastState.STOP")
                        stopAgoraLive()
                    }
                    // agora stop하는 구문
                    CastState.ERROR -> {
                        DLogger.d("### liveCastState.observe > CastState.ERROR")
                        binding.clReady.visibility = View.VISIBLE
                        stopAgoraLive()
                    }
                    //
                    CastState.PLAYING -> {
                        DLogger.d("### liveCastState.observe > CastState.PLAYING")
                    }
                }
            }

            liveSoundState.observe(viewLifecycleOwner) { isSoundOn ->
                DLogger.d("### liveSoundState.observe > isSoundOn : $isSoundOn ")
                setLiveVolume(isSoundOn)

                // KIH 추가 - playPosition 0 에서 사운드 on/off 처리 --------- (s)
                if (playPosition.value == 0 && position == 1) {
                    DLogger.d("### liveSoundState.observe > call agoraJoin() #1")
                    if (isSoundOn ) {
                        if(!isJoined) {
                            DLogger.d("### liveSoundState.observe > call agoraJoin() #2")
                            agoraJoin(channellist[0])
                        }
                    } else {
                        if(isJoined) {
                            //stopAgoraLive()
                            viewModel._liveCastState.postValue(CastState.STOP)
                        }
                    }
                }
                // (e)
            }

            start() // rxbus 이벤트 활성화 위해 onCreate에서 처리
            agoraLiveEventCallback() // rxbus 이벤트 활성화 위해 onCreate에서 처리
        }
    }

    /**
     * Agora engine.join 함수
     */
    private fun agoraJoin(channelId: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            val options = ChannelMediaOptions()
            options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            // by kih, 24.01.21 Set Low latency for Broadcast streaming
            options.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
            options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE // 수신자

            viewModel.engine?.joinChannel("", channelId, 0, options)
            DLogger.d("### agoraJoin() > handler list : $channelId")
        }, 300)

        // 현재 화면이 1일때 playhandler true로 변경
//        if (viewModel.playPosition.value == 1) {
//            viewModel.playHandler.value = true
//        }

    }

    /**
     * 뷰 그리기
     *VideoCanvas에 뷰가 그려질 경우 CastState.JOIN -> CastState.PLAYING으로 변경
     * CastState.PLAYING일경우 썸네일 gone
     */
    private fun setupRemoteVideo() {
        // MainThread
        DLogger.d("### setupRemoteVideo 구문 탐")

        Handler(Looper.getMainLooper()).post {
            surfaceView = SurfaceView(requireContext())
            surfaceView?.setZOrderMediaOverlay(true)
            container.addView(surfaceView)
            viewModel.engine?.setupRemoteVideo(
                VideoCanvas(
                    surfaceView,
                    VideoCanvas.RENDER_MODE_ADAPTIVE,
                    viewModel.agoraChannelUid
                )
            )
            surfaceView?.visibility = View.VISIBLE
        }

        viewModel._liveCastState.postValue(CastState.PLAYING)
    }

}
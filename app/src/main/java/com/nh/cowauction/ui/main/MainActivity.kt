package com.nh.cowauction.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.nh.cowauction.BR
import com.nh.cowauction.MainApplication
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseActivity
import com.nh.cowauction.contants.Config
import com.nh.cowauction.contants.ExtraCode
import com.nh.cowauction.databinding.ActivityMainBinding
import com.nh.cowauction.extension.exitApp
import com.nh.cowauction.extension.startAct
import com.nh.cowauction.repository.preferences.AccountPref
import com.nh.cowauction.ui.auction.AuctionActivity
import com.nh.cowauction.ui.auction.WatchAuctionActivity
import com.nh.cowauction.ui.dialog.CommonDialog
import com.nh.cowauction.ui.permissions.PermissionsActivity
import com.nh.cowauction.ui.splash.SplashActivity
import com.nh.cowauction.utility.DLogger
import com.nh.cowauction.utility.RetrofitLogger.Logger.Companion.TAG
import com.nh.cowauction.utility.RxBus
import com.nh.cowauction.utility.RxBusEvent
import com.nh.cowauction.viewmodels.MainViewModel
import com.nh.cowauction.widget.keyboard.FluidContentResize
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    override val layoutId = R.layout.activity_main
    override val viewModel: MainViewModel by viewModels()
    override val bindingVariable = BR.viewModel
    private val DEEPLINK_URL = "https://www.xn--o39an74b9ldx9g.kr/home"
    private val SHORT_DYNAMIC_LINK = "https://nhauction.page.link/link"
    private var isDoubleClick : Boolean = false

    private lateinit var splashFinishDisposable: Disposable

    @Inject
    lateinit var accountPref: AccountPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        FluidContentResize.listen(this)

        // savedInstanceState == null 분기의 의미가 없어서 주석 처리함
//        if (savedInstanceState == null) {
                // 권한 고지 페이지 노출 여부
                if (accountPref.isPermissionsPageShow()) {
                    DLogger.d("### SplashActivity")
                    startAct<SplashActivity>(
                        enterAni = android.R.anim.fade_in,
                        exitAni = android.R.anim.fade_out
                    )
                } else {
                    DLogger.d("### PermissionsActivity")
                    startAct<PermissionsActivity>(
                        enterAni = android.R.anim.fade_in,
                        exitAni = android.R.anim.fade_out
                    )
                }
//        }

        with(viewModel) {

            finish.observe(this@MainActivity, {
                // 웹뷰 히스토리가 있으면
                if (binding.webView.canGoBack()) {
                    // 뒤로가기
                    binding.webView.goBack()

                    this@MainActivity.isDoubleClick = false
                } else {
                    // 히스토리가 없는 경우
                    // 뒤로가기 클릭 시 토스트 메세지 호출
                    this@MainActivity.showToast(R.string.str_back_press_info)
                    if (it) {
                        if (this@MainActivity.isDoubleClick) {
                            clearApplicationCache(cacheDir)
                            exitApp()
                        }else{
                            this@MainActivity.isDoubleClick = true
                        }
                    } else {
                        this@MainActivity.isDoubleClick = true
                    }
                }
            })

            //경매 응찰
            startAuctionBidding.observe(this@MainActivity) {
                startAct<AuctionActivity> {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                }
            }
            //경매 관전
            startWatchAuction.observe(this@MainActivity) {
                startAct<WatchAuctionActivity> {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                    putExtra(ExtraCode.WATCH_AUCTION_URL, viewModel._watchAuctionUrl.value)
                }
            }

            startToastMessage.observe(this@MainActivity, {
                CommonDialog(this@MainActivity)
                    .setContents(it)
                    .setPositiveButton(R.string.str_confirm)
                    .show()
            })
        }

        splashFinishDisposable = RxBus.listen(RxBusEvent.MainEnterEvent::class.java)
            .subscribe({
                DLogger.d("onCompleted ${it}")
                mainStart()
                performTargetUrl(intent)
                DynamicLinkListener(intent)
            }, {

            })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mainStart()
        performTargetUrl(intent)
        /** 딥링크 수신*/
        DynamicLinkListener(intent)
    }

    override fun finish() {
        super.finish()
        if (!splashFinishDisposable.isDisposed) splashFinishDisposable.dispose()
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    /** DynamicLink 수신 */
    fun DynamicLinkListener(intent: Intent?) {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent!!)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                var dynamicLink: Uri?
                if (pendingDynamicLinkData != null) {

                    dynamicLink = pendingDynamicLinkData.link // deeplink로 app 넘어 왔을 경우

                    /** 딥링크 데이터 수신성공 */
                    Log.e("cow manager", "dynamicLink: $dynamicLink")

                    val dynamicLinkparam: String? = dynamicLink?.getQueryParameter("urlParam")
                    val dynamicLinkparam2: Array<String> =
                        dynamicLink.toString().split("urlParam=").toTypedArray()

                    Log.d("cow manager", "dynamicLinkparam: $dynamicLinkparam")
                    Log.d("cow manager", "dynamicLinkparam2: ${dynamicLinkparam2[1]}")

                    if (dynamicLinkparam != null) {
                        viewModel.setWebUrl(dynamicLinkparam.toString())
                    } else {
                        viewModel.setWebUrl(dynamicLinkparam2[1])
                    }

//                    intent.getStringExtra(ExtraCode.DYNAMIC_LINK_TARGET_URL)?.let {
//                        viewModel.setWebUrl(dynamicLink.toString())
//                    }
                    intent.removeExtra(ExtraCode.DYNAMIC_LINK_TARGET_URL)

                } else {
                    if (pendingDynamicLinkData == null) { // app으로 실행 했을 경우 (deeplink 없는 경우)
                        Log.d(TAG, "No have dynamic link")
                        return@addOnSuccessListener
                    }
                }
            }

            /** 딥링크 데이터 수신실패 */
            .addOnFailureListener { e ->
                Log.w(TAG, "getDynamicLink : onFailure", e)
            }
    }

    /**
     * DeepLink 처리 함수
     * @param intent Intent
     */
    private fun performTargetUrl(intent: Intent?) {
        if (intent == null) return

        DLogger.d("performTargetUrl ${intent.getStringExtra(ExtraCode.DEEP_LINK_TARGET_URL)}")
        intent.getStringExtra(ExtraCode.DEEP_LINK_TARGET_URL)?.let {
            viewModel.setWebUrl(Config.BASE_DOMAIN + it)
        }
        intent.removeExtra(ExtraCode.DEEP_LINK_TARGET_URL)
    }

    /**
     * 메인 시작
     */
    private fun mainStart() {

        if (application is MainApplication) {
            (application as MainApplication).introActivity?.get()?.finish()
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        window.decorView.setBackgroundColor(Color.WHITE)
        viewModel.start()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // 다른데 영역 터치시 키보드 내리기
        if (currentFocus != null && currentFocus is EditText) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun clearApplicationCache(dir: File?): Boolean {
        try {
            if (dir != null && dir.isDirectory) {
                val children = dir.list()
                if (children != null) {
                    for (idx in children.indices) {
                        val child = children.get(idx)
                        val isSuccess = clearApplicationCache(File(dir, child))
                        if (!isSuccess) {
                            return false
                        }
                    }
                } else {
                    return true
                }
            }
        } catch (ex: SecurityException) {
            DLogger.e("Clear Cache Error $ex")
        }
        return dir?.delete() ?: true
    }
}
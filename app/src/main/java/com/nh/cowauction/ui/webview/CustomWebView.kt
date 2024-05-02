package com.nh.cowauction.ui.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nh.cowauction.BuildConfig
import com.nh.cowauction.R
import com.nh.cowauction.base.BaseViewModel
import com.nh.cowauction.contants.Config
import com.nh.cowauction.extension.exitApp
import com.nh.cowauction.extension.multiNullCheck
import com.nh.cowauction.extension.toIntOrDef
import com.nh.cowauction.model.auction.WatchInfo
import com.nh.cowauction.model.receive.CurrentEntryInfo
import com.nh.cowauction.model.user.UserInfo
import com.nh.cowauction.repository.tcp.login.LoginManager
import com.nh.cowauction.ui.dialog.CommonDialog
import com.nh.cowauction.ui.main.MainActivity
import com.nh.cowauction.utility.DLogger
import com.nh.cowauction.utility.DeviceProvider
import com.nh.cowauction.utility.NetworkConnectionProvider
import com.nh.cowauction.utility.RxBus
import com.nh.cowauction.utility.RxBusEvent
import com.nh.cowauction.viewmodels.FetchViewModel
import com.nh.cowauction.viewmodels.MainViewModel
import com.nh.cowauction.viewmodels.WatchAuctionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject


/**
 * Description : 웹뷰
 *
 * Created by hmju on 2021-07-14
 */
@AndroidEntryPoint
class CustomWebView @JvmOverloads constructor(
    private val ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(ctx, attrs, defStyleAttr), LifecycleOwner, LifecycleObserver, NestedScrollingChild {

    interface Listener {
        fun onScroll(scrollY: Int)
    }

    @Inject
    lateinit var deviceProvider: DeviceProvider

    @Inject
    lateinit var loginManager: LoginManager

    @Inject
    lateinit var networkConnectionProvider: NetworkConnectionProvider

    private val activity: Activity by lazy { ctx as Activity }
    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    private var lastY: Int = 0
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private var nestedOffsetY: Int = 0
    private val childHelper: NestedScrollingChildHelper by lazy { NestedScrollingChildHelper(this) }

    private val uiHandler = Handler(Looper.getMainLooper())
    var viewModel: BaseViewModel? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null
    var listener: Listener? = null

    private var currProgress = 0
    private var prevScroll = 0
    private val jsonFormat: Json by lazy {
        Json {
            isLenient = true // Json 큰따옴표 느슨하게 체크.
            ignoreUnknownKeys = true // Field 값이 없는 경우 무시
            coerceInputValues = true // "null" 이 들어간경우 default Argument 값으로 대체
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onStateEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
        DLogger.d("WebView onStateEvent $event")
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
            }
            Lifecycle.Event.ON_RESUME -> {
            }
            else -> {
            }
        }
    }

    override fun getLifecycle() = lifecycleRegistry

    init {
        setWebSetting()
        setCookie()
        isNestedScrollingEnabled = true
        webChromeClient = WebChromeClientClass()
        webViewClient = WebViewClientClass()
        addJavascriptInterface(JavaInterface(), "auctionBridge")

        if (ctx is FragmentActivity) {
            ctx.lifecycle.addObserver(this)
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun setWebSetting() {
        this.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        setNetworkAvailable(true)
        settings.apply {
            javaScriptEnabled = true // 자바 스크립트 허용
            javaScriptCanOpenWindowsAutomatically = true // 팝업창 오픈 기능
            loadWithOverviewMode = true // 웹뷰 화면 맞춤
            useWideViewPort = true // 뷰포트 사용여부
            loadsImagesAutomatically = true // 앱 이미지 리소스 자동 로드
            domStorageEnabled = true // 로컬 스토리지 사용 여부
            loadWithOverviewMode = true // 콘텐츠가 웹뷰보다 클경우 스크린 크기에 맞게 조정.

            setSupportMultipleWindows(true) // 멀티 윈도우 허용
            setGeolocationEnabled(true) // 위치 허용
            textZoom = 100
            cacheMode = WebSettings.LOAD_NO_CACHE // 캐시 거부
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // http 리소스 허용
            if (BuildConfig.DEBUG) {
                setWebContentsDebuggingEnabled(true)
            }
        }
        requestFocus()
        isFocusable = true
        isFocusableInTouchMode = true
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_UP -> {
                    if (v.hasFocus()) {
                        v.requestFocus()
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

    /**
     * Setting Cookies
     */
    private fun setCookie() {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@CustomWebView, true)
        }
    }

    override fun loadUrl(url: String) {
        if (loginManager.getUserToken().isEmpty()) {
            DLogger.d("Normal LoadUrl $url")
            super.loadUrl(url)
        } else {
            val headers = HashMap<String, String>()
            headers["Authorization"] = loginManager.getUserToken()
            DLogger.d("Header LoadUrl $url")
            super.loadUrl(url, headers)
        }
    }

    override fun canGoBack(): Boolean {
        val result = super.canGoBack()
        DLogger.d("canGoBack Current Url $url, $result")
        return result
    }

    override fun goBack() {
        super.goBack()
//        if (canGoBack()) {
//            super.goBack()
//        } else {
////            activity.finish()
//        }
    }

    /**
     * 자바스크립트 함수 호출
     */
    fun callJavaScript(script: String?) {
        if (script.isNullOrEmpty()) return

        DLogger.d("CallJavaScript $script")
        uiHandler.post {
            loadUrl(script)
        }
    }

    /**
     * 자바 스크립트 콜백 함수 호출
     */
    fun fetchJavaScript(script: String?, callback: (String) -> Unit) {
        if (script.isNullOrEmpty()) return

        DLogger.d("fetchJavaScript $script")
        uiHandler.post { evaluateJavascript(script, callback) }
    }

    inner class JavaInterface {

        /**
         * 사용자 정보 Json 형식
         */
        @JavascriptInterface
        fun setUserInfo(userJson: String?) {
            if (userJson.isNullOrEmpty()) return
            DLogger.d("JavaInterface Function setUserInfo $userJson")

            runCatching {
                jsonFormat.decodeFromString<UserInfo>(userJson).run {
                    DLogger.d("UserInfo $this")
                    loginManager.setUserToken(userToken)
                    loginManager.setAuctionCode(auctionCode)
                    loginManager.setAuctionName(auctionCodeName)
                    loginManager.setTraderMngNum(userNum)
                    loginManager.setNearAuctionCode(nearestBranch)
                }
            }.onFailure {
                DLogger.e("JsonParser Error $it")
            }
        }

        @JavascriptInterface
        fun setCowInfo(cowInfo: String?) {

            if (cowInfo.isNullOrEmpty()) return
            DLogger.d("JavaInterface Function setCowInfo $cowInfo")

            runCatching {
                if (viewModel is WatchAuctionViewModel) {

                    val s = cowInfo.trim().split("|")

                    s?.let { it ->
                        val curCowInfo = CurrentEntryInfo(
                            entryNum = it[0],
                            entryGender = it[1],
                            exhibitor = it[2],
                            weight = it[3],
                            cavingNum = it[4],
                            motherTypeCode = it[5],
                            pasgQcn = it[6],
                            entryKpn = it[7],
                            lowPrice = it[8].toIntOrDef(),
                            note = it[9]
                        )

                        curCowInfo?.let { it ->
                            DLogger.d("result watch cow info  $it")
                            (viewModel as WatchAuctionViewModel).onReceiveMessage.onCurrentEntryInfo(
                                it
                            )
                        }
                    }
                }
            }.onFailure { err ->
                DLogger.e("StrDecode Error $err")
            }
        }

        /**
         * 경매 응찰 화면 이동
         */
        @JavascriptInterface
        fun moveAuctionBid() {
            DLogger.d("JavaInterface Function moveAuctionBid ${loginManager.isAuctionAvailable()}")
            if (loginManager.isAuctionAvailable()) {
                if (viewModel is MainViewModel) {
                    (viewModel as MainViewModel).moveAuctionBidding()
                }
            } else {
                // 응찰 가능한 상태가 아닙니다.
                if (activity is MainActivity) {
                    uiHandler.post {
                        runCatching {
                            val token =
                                CookieManager.getInstance().getCookie(originalUrl).split(";").map {
                                    return@map if (it.isNotEmpty() && it.contains("=")) {
                                        val s = it.split("=")
                                        s[0].trim() to s[1].trim()
                                    } else null
                                }.find { if (it != null) it.first == "access_token" else false }
                            DLogger.d("AccessToken $token")
                            fetchJavaScript("javascript:getLoginUserInfo('${token?.second}')") { jsonStr ->
                                DLogger.d("Json $jsonStr")
                                runCatching {
                                    var json = jsonStr.replace("\\", "")
                                    if (json.indexOf('\"') == 0) {
                                        json = json.slice(1 until json.lastIndex)
                                    }
                                    jsonFormat.decodeFromString<UserInfo>(json).run {
                                        DLogger.d("User Info $this")
                                        if (success) {
                                            loginManager.setAuctionCode(auctionCode)
                                            loginManager.setAuctionName(auctionCodeName)
                                            loginManager.setTraderMngNum(userNum)
                                            loginManager.setUserToken(token?.second)
                                            loginManager.setNearAuctionCode(nearestBranch)
                                            if (viewModel is MainViewModel) {
                                                (viewModel as MainViewModel).moveAuctionBidding()
                                            }
                                        } else {
                                            (activity as MainActivity).showToast("응찰 가능한 상태가 아닙니다.")
                                        }
                                    }
                                }.onFailure { err ->
                                    DLogger.e("JsonDecode Error $err")
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * 경매 관전 이동
         */
        @JavascriptInterface
        fun moveAuctionWatch(watchJson: String?) {
            if (watchJson.isNullOrEmpty()) {
                DLogger.d("JavaInterface Function moveAuctionWatch NULL")
                return
            }

            DLogger.d("JavaInterface Function moveAuctionWatch $watchJson")
            runCatching {

                jsonFormat.decodeFromString<WatchInfo>(watchJson).run {
                    DLogger.d("watchInfo $this")

                    multiNullCheck(this.url, this.watch_token) { url, token ->

                        fetchJavaScript("javascript:getLoginUserInfo('${token}')") { jsonStr ->
                            DLogger.d("Json $jsonStr")
                            runCatching {
                                var json = jsonStr.replace("\\", "")
                                if (json.indexOf('\"') == 0) {
                                    json = json.slice(1 until json.lastIndex)
                                }
                                jsonFormat.decodeFromString<UserInfo>(json).run {
                                    DLogger.d("User Info $this")
                                    if (success) {

                                        loginManager.setAuctionCode(auctionCode)
                                        loginManager.setWatchAuctionName(resources.getString(R.string.str_watch_auction))
                                        loginManager.setUserWatchToken(token)

                                        if (viewModel is MainViewModel) {
                                            (viewModel as MainViewModel).moveWatchAuction(url)
                                        }

                                    } else {
                                        (activity as MainActivity).showToast("경매 관전 접속에 실패했습니다.")
                                    }
                                }
                            }.onFailure { err ->
                                DLogger.e("JsonDecode Error $err")
                            }
                        }
                    }
                }
            }.onFailure {
                DLogger.e("JsonParser Error $it")
            }
        }

        @JavascriptInterface
        fun setAucPrgSq(aucPrgSq: String?) {
            DLogger.d("[응찰 내역 출장우 번호]=> $aucPrgSq")
            aucPrgSq?.let {
                uiHandler.post {
                    if (viewModel is FetchViewModel) {
                        (viewModel as FetchViewModel).setAucPrgSq(it)
                    }
                }
            }
        }

        @JavascriptInterface
        fun getAppVersionInfo(): String {
            var currVersion = deviceProvider.getVersionName()
            DLogger.d("Return Application Version : $currVersion")

            return currVersion
        }

        @JavascriptInterface
        fun callUrl(url : String?) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            activity.startActivity(intent)
            DLogger.d("Return Application Version : $url")
        }
//        @JavascriptInterface
//        fun moveWebPage(url: String?) {
//            if (url.isNullOrEmpty()) return
//
//            DLogger.d("JavaInterface Function moveWebPage $url")
//
//            uiHandler.post {
//                activity.startAct<CommonWebActivity> {
////                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
////                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    putExtra(ExtraCode.WEB_URL, url)
//                }
//            }
//        }
    }

    inner class WebChromeClientClass : WebChromeClient() {
        @SuppressLint("SetJavaScriptEnabled")
        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            if (resultMsg == null) return false

            val popupWebView = WebView(activity).apply {
                settings.javaScriptEnabled = true
            }

            val dialog = Dialog(activity, R.style.FullScreenDialog).apply {
                setContentView(popupWebView)
                window?.attributes?.also {
                    it.width = ViewGroup.LayoutParams.MATCH_PARENT
                    it.height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                setOnDismissListener {
                    popupWebView.destroy()
                }
            }
            popupWebView.webChromeClient = object : WebChromeClient() {
                override fun onCloseWindow(window: WebView?) {
                    dialog.dismiss()
                    window?.destroy()
                }
            }
            popupWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }

            dialog.show()
            (resultMsg.obj as WebViewTransport).webView = popupWebView
            resultMsg.sendToTarget()
            return true
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            DLogger.d("onProgressChanged $newProgress")
            currProgress = newProgress
        }
    }

    inner class WebViewClientClass : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            DLogger.d("onPageStarted $url")
            if (networkConnectionProvider.isNetworkAvailable()) {
                viewModel?.onLoadingShow()
            } else {
                view?.stopLoading()
                CommonDialog(context)
                    .setContents(R.string.str_network_error_msg)
                    .setPositiveButton(R.string.str_confirm)
                    .setListener(object : CommonDialog.Listener {
                        override fun onClick(which: Int) {
                            activity.exitApp()
                        }
                    })
                    .show()
            }

        }

        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            DLogger.d("doUpdateVisitedHistory isReload $isReload $url")
            if (url.equals(Config.BASE_DOMAIN) || url.equals(Config.BASE_DOMAIN + "/home")) {
                clearHistory()
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url.toString()

            if (url.startsWith("tel:")) {
                val mIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                activity.startActivity(mIntent)

                return true
            }
            DLogger.d("shouldOverrideUrlLoading $url")

            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            DLogger.d("onPageFinished $url")

            viewModel?.onLoadingDismiss()
            CookieManager.getInstance().flush()
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        var returnValue = false
        val event = MotionEvent.obtain(ev)
        val action = ev?.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            nestedOffsetY = 0
        }
        val eventY = event.y.toInt()
        event.offsetLocation(0f, nestedOffsetY.toFloat())
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                var deltaY: Int = lastY - eventY
                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY, scrollConsumed, scrollOffset)) {
                    deltaY -= scrollConsumed[1]
                    lastY = eventY - scrollOffset[1]
                    event.offsetLocation(0f, -scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
                }
                returnValue = super.onTouchEvent(event)

                // NestedScroll
                if (dispatchNestedScroll(0, scrollOffset[1], 0, deltaY, scrollOffset)) {
                    event.offsetLocation(0f, scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
                    lastY -= scrollOffset[1]
                }
            }
            MotionEvent.ACTION_DOWN -> {
                returnValue = super.onTouchEvent(event)
                lastY = eventY
                // start NestedScroll
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                returnValue = super.onTouchEvent(event)
                // end NestedScroll
                stopNestedScroll()
            }
        }
        return returnValue
    }

    // Nested Scroll implements
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return childHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        childHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return childHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return childHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        swipeRefreshLayout?.isEnabled = prevScroll == 0 && scrollY == 0
//        DLogger.d("onOverScrolled $scrollY\tPrevScroll$prevScroll\t${swipeRefreshLayout?.isEnabled}")
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        prevScroll = t
        listener?.onScroll(t)
    }
}
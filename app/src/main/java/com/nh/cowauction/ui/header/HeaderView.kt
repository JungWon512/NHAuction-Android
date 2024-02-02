package com.nh.cowauction.ui.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.nh.cowauction.BR
import com.nh.cowauction.R
import com.nh.cowauction.contants.HeaderType
import com.nh.cowauction.extension.initBinding
import com.nh.cowauction.extension.toIntOrDef
import com.nh.cowauction.livedata.NonNullLiveData
import com.nh.cowauction.repository.tcp.login.LoginManager
import com.nh.cowauction.ui.auction.AuctionActivity
import com.nh.cowauction.ui.auction.WatchAuctionActivity
import com.nh.cowauction.utility.DLogger
import com.nh.cowauction.utility.ResourceProvider
import com.nh.cowauction.utility.RxBus
import com.nh.cowauction.utility.RxBusEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Description : DrawerLayout 기반의 HeaderView
 *
 * Created by juhongmin on 6/2/21
 */
@AndroidEntryPoint
class HeaderView @JvmOverloads constructor(
        ctx: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : CoordinatorLayout(ctx, attrs, defStyleAttr), LifecycleOwner, LifecycleObserver {

    @Inject
    lateinit var loginManager: LoginManager

    @Inject
    lateinit var resProvider: ResourceProvider


    private var type: HeaderType = HeaderType.NONE
    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }
    private val activity: FragmentActivity by lazy { context as FragmentActivity }

    private var _binding: ViewDataBinding? = null
    val binding: ViewDataBinding get() = _binding!!

    private val _userNum: MutableLiveData<Int> by lazy { MutableLiveData() }
    val userNum: LiveData<Int> get() = _userNum
    private val _fmtText :MutableLiveData<String> by lazy { MutableLiveData(resProvider.getString(R.string.fmt_participation_num)) }
    val fmtText: LiveData<String> get() = _fmtText

    private val _isFullScreen = NonNullLiveData(false)
    val isFullScreen: LiveData<Boolean> get() = _isFullScreen

    // 현재 어떤 화면인지 true 관전, false 응찰
    private val _isAuctionActivity = NonNullLiveData(true)
    val isAuctionActivity: LiveData<Boolean> get() = _isAuctionActivity

    init {
        fitsSystemWindows = true
        if (!isInEditMode) {
            context.obtainStyledAttributes(attrs, R.styleable.HeaderView).run {
                try {
                    type = HeaderType.values()[getInt(R.styleable.HeaderView_headerType, 0)]
                } finally {
                }
                recycle()
            }

            _binding = initBinding(type.id, this) {
                setVariable(BR.headerView, this@HeaderView)
            }

            activity.lifecycle.addObserver(this)
        } else {
            val tempView = LayoutInflater.from(context).inflate(HeaderType.NONE.id, this, false)
            addView(tempView)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onStateEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
//        DLogger.d("onStateEvent $event")
        when (event) {
            Lifecycle.Event.ON_CREATE -> {

                // 경매 응찰 화면에서는 fullview 아이콘 안 보이도록 수정 (false->gone, true->visible)
                if (activity is AuctionActivity) {
                    _isAuctionActivity.postValue(false)
                }

                _userNum.postValue(loginManager.getUserNum().toIntOrDef())
            }
            Lifecycle.Event.ON_RESUME -> {

            }
            Lifecycle.Event.ON_PAUSE -> {

            }
            Lifecycle.Event.ON_STOP -> {

            }
            Lifecycle.Event.ON_DESTROY -> {

            }
            else -> {
            }
        }
    }

    override fun getLifecycle() = lifecycleRegistry

    fun setHeaderUserName(userName: String?) {
        if (!userName.isNullOrEmpty()) {
            DLogger.d("UserName $userName")
//            _userName.value = userName
        }
    }

    /**
     * set Header Title
     * @param title
     */
    fun setHeaderTitle(title: String?) {
        if (!title.isNullOrEmpty()) {
//            _headerTitle.value = title
        }
    }

    fun setWishPrice(price: Int?) {
        if (price != null) {
//            _wishPrice.value = price
        }
    }

    fun setPartNum(part: Int?) {
        if (part != null) {
//            _userNum.value = part
        }
    }

    fun onMenu() {
    }

    fun onBack() {
        if (activity is AuctionActivity || activity is WatchAuctionActivity) {
            activity.onBackPressed()
        }
    }

    fun onSound(v: AppCompatImageView) {
        v.isSelected = !v.isSelected
        DLogger.d("### v.isSelected ${v.isSelected}")
        RxBus.publish(RxBusEvent.LiveSoundEvent(isSoundOn = v.isSelected, isBackGround = false, isForeGround = false))
    }

    /**
     * 경매,경매관전
     * Full Screen Toggle
     */
    fun toggleFullScreen(state :Boolean) {
        _isFullScreen.postValue(!state)

        if (activity is AuctionActivity) {
            (activity as AuctionActivity).resizingFullScreen(!state)
        }
        if (activity is WatchAuctionActivity) {
            (activity as WatchAuctionActivity).resizingFullScreen(!state)
        }
    }

}
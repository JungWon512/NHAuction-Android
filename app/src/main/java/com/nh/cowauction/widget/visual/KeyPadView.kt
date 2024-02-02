package com.nh.cowauction.widget.visual

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.nh.cowauction.extension.getFragmentAct
import com.nh.cowauction.utility.DLogger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.concurrent.TimeUnit

/**
 * Description : Key Pad 전용 View
 *
 * Created by hmju on 2021-08-10
 */
class KeyPadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleOwner, LifecycleObserver {

    companion object {
        const val CLICK_INTERVAL = 500L
    }

    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }
    private val subject: Subject<Long> by lazy {
        BehaviorSubject.createDefault(0L).toSerialized()
    }
    private val performObservable =
        Flowable.interval(100, TimeUnit.MILLISECONDS).publish().refCount()
    private var isPress = false

    init {
        isHapticFeedbackEnabled = true

        val activity = context.getFragmentAct()
        if (activity is FragmentActivity) {
            activity.lifecycle.addObserver(this)
        }

        subject.toFlowable(BackpressureStrategy.BUFFER)
            .observeOn(AndroidSchedulers.mainThread())
            .delay(CLICK_INTERVAL, TimeUnit.MILLISECONDS)
            .distinctUntilChanged { t1, t2 ->
                val diffTime = Math.abs(t2 - t1)
                diffTime < CLICK_INTERVAL
            }
            .subscribe {
                if (it == 0L) return@subscribe
                handleLongClick()
            }.addTo(compositeDisposable)
    }

    private var performDisposable: Disposable? = null

    private fun handleLongClick() {
        performDisposable?.dispose()
        performDisposable = null
        performDisposable = performObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (isPress) {
                     performClick()
                } else {
                    performDisposable?.dispose()
                    performDisposable = null
                }
            }.addTo(compositeDisposable)
    }

    override fun getLifecycle() = lifecycleRegistry

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onStateEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
            }
            Lifecycle.Event.ON_RESUME -> {
            }
            Lifecycle.Event.ON_DESTROY -> {
                compositeDisposable.clear()
                if (!compositeDisposable.isDisposed) {
                    compositeDisposable.dispose()
                }
            }
            else -> {
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        DLogger.d("onTouch ${event?.action}")
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                isPress = true
                val time = System.currentTimeMillis()
                subject.onNext(time)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPress = false
            }
        }
        return super.onTouchEvent(event)
    }
}
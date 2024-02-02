package com.nh.cowauction.base

import androidx.lifecycle.ViewModel
import com.nh.cowauction.contants.LoadingDialogState
import com.nh.cowauction.extension.request
import com.nh.cowauction.livedata.SingleLiveEvent
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo

/**
 * Description : BaseViewModel Class
 *
 * Created by hmju on 2021-05-18
 */
open class BaseViewModel : ViewModel() {

    protected val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    val startLoadingDialog: SingleLiveEvent<LoadingDialogState> by lazy { SingleLiveEvent() }

    /**
     * RxJava 로 작업 요청시 로딩바 보이게 하기 위한 확장 함수.
     * subscribeOn 보다 위에 있어야 한다.
     */
    protected fun <T : Any, S : Single<T>> S.doLoading(): Single<T> =
        doOnSubscribe { onLoadingShow() }

    /**
     * 로딩 다이얼로그 Show
     */
    fun onLoadingShow() {
        if (startLoadingDialog.value != LoadingDialogState.SHOW) {
            startLoadingDialog.postValue(LoadingDialogState.SHOW)
        }
    }

    /**
     * 로딩 다이얼로그 Dismiss
     */
    fun onLoadingDismiss() {
        if (startLoadingDialog.value != LoadingDialogState.DISMISS) {
            startLoadingDialog.postValue(LoadingDialogState.DISMISS)
        }
    }

    /**
     * ViewModel
     * 네트워크 공통 처리 함수.
     * 쓰레드 세팅 자동 io, UiThread 타입.
     * @param success Api Call Success 콜백 고차 함수.
     * @param failure Api Call 실패시 콜백 고차 함수.
     * @author hmju
     */
    protected inline fun <reified T : Any> Single<T>.request(
            crossinline success: (T) -> Unit = {},
            crossinline failure: (Throwable) -> Unit = {}
    ) {
        request(
                loadingDialog = startLoadingDialog,
                errorDialog = null,
                success = success,
                failure = failure
        ).addTo(compositeDisposable)
    }

    /**
     * Activity / Fragment 에서 onDestroy 시 리소스들 정리
     * clear
     */
    fun clearDisposable() {
        compositeDisposable.clear()
    }

    /**
     * ViewModel 이 제거 되는 시점
     */
    override fun onCleared() {
        super.onCleared()
        // Disposable 구독 해제.
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }
}
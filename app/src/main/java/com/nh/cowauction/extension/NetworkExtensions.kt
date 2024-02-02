package com.nh.cowauction.extension

import androidx.lifecycle.MutableLiveData
import com.nh.cowauction.contants.LoadingDialogState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleTransformer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.observers.ConsumerSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

inline fun <reified T> Single<T>.applyApiScheduler() : Single<T> = compose { it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }

inline fun <reified T> handleNetworkErrorRetry(): SingleTransformer<T, T> =
    SingleTransformer { emit ->
        emit.retryWhen {
            it.take(3).flatMap { err ->
                when (err) {
                    is HttpException -> {
                        when (err.code()) {
                            in IntRange(201,299)-> return@flatMap Flowable.timer(500, TimeUnit.MILLISECONDS)
                            else -> return@flatMap Flowable.error(err)
                        }
                    }
                    else -> return@flatMap Flowable.error(err)
                }
            }
        }
    }

/**
 *  Network 공통 처리 함수.
 *  로딩 프로그래스 다이얼로끄 및 네트워크 에러 다이얼로그는 아래 함수 내에서 처리 하므로 Receive 처리에서 처리할 필요 없음.
 *
 *  @param loadingDialog 로딩 프로그래스바 (Optional)
 *  @param errorDialog 네트워크 에러 다이얼 로그 (Optional)
 *  @param success Api Call Success 콜백 함수
 *  @param failure Api Call 실패시 콜백 함수 (Optional)
 *  @sample
 *  Case 1. -> 공통 실패 처리 후 따로 처리를 해야 하는 경우.
 *  {
 *      ApiService.Interface()
 *          .request(true, loadingDialog, errorDialog, { response->
 *              성공후 데이터 처리..
 *          }, {
 *              실패에 대한 후처리
 *          }
 *  }
 *  Case 2. -> 공통 실패 처리 후 처리가 없는 경우.
 *  {
 *      ApiService.Interface()
 *          .request(true, loadingDialog, errorDialog, { response->
 *              성공후 데이터 처리..
 *          }
 *  }
 *  @author hmju
 */
inline fun <reified T : Any> Single<T>.request(
    loadingDialog: MutableLiveData<LoadingDialogState>? = null,
    errorDialog: MutableLiveData<Any>? = null,
    crossinline success: (T) -> Unit,
    crossinline failure: (Throwable) -> Unit = {}
): Disposable {
    val observer: ConsumerSingleObserver<T> = ConsumerSingleObserver({ response ->
        // 로딩 프로그래스 노출된 상태면 -> 숨김
        if (LoadingDialogState.DISMISS != loadingDialog?.value) {
            loadingDialog?.postValue(LoadingDialogState.DISMISS)
        }
        success.invoke(response)
    }, { throwable ->
        if (LoadingDialogState.DISMISS != loadingDialog?.value) {
            loadingDialog?.postValue(LoadingDialogState.DISMISS)
        }

        // TODO Error Dialog Perform
        failure.invoke(throwable)
    })
    subscribe(observer)
    return observer
}
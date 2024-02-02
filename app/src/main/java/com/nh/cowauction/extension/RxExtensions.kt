package com.nh.cowauction.extension

import io.reactivex.rxjava3.subscribers.DisposableSubscriber

open class SimpleDisposableSubscriber<T> : DisposableSubscriber<T>() {
    override fun onNext(t: T) {
    }

    override fun onError(t: Throwable?) {
    }

    override fun onComplete() {
    }
}

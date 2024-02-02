package com.nh.cowauction.livedata

import androidx.lifecycle.MutableLiveData

/**
 * Description : NonNull Mutable LiveData.
 *
 * Created by hmju on 2021-05-18
 */
class NonNullLiveData<T>(defValue: T) : MutableLiveData<T>() {
    init {
        value = defValue
    }

    override fun getValue() = super.getValue()!!
}
package com.nh.cowauction.viewmodels

import com.nh.cowauction.base.BaseViewModel
import com.nh.cowauction.livedata.SingleLiveEvent
import com.nh.cowauction.repository.preferences.AccountPref
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description :
 *
 * Created by hmju on 2021-10-07
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val accountPref: AccountPref
) : BaseViewModel() {

    val startPermissions : SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }

    fun onConfirm(){
        accountPref.setPermissionsPage(true)
        startPermissions.call()
    }
}
package com.nh.cowauction.repository.preferences

import javax.inject.Inject

class AccountPref @Inject constructor(
    private val pref: BasePref
) {
    private val PREF_PERMISSIONS = "permissions_check"

    fun setPermissionsPage(isDone: Boolean = true) {
        pref.setValue(PREF_PERMISSIONS,isDone)
    }

    fun isPermissionsPageShow() = pref.getValue(PREF_PERMISSIONS,false)
}
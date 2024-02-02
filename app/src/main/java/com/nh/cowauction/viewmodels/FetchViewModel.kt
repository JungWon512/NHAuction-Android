package com.nh.cowauction.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nh.cowauction.base.BaseViewModel
import com.nh.cowauction.contants.Config
import com.nh.cowauction.livedata.SingleLiveEvent
import com.nh.cowauction.repository.tcp.login.LoginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Description : 조회 페이지.
 *
 * Created by hmju on 2021-06-16
 */
@HiltViewModel
class FetchViewModel @Inject constructor(
        private val loginManager: LoginManager
) : BaseViewModel() {

    val startFinish: SingleLiveEvent<Unit> by lazy { SingleLiveEvent() }
    private val _webUrl: MutableLiveData<String> by lazy { MutableLiveData() }
    val webUrl: LiveData<String> get() = _webUrl
    val aucPrgSq: MutableLiveData<Int> by lazy { MutableLiveData() }

    fun start() {
        // https://가축시장.kr/auction/api/entryListApi?naBzplc=8808990656656&loginNo=35
        val url = String.format(Config.BASE_FETCH_FMT_WEB_URL, loginManager.getAuctionCode(), loginManager.getTraderMngNum())
        _webUrl.postValue(url)
    }

    fun close() {
        startFinish.call()
    }

    fun setAucPrgSq(num:String){
        aucPrgSq.value = num.toInt()
        close()
    }
}
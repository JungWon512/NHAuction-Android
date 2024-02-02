package com.nh.cowauction.utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Description :
 *
 * Created by hmju on 2021-10-12
 */

interface NetworkConnectionProvider {
    fun isNetworkAvailable(): Boolean
}

class NetworkConnectionProviderImpl @Inject constructor(
        @ApplicationContext private val context: Context
) : NetworkConnectionProvider {

    private var manager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val builder: NetworkRequest.Builder = NetworkRequest.Builder().apply {
        addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    private lateinit var connectivityCallback: ConnectivityManager.NetworkCallback
    private var isNetworkConnection: Boolean = false
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isNetworkConnection = isNetworkAvailableFunc()
        }
    }

    init {
        isNetworkConnection = isNetworkAvailableFunc()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> manager.registerDefaultNetworkCallback(
                    connectivityCallbackApi23()
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> networkAvailableRequestApi23()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> networkAvailableRequestApi21()
            else -> {
                @Suppress("DEPRECATION")
                context.registerReceiver(
                        networkReceiver,
                        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )
            }
        }
    }


    private fun networkAvailableRequestApi21() {
        manager.registerNetworkCallback(builder.build(), connectivityCallbackApi21())
    }

    private fun networkAvailableRequestApi23() {
        manager.registerNetworkCallback(builder.build(), connectivityCallbackApi23())
    }

    private fun connectivityCallbackApi21(): ConnectivityManager.NetworkCallback {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    isNetworkConnection = true
                }

                override fun onLost(network: Network) {
                    isNetworkConnection = false
                }
            }
            return connectivityCallback
        } else {
            throw IllegalAccessError("Accessing wrong API version")
        }
    }

    private fun connectivityCallbackApi23(): ConnectivityManager.NetworkCallback {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                        network: Network,
                        capabilities: NetworkCapabilities
                ) {
                    if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    ) {
                        isNetworkConnection = true
                    }
                }

                override fun onLost(network: Network) {
                    isNetworkConnection = false
                }
            }
            return connectivityCallback
        } else {
            throw IllegalAccessError("Accessing wrong API version")
        }
    }

    /**
     * Network or Cellular State
     */
    @Suppress("DEPRECATION")
    private fun isNetworkAvailableFunc(): Boolean {
        DLogger.d("isNetworkAvailable ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = manager.activeNetwork ?: return false
            val actNw = manager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return manager.activeNetworkInfo?.isConnected ?: false
        }
    }

    override fun isNetworkAvailable() = isNetworkConnection
}
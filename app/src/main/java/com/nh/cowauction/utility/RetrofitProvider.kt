package com.nh.cowauction.utility

import com.nh.cowauction.contants.Config
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Description : Retrofit Http Provider Class
 *
 * Created by hmju on 2021-05-18
 */
interface RetrofitProvider {
    fun createClient(): OkHttpClient
}

class RetrofitProviderImpl : RetrofitProvider {

    /**
     * 헤더관련 Interceptor 함수.
     */
    private fun headerInterceptor() = Interceptor { chain ->
        val origin = chain.request()
        chain.proceed(origin.newBuilder().apply {
            header("accept", "application/json")
            header("Content-Type", "application/json")
            method(origin.method, origin.body)
        }.build())
    }

    override fun createClient() = OkHttpClient.Builder().apply {
        retryOnConnectionFailure(false)
        connectTimeout(15, TimeUnit.SECONDS)
        readTimeout(15, TimeUnit.SECONDS)
        writeTimeout(15, TimeUnit.SECONDS)
        connectionPool(ConnectionPool(5, 1, TimeUnit.SECONDS))
        addInterceptor(headerInterceptor())
        if (Config.IS_DEBUG) {
            addInterceptor(RetrofitLogger())
        }
    }.build()
}
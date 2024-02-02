package com.nh.cowauction.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nh.cowauction.contants.Config
import com.nh.cowauction.repository.http.ApiService
import com.nh.cowauction.utility.RetrofitProvider
import com.nh.cowauction.utility.RetrofitProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Singleton
    @Provides
    fun provideHttpClient(): RetrofitProvider = RetrofitProviderImpl()

    @ExperimentalSerializationApi
    @Singleton
    @Provides
    fun provideApiService(retrofitProvider: RetrofitProvider): ApiService =
        Retrofit.Builder().apply {
            baseUrl(Config.BASE_DOMAIN)
            client(retrofitProvider.createClient())
            addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            addConverterFactory(Json {
                isLenient = true // Json 큰따옴표 느슨하게 체크.
                ignoreUnknownKeys = true // Field 값이 없는 경우 무시
                coerceInputValues = true // "null" 이 들어간경우 default Argument 값으로 대체
            }.asConverterFactory("application/json".toMediaType()))
        }.build().create(ApiService::class.java)
}
package com.nh.cowauction.di

import com.nh.cowauction.repository.preferences.BasePref
import com.nh.cowauction.repository.preferences.BasePrefImpl
import com.nh.cowauction.repository.tcp.NettyClient
import com.nh.cowauction.repository.tcp.NettyClientImpl
import com.nh.cowauction.repository.tcp.login.LoginManager
import com.nh.cowauction.repository.tcp.login.LoginManagerImpl
import com.nh.cowauction.utility.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AppModule {

    @Singleton
    @Binds
    abstract fun bindBasePref(basePref: BasePrefImpl): BasePref

    @Binds
    abstract fun bindResourceProvider(resourceProvider: ResourceProviderImpl): ResourceProvider

    @Binds
    abstract fun bindDeviceProvider(deviceProvider: DeviceProviderImpl): DeviceProvider

    @Binds
    abstract fun bindTextToSpeechProvider(ttsProvider: TextToSpeechProviderImpl): TextToSpeechProvider


    @Singleton
    @Binds
    abstract fun bindAgoraLiveProvider(agoraLiveProvider: AgoraLiveProviderImpl): AgoraLiveProvider

    @Singleton
    @Binds
    abstract fun bindLoginManager(loginManager: LoginManagerImpl): LoginManager

    @Singleton
    @Binds
    abstract fun bindNettyClient(nettyClient: NettyClientImpl): NettyClient

    @Singleton
    @Binds
    abstract fun bindNetworkConnection(networkConnectionProvider: NetworkConnectionProviderImpl): NetworkConnectionProvider
}
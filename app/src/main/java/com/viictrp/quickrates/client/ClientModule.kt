package com.viictrp.quickrates.client

import com.viictrp.quickrates.client.impl.CurrencyClientImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ClientModule {

    @Provides
    fun provideCurrencyClient(): CurrencyClient {
        return CurrencyClientImpl()
    }
}
package com.viictrp.quickrates.client

import com.viictrp.quickrates.client.dto.CurrencyDTO

interface CurrencyClient {

    suspend fun fetchCurrency(): CurrencyDTO?
}
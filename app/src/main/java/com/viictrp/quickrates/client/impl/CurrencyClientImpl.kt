package com.viictrp.quickrates.client.impl

import android.util.Log
import com.viictrp.quickrates.client.CurrencyClient
import com.viictrp.quickrates.client.dto.CurrencyDTO
import com.viictrp.quickrates.client.dto.CurrencyResponse
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class CurrencyClientImpl : CurrencyClient {
    private var client: OkHttpClient = OkHttpClient()

    override suspend fun fetchCurrency(): CurrencyDTO? {
        return try {
            val request = Request.Builder()
                .url("https://economia.awesomeapi.com.br/json/last/USD-BRLPTAX?token=token")
                .build()

            val currencyResponse = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.i("CurrencyClient", "Request failed with code: ${response.code}")
                    return@use null
                }

                val json = response.body?.string() ?: return@use null
                Json.decodeFromString<CurrencyResponse>(json)
            }

            currencyResponse?.usdBrl

        } catch (error: Exception) {
            Log.e("CurrencyClient", "Error fetching currency: ${error.message}")
            null
        }
    }
}
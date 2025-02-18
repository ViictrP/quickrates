package com.viictrp.quickrates.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyDTO(
    val code: String,
    val codein: String,
    val name: String,
    val high: String,
    val low: String,
    val varBid: String,
    val pctChange: String,
    val bid: String,
    val ask: String,
    val timestamp: String,
    @SerialName("create_date") val createDate: String
)

@Serializable
data class CurrencyResponse(
    @SerialName("USDBRL") val usdBrl: CurrencyDTO
)
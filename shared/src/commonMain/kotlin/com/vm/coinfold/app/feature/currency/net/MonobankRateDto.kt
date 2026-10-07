package com.vm.coinfold.app.feature.currency.net

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

/**
 * One item of `GET /bank/currency`. Rates are kept as [JsonPrimitive] so they can be parsed
 * into BigDecimal from the exact JSON text without going through Double.
 */
@Serializable
data class MonobankRateDto(
    val currencyCodeA: Int,
    val currencyCodeB: Int,
    val date: Long = 0,
    val rateBuy: JsonPrimitive? = null,
    val rateSell: JsonPrimitive? = null,
    val rateCross: JsonPrimitive? = null,
)

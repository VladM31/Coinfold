package com.vm.coinfold.app.feature.currency.net

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.shared.domain.Currency
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.JsonPrimitive

class MonobankCurrencyClient(private val httpClient: HttpClient) : CurrencyClient {

    override suspend fun getRates(): List<Rate> =
        httpClient.get(URL).body<List<MonobankRateDto>>().mapNotNull { it.toRate() }

    private companion object {
        const val URL = "https://api.monobank.ua/bank/currency"
    }
}

/** Uses the middle of buy/sell when both exist, otherwise the cross rate. Unknown currencies are skipped. */
internal fun MonobankRateDto.toRate(): Rate? {
    val from = Currency.entries.firstOrNull { it.isoNumeric == currencyCodeA } ?: return null
    val to = Currency.entries.firstOrNull { it.isoNumeric == currencyCodeB } ?: return null
    val buy = rateBuy.toDecimalOrNull()
    val sell = rateSell.toDecimalOrNull()
    val value = if (buy != null && sell != null) {
        (buy + sell).multiply(HALF)
    } else {
        rateCross.toDecimalOrNull()
    } ?: return null
    if (value.signum() <= 0) return null
    return Rate(from, to, value)
}

private val HALF = BigDecimal.parseString("0.5")

private fun JsonPrimitive?.toDecimalOrNull(): BigDecimal? =
    this?.content?.let { runCatching { BigDecimal.parseString(it) }.getOrNull() }

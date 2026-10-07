package com.vm.coinfold.app.feature.currency.net.clients.impls

import com.vm.coinfold.app.feature.currency.net.clients.CurrencyClient
import com.vm.coinfold.app.feature.currency.net.dtos.MonobankRateDto
import com.vm.coinfold.app.feature.currency.net.mappers.toRate
import com.vm.coinfold.app.feature.currency.net.models.Rate
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class MonobankCurrencyClient(private val httpClient: HttpClient) : CurrencyClient {

    override suspend fun getRates(): List<Rate> =
        httpClient.get(URL).body<List<MonobankRateDto>>().mapNotNull { it.toRate() }

    private companion object {
        const val URL = "https://api.monobank.ua/bank/currency"
    }
}

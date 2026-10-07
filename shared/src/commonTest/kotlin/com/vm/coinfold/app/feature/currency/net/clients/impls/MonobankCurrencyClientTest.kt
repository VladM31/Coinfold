package com.vm.coinfold.app.feature.currency.net.clients.impls

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.shared.domain.models.Currency
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class MonobankCurrencyClientTest {
    private val body = """
        [
          {"currencyCodeA":840,"currencyCodeB":980,"date":1,"rateBuy":41.0,"rateSell":41.5},
          {"currencyCodeA":978,"currencyCodeB":980,"date":1,"rateBuy":44.0,"rateSell":45.0},
          {"currencyCodeA":978,"currencyCodeB":840,"date":1,"rateCross":1.08},
          {"currencyCodeA":826,"currencyCodeB":980,"date":1,"rateBuy":50.0,"rateSell":51.0}
        ]
    """.trimIndent()

    @Test
    fun parsesAndMapsRates() = runTest {
        val engine = MockEngine {
            respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val rates = MonobankCurrencyClient(client).getRates()

        // GBP is not supported and is skipped
        assertEquals(3, rates.size)
        val usd = rates.first { it.from == Currency.USD && it.to == Currency.UAH }
        assertEquals(BigDecimal.parseString("41.25"), usd.rate)
        val cross = rates.first { it.from == Currency.EUR && it.to == Currency.USD }
        assertEquals(BigDecimal.parseString("1.08"), cross.rate)
    }
}

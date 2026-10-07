package com.vm.coinfold.app.feature.currency.net.clients

import com.vm.coinfold.app.feature.currency.net.models.Rate

/** Source of exchange rates; swap the implementation (and the Koin binding) to change provider. */
interface CurrencyClient {
    suspend fun getRates(): List<Rate>
}

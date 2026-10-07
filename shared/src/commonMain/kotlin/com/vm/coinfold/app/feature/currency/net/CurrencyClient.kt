package com.vm.coinfold.app.feature.currency.net

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.shared.domain.Currency

/** 1 unit of [from] costs [rate] units of [to]. */
data class Rate(val from: Currency, val to: Currency, val rate: BigDecimal)

/** Source of exchange rates; swap the implementation (and the Koin binding) to change provider. */
interface CurrencyClient {
    suspend fun getRates(): List<Rate>
}

package com.vm.coinfold.app.feature.currency.main

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.vm.coinfold.app.shared.domain.Currency
import com.vm.coinfold.app.shared.domain.Money

/** Result of a conversion: the converted amount and the rate that was applied (to store in the transaction). */
data class Conversion(val money: Money, val rate: BigDecimal)

/** Immutable snapshot of known direct rates, keyed by (from, to). */
class RateTable(private val direct: Map<Pair<Currency, Currency>, BigDecimal>) {

    /** Rate for 1 [from] in [to]: direct, inverse, or through UAH; null if unknown. */
    fun find(from: Currency, to: Currency): BigDecimal? {
        if (from == to) return BigDecimal.ONE
        directOrInverse(from, to)?.let { return it }
        if (from != Currency.UAH && to != Currency.UAH) {
            val toUah = directOrInverse(from, Currency.UAH) ?: return null
            val fromUah = directOrInverse(Currency.UAH, to) ?: return null
            return toUah.multiply(fromUah)
        }
        return null
    }

    fun convert(money: Money, target: Currency): Conversion? {
        val rate = find(money.currency, target) ?: return null
        return Conversion(money.convertTo(target, rate), rate)
    }

    private fun directOrInverse(from: Currency, to: Currency): BigDecimal? {
        direct[from to to]?.let { return it }
        val inverse = direct[to to from] ?: return null
        return BigDecimal.ONE.divide(inverse, INVERSE_MODE)
    }

    companion object {
        val Empty = RateTable(emptyMap())
        private val INVERSE_MODE = DecimalMode(16, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO, 10)
    }
}

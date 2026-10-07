package com.vm.coinfold.app.feature.currency.domain.models

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RateTableTest {
    private fun dec(s: String) = BigDecimal.parseString(s)

    private val table = RateTable(
        mapOf(
            (Currency.USD to Currency.UAH) to dec("40"),
            (Currency.EUR to Currency.UAH) to dec("44"),
        ),
    )

    @Test
    fun sameCurrencyIsOne() {
        assertEquals(BigDecimal.ONE, table.find(Currency.USD, Currency.USD))
    }

    @Test
    fun directAndInverse() {
        assertEquals(dec("40"), table.find(Currency.USD, Currency.UAH))
        assertEquals(dec("0.025"), table.find(Currency.UAH, Currency.USD)?.roundSignificand())
    }

    @Test
    fun crossViaUah() {
        // 1 USD = 40 UAH, 1 EUR = 44 UAH -> 1 USD = 40 * (1/44) EUR
        val conversion = assertNotNull(table.convert(Money(4400, Currency.USD), Currency.EUR))
        assertEquals(Money(4000, Currency.EUR), conversion.money)
    }

    @Test
    fun unknownRateIsNull() {
        assertNull(RateTable.Empty.find(Currency.USD, Currency.UAH))
        assertNull(RateTable.Empty.convert(Money(100, Currency.USD), Currency.EUR))
    }

    // Inverse rates carry 10 decimals; trim for readable equality.
    private fun BigDecimal.roundSignificand(): BigDecimal =
        roundToDigitPositionAfterDecimalPoint(6, com.ionspin.kotlin.bignum.decimal.RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
            .let { BigDecimal.parseString(it.toPlainString().trimEnd('0')) }
}

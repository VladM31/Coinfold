package com.vm.coinfold.app.shared.domain.models

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {
    @Test
    fun plusAndMinusSameCurrency() {
        val a = Money(1000, Currency.UAH)
        val b = Money(250, Currency.UAH)
        assertEquals(Money(1250, Currency.UAH), a + b)
        assertEquals(Money(750, Currency.UAH), a - b)
    }

    @Test
    fun plusDifferentCurrencyFails() {
        assertFailsWith<IllegalArgumentException> { Money(1, Currency.UAH) + Money(1, Currency.USD) }
    }

    @Test
    fun convertRoundsHalfUp() {
        // 10.00 USD * 41.255 = 412.55 UAH
        val uah = Money(1000, Currency.USD).convertTo(Currency.UAH, BigDecimal.parseString("41.255"))
        assertEquals(Money(41255, Currency.UAH), uah)
        // 0.01 USD * 41.5 = 0.415 -> 0.42 UAH
        val tiny = Money(1, Currency.USD).convertTo(Currency.UAH, BigDecimal.parseString("41.5"))
        assertEquals(Money(42, Currency.UAH), tiny)
    }

    @Test
    fun ofRoundsToMinorUnits() {
        assertEquals(Money(1235, Currency.EUR), Money.of(BigDecimal.parseString("12.345"), Currency.EUR))
    }
}

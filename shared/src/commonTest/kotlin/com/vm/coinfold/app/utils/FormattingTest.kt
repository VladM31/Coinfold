package com.vm.coinfold.app.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.ResolvedLanguage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class FormattingTest {
    @Test
    fun formatsUkrainian() {
        assertEquals("1 234,56 ₴", Money(123456, Currency.UAH).format(ResolvedLanguage.UK))
        assertEquals("-0,05 ₴", Money(-5, Currency.UAH).format(ResolvedLanguage.UK))
    }

    @Test
    fun formatsEnglish() {
        assertEquals("$1,234,567.80", Money(123456780, Currency.USD).format(ResolvedLanguage.EN))
        assertEquals("-€10.00", Money(-1000, Currency.EUR).format(ResolvedLanguage.EN))
        assertEquals("+€0.10", Money(10, Currency.EUR).format(ResolvedLanguage.EN, showPlus = true))
    }

    @Test
    fun parsesAmounts() {
        assertEquals(BigDecimal.parseString("1234.5"), parseAmount("1 234,5"))
        assertEquals(BigDecimal.parseString("12.34"), parseAmount("12.34"))
        assertNull(parseAmount("12.345"))
        assertNull(parseAmount("abc"))
        assertNull(parseAmount(""))
        assertNull(parseAmount("-5"))
        assertEquals(BigDecimal.parseString("-5"), parseAmount("-5", allowNegative = true))
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun todayKeepsCurrentMomentOtherDaysUseNoon() {
        val tz = TimeZone.UTC
        val now = Instant.parse("2026-03-10T15:30:00Z")
        assertEquals(now.toEpochMilliseconds(), epochMillisFor(LocalDate(2026, 3, 10), tz, now))
        assertEquals(
            Instant.parse("2026-03-05T12:00:00Z").toEpochMilliseconds(),
            epochMillisFor(LocalDate(2026, 3, 5), tz, now),
        )
    }

    @Test
    fun formatsDate() {
        assertEquals("05.03.2026", LocalDate(2026, 3, 5).format())
    }

    @Test
    fun parsesRates() {
        assertEquals(BigDecimal.parseString("41.2534"), parseRate("41,2534"))
        assertEquals(BigDecimal.parseString("0.025"), parseRate("0.025"))
        assertNull(parseRate("0"))
        assertNull(parseRate("-3"))
        assertNull(parseRate("1.1234567")) // more than 6 decimals
        assertNull(parseRate("abc"))
        assertNull(parseRate(""))
    }
}

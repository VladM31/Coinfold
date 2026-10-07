package com.vm.coinfold.app.feature.expenses.domain.services

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.shared.domain.models.Period
import com.vm.coinfold.app.feature.expenses.domain.models.PeriodTotal
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class ExpensesLogicTest {

    @Test
    fun calendarMonthPeriod() {
        val p = Period.containing(LocalDate(2026, 3, 15), startDay = 1)
        assertEquals(LocalDate(2026, 3, 1), p.start)
        assertEquals(LocalDate(2026, 4, 1), p.endExclusive)
        assertEquals(LocalDate(2026, 3, 31), p.endInclusive)
    }

    @Test
    fun customStartDayPeriod() {
        // before the 7th we are still in the period that started on the 7th of the previous month
        val early = Period.containing(LocalDate(2026, 3, 3), startDay = 7)
        assertEquals(LocalDate(2026, 2, 7), early.start)
        assertEquals(LocalDate(2026, 3, 7), early.endExclusive)
        assertEquals(LocalDate(2026, 3, 6), early.endInclusive)

        val late = Period.containing(LocalDate(2026, 3, 7), startDay = 7)
        assertEquals(LocalDate(2026, 3, 7), late.start)
    }

    @Test
    fun navigationAcrossYearBoundary() {
        val p = Period.containing(LocalDate(2026, 1, 10), startDay = 7)
        assertEquals(LocalDate(2025, 12, 7), p.shiftMonths(-1).start)
        assertEquals(LocalDate(2026, 2, 7), p.shiftMonths(1).start)
    }

    private val food = Category(1, "Food", 0xFF00FF00, "x")
    private val rates = RateTable(mapOf((Currency.USD to Currency.UAH) to BigDecimal.parseString("40")))

    @Test
    fun summaryAggregatesAndConverts() {
        val totals = listOf(
            PeriodTotal(TransactionType.INCOME, null, Money(100_000, Currency.UAH)),
            PeriodTotal(TransactionType.EXPENSE, 1, Money(20_000, Currency.UAH)),
            PeriodTotal(TransactionType.EXPENSE, 1, Money(100, Currency.USD)), // 40 UAH
            PeriodTotal(TransactionType.EXPENSE, null, Money(1_000, Currency.UAH)), // manual withdrawal
            PeriodTotal(TransactionType.EXPENSE, 99, Money(500, Currency.UAH)), // archived category
        )
        val s = calculateSummary(totals, listOf(food), rates, Currency.UAH)
        assertEquals(Money(24_000, Currency.UAH), s.perCategory.single().spent)
        assertEquals(Money(1_500, Currency.UAH), s.other)
        assertEquals(Money(25_500, Currency.UAH), s.spent)
        assertEquals(Money(74_500, Currency.UAH), s.remaining)
    }

    @Test
    fun missingRateIsFlaggedAndSkipped() {
        val totals = listOf(PeriodTotal(TransactionType.EXPENSE, 1, Money(100, Currency.EUR)))
        val s = calculateSummary(totals, listOf(food), rates, Currency.UAH)
        assertTrue(s.hasMissingRates)
        assertEquals(Money(0, Currency.UAH), s.spent)
    }
}

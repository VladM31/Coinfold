package com.vm.coinfold.app.feature.overview.domain.services

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.overview.domain.models.OverviewTransaction
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.Period
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalTime::class)
class OverviewCalculatorTest {
    private val food = Category(1, "Food", 0xFF00FF00, "icon:cart")
    private val fun_ = Category(2, "Fun", 0xFF0000FF, "icon:movie")
    private val rates = RateTable(mapOf((Currency.USD to Currency.UAH) to BigDecimal.parseString("40")))
    private val period = Period.containing(LocalDate(2026, 3, 10), 1) // March, 31 days

    private fun tx(type: TransactionType, category: Long?, money: Money, day: Int) = OverviewTransaction(
        type, category, money,
        Instant.parse("2026-03-${day.toString().padStart(2, '0')}T12:00:00Z").toEpochMilliseconds(),
    )

    private fun calc(txs: List<OverviewTransaction>, today: LocalDate = LocalDate(2026, 3, 10)) =
        calculateOverview(txs, listOf(food, fun_), rates, Currency.UAH, period, today, TimeZone.UTC)

    @Test
    fun totalsSharesAndBars() {
        val summary = calc(
            listOf(
                tx(TransactionType.INCOME, null, Money(100_000, Currency.UAH), 1),
                tx(TransactionType.EXPENSE, 1, Money(30_000, Currency.UAH), 2),
                tx(TransactionType.EXPENSE, 1, Money(100, Currency.USD), 2), // 40.00 UAH
                tx(TransactionType.EXPENSE, 2, Money(10_000, Currency.UAH), 5),
                tx(TransactionType.EXPENSE, null, Money(10_000, Currency.UAH), 5), // manual withdrawal
            ),
        )

        assertEquals(Money(100_000, Currency.UAH), summary.income)
        assertEquals(Money(54_000, Currency.UAH), summary.spent)
        assertEquals(Money(46_000, Currency.UAH), summary.balance)

        assertEquals(31, summary.bars.size)
        assertEquals(Money(34_000, Currency.UAH), summary.bars[1].total) // 2 March
        assertEquals(listOf(food.color), summary.bars[1].segments.map { it.color })
        assertEquals(listOf(fun_.color, null), summary.bars[4].segments.map { it.color }) // 5 March

        // sorted by spending; 34000/54000 = 63%, the other two 18.5% round to 19%
        assertEquals(food, summary.shares[0].category)
        assertEquals(63, summary.shares[0].percent)
        assertEquals(3, summary.shares.size)
        assertEquals(Money(34_000, Currency.UAH), summary.maxBar)
    }

    @Test
    fun averagesUseElapsedDaysOfCurrentPeriod() {
        val summary = calc(listOf(tx(TransactionType.EXPENSE, 1, Money(100_000, Currency.UAH), 3)), today = LocalDate(2026, 3, 10))
        // 10 days passed: 1000.00 / 10 = 100.00 per day
        assertEquals(Money(10_000, Currency.UAH), summary.dayAverage)
        assertEquals(Money(70_000, Currency.UAH), summary.weekAverage)
        assertEquals(Money(310_000, Currency.UAH), summary.monthAverage)

        // a finished period is divided by its full length (31 days)
        val past = calc(listOf(tx(TransactionType.EXPENSE, 1, Money(31_000, Currency.UAH), 3)), today = LocalDate(2026, 5, 1))
        assertEquals(Money(1_000, Currency.UAH), past.dayAverage)
    }

    @Test
    fun missingRateIsFlagged() {
        val summary = calc(listOf(tx(TransactionType.EXPENSE, 1, Money(100, Currency.EUR), 3)))
        assertTrue(summary.hasMissingRates)
        assertEquals(Money(0, Currency.UAH), summary.spent)
    }
}

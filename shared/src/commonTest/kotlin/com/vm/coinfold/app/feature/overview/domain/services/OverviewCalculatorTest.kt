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
    fun dayAverageUsesElapsedDays() {
        // 10 days passed: 1000.00 / 10 days = 100.00 per day
        val summary = calc(listOf(tx(TransactionType.EXPENSE, 1, Money(100_000, Currency.UAH), 3)), today = LocalDate(2026, 3, 10))
        assertEquals(Money(10_000, Currency.UAH), summary.dayAverage)

        // a finished period is divided by its full length (31 days)
        val past = calc(listOf(tx(TransactionType.EXPENSE, 1, Money(31_000, Currency.UAH), 3)), today = LocalDate(2026, 5, 1))
        assertEquals(Money(1_000, Currency.UAH), past.dayAverage)
    }

    @Test
    fun weekAverageIsActualSpendingPerStartedWeek() {
        val expenses = listOf(tx(TransactionType.EXPENSE, 1, Money(78_000, Currency.UAH), 1))
        // 2 days passed: one started week, so the average is simply what was spent, not 7 x the daily rate
        assertEquals(Money(78_000, Currency.UAH), calc(expenses, today = LocalDate(2026, 3, 2)).weekAverage)
        // 7 days: still one week
        assertEquals(Money(78_000, Currency.UAH), calc(expenses, today = LocalDate(2026, 3, 7)).weekAverage)
        // 8..14 days: two started weeks
        assertEquals(Money(39_000, Currency.UAH), calc(expenses, today = LocalDate(2026, 3, 10)).weekAverage)
    }

    @Test
    fun monthAverageIsTheMeanOfAllPeriodsWithExpenses() {
        fun at(month: Int, amount: Long) = OverviewTransaction(
            TransactionType.EXPENSE, 1, Money(amount, Currency.UAH),
            Instant.parse("2026-%02d-10T12:00:00Z".format(month)).toEpochMilliseconds(),
        )
        val history = listOf(at(2, 100_000), at(3, 200_000), at(3, 100_000))
        val summary = calculateOverview(
            transactions = history.filter { it.dateTime >= Instant.parse("2026-03-01T00:00:00Z").toEpochMilliseconds() },
            categories = listOf(food), rates = rates, mainCurrency = Currency.UAH, period = period,
            today = LocalDate(2026, 3, 10), timeZone = TimeZone.UTC, history = history, periodStartDay = 1,
        )
        // February 1000.00 and March 3000.00 -> 2000.00 per month, nothing extrapolated
        assertEquals(Money(200_000, Currency.UAH), summary.monthAverage)
    }

    @Test
    fun monthAverageWithOnlyThePeriodEqualsItsSpending() {
        val summary = calc(listOf(tx(TransactionType.EXPENSE, 1, Money(78_000, Currency.UAH), 1)), today = LocalDate(2026, 3, 2))
        assertEquals(Money(78_000, Currency.UAH), summary.monthAverage)
    }

    @Test
    fun missingRateIsFlagged() {
        val summary = calc(listOf(tx(TransactionType.EXPENSE, 1, Money(100, Currency.EUR), 3)))
        assertTrue(summary.hasMissingRates)
        assertEquals(Money(0, Currency.UAH), summary.spent)
    }

    // ---------- comparison with the previous period and the trend ----------

    private fun inFeb(day: Int, amount: Long, type: TransactionType = TransactionType.EXPENSE, category: Long? = 1) =
        OverviewTransaction(
            type, category, Money(amount, Currency.UAH),
            Instant.parse("2026-02-%02dT12:00:00Z".format(day)).toEpochMilliseconds(),
        )

    private fun compare(
        current: List<OverviewTransaction>,
        previous: List<OverviewTransaction>,
        today: LocalDate,
    ) = calculateOverview(
        transactions = current, categories = listOf(food, fun_), rates = rates, mainCurrency = Currency.UAH,
        period = period, today = today, timeZone = TimeZone.UTC, previousTransactions = previous,
    )

    @Test
    fun runningPeriodIsComparedWithTheSameDaysOfThePreviousOne() {
        val summary = compare(
            current = listOf(tx(TransactionType.EXPENSE, 1, Money(100_000, Currency.UAH), 3)),
            previous = listOf(inFeb(5, 50_000), inFeb(20, 900_000)), // the 20th is outside the first 10 days
            today = LocalDate(2026, 3, 10),
        )
        val c = summary.comparison
        assertEquals(Money(50_000, Currency.UAH), c.previousSpent)
        assertEquals(100, c.spentChangePercent) // 1000 vs 500
        assertTrue(c.sameDaysOnly)
    }

    @Test
    fun finishedPeriodIsComparedWithTheWholePreviousOne() {
        val summary = compare(
            current = listOf(tx(TransactionType.EXPENSE, 1, Money(100_000, Currency.UAH), 3)),
            previous = listOf(inFeb(5, 50_000), inFeb(20, 900_000)),
            today = LocalDate(2026, 5, 1),
        )
        assertEquals(Money(950_000, Currency.UAH), summary.comparison.previousSpent)
        assertEquals(-89, summary.comparison.spentChangePercent) // 1000 vs 9500 -> -89.5% rounded away from zero
        assertEquals(false, summary.comparison.sameDaysOnly)
    }

    @Test
    fun noPreviousSpendingMeansNoPercentage() {
        val summary = compare(
            current = listOf(tx(TransactionType.EXPENSE, 1, Money(100_000, Currency.UAH), 3)),
            previous = emptyList(),
            today = LocalDate(2026, 3, 10),
        )
        assertEquals(null, summary.comparison.spentChangePercent)
        assertEquals(null, summary.shares.single().changePercent)
    }

    @Test
    fun categoriesGetTheirOwnChange() {
        val summary = compare(
            current = listOf(
                tx(TransactionType.EXPENSE, 1, Money(150_000, Currency.UAH), 3),
                tx(TransactionType.EXPENSE, 2, Money(20_000, Currency.UAH), 4),
            ),
            previous = listOf(inFeb(2, 100_000, category = 1), inFeb(3, 40_000, category = 2)),
            today = LocalDate(2026, 3, 10),
        )
        assertEquals(50, summary.shares.first { it.category == food }.changePercent) // 1500 vs 1000
        assertEquals(-50, summary.shares.first { it.category == fun_ }.changePercent) // 200 vs 400
    }

    @Test
    fun incomeIsComparedToo() {
        val summary = compare(
            current = listOf(tx(TransactionType.INCOME, null, Money(300_000, Currency.UAH), 2)),
            previous = listOf(inFeb(2, 200_000, TransactionType.INCOME, null)),
            today = LocalDate(2026, 3, 10),
        )
        assertEquals(50, summary.comparison.incomeChangePercent)
    }

    @Test
    fun trendHasTwelvePeriodsEndingWithTheCurrentOne() {
        val history = listOf(
            inFeb(5, 100_000),
            inFeb(6, 300_000, TransactionType.INCOME, null),
            tx(TransactionType.EXPENSE, 1, Money(200_000, Currency.UAH), 3),
        )
        val summary = calculateOverview(
            transactions = history.filter { it.dateTime >= Instant.parse("2026-03-01T00:00:00Z").toEpochMilliseconds() },
            categories = listOf(food), rates = rates, mainCurrency = Currency.UAH, period = period,
            today = LocalDate(2026, 3, 10), timeZone = TimeZone.UTC, history = history, periodStartDay = 1,
        )

        assertEquals(12, summary.trend.size)
        assertEquals(LocalDate(2026, 3, 1), summary.trend.last().periodStart)
        assertEquals(LocalDate(2025, 4, 1), summary.trend.first().periodStart)
        val feb = summary.trend[summary.trend.size - 2]
        assertEquals(Money(100_000, Currency.UAH), feb.spent)
        assertEquals(Money(300_000, Currency.UAH), feb.income)
        assertEquals(Money(200_000, Currency.UAH), summary.trend.last().spent)
        assertEquals(Money(0, Currency.UAH), summary.trend.first().spent)
    }
}

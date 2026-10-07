package com.vm.coinfold.app.feature.overview.domain.services

import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.overview.domain.models.BarSegment
import com.vm.coinfold.app.feature.overview.domain.models.CategoryShare
import com.vm.coinfold.app.feature.overview.domain.models.DayBar
import com.vm.coinfold.app.feature.overview.domain.models.OverviewSummary
import com.vm.coinfold.app.feature.overview.domain.models.OverviewTransaction
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.Period
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** Integer division rounding half up for non-negative values. */
private fun divideRounded(value: Long, divisor: Long): Long = (value + divisor / 2) / divisor

/**
 * Builds the statistics of one [period] in [mainCurrency]: income, spending, spending per day split by
 * category, the share of each category and average spending per day/week/month.
 *
 * Averages divide by the days that have already passed in the period ([today] decides), so the current
 * period is not diluted by days that have not happened yet.
 */
@OptIn(ExperimentalTime::class)
fun calculateOverview(
    transactions: List<OverviewTransaction>,
    categories: List<Category>,
    rates: RateTable,
    mainCurrency: Currency,
    period: Period,
    today: LocalDate,
    timeZone: TimeZone,
): OverviewSummary {
    val zero = Money.zero(mainCurrency)
    val activeIds = categories.mapTo(HashSet()) { it.id }
    var income = zero
    var missing = false
    // day -> category key (null = other) -> spent
    val perDay = HashMap<LocalDate, HashMap<Long?, Money>>()
    val perCategory = HashMap<Long?, Money>()

    for (tx in transactions) {
        val converted = rates.convert(tx.money, mainCurrency)?.money
        if (converted == null) {
            missing = true
            continue
        }
        if (tx.type == TransactionType.INCOME) {
            income += converted
            continue
        }
        val key = tx.categoryId?.takeIf { it in activeIds }
        val date = Instant.fromEpochMilliseconds(tx.dateTime).toLocalDateTime(timeZone).date
        val day = perDay.getOrPut(date) { HashMap() }
        day[key] = (day[key] ?: zero) + converted
        perCategory[key] = (perCategory[key] ?: zero) + converted
    }

    val spent = perCategory.values.fold(zero) { acc, m -> acc + m }

    val lengthDays = period.start.daysUntil(period.endExclusive)
    val bars = (0 until lengthDays).map { offset ->
        val date = period.start.plus(offset, DateTimeUnit.DAY)
        val byCategory = perDay[date].orEmpty()
        // Stack in category order so colors line up between days; "other" goes on top.
        val segments = categories.mapNotNull { c ->
            byCategory[c.id]?.let { BarSegment(c.color, it) }
        } + listOfNotNull(byCategory[null]?.let { BarSegment(null, it) })
        DayBar(date, segments, segments.fold(zero) { acc, s -> acc + s.amount })
    }

    val shares = buildList {
        categories.forEach { c ->
            val amount = perCategory[c.id] ?: return@forEach
            add(CategoryShare(c, amount, percentOf(amount, spent)))
        }
        perCategory[null]?.let { add(CategoryShare(null, it, percentOf(it, spent))) }
    }.sortedByDescending { it.spent.minorUnits }

    val elapsedDays = when {
        today < period.start -> 1
        today >= period.endExclusive -> lengthDays
        else -> period.start.daysUntil(today) + 1
    }.coerceAtLeast(1)
    val dayAverage = Money(divideRounded(spent.minorUnits, elapsedDays.toLong()), mainCurrency)
    val weekAverage = Money(divideRounded(spent.minorUnits * 7, elapsedDays.toLong()), mainCurrency)
    val monthAverage = Money(divideRounded(spent.minorUnits * lengthDays, elapsedDays.toLong()), mainCurrency)

    return OverviewSummary(
        mainCurrency = mainCurrency,
        income = income,
        spent = spent,
        bars = bars,
        shares = shares,
        dayAverage = dayAverage,
        weekAverage = weekAverage,
        monthAverage = monthAverage,
        hasMissingRates = missing,
    )
}

private fun percentOf(part: Money, total: Money): Int =
    if (total.minorUnits <= 0) 0 else divideRounded(part.minorUnits * 100, total.minorUnits).toInt()

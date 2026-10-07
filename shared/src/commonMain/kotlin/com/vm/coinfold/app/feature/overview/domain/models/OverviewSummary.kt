package com.vm.coinfold.app.feature.overview.domain.models

import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlinx.datetime.LocalDate

/** A transaction reduced to what the overview needs; [money] is in the currency it was entered in. */
data class OverviewTransaction(
    val type: TransactionType,
    val categoryId: Long?,
    val money: Money,
    val dateTime: Long,
)

/** One colored part of a day bar; a null [color] is spending without an active category. */
data class BarSegment(val color: Long?, val amount: Money)

data class DayBar(val date: LocalDate, val segments: List<BarSegment>, val total: Money)

/**
 * A category (null = "other") with its spending and share of all spending in whole percent.
 * [changePercent] is the change against the previous period (positive = more spending); null if there was
 * nothing to compare with.
 */
data class CategoryShare(
    val category: Category?,
    val spent: Money,
    val percent: Int,
    val changePercent: Int? = null,
)

/**
 * This period against the previous one. While the period is still running only the same number of days of the
 * previous period are compared ([sameDaysOnly]), so a half-finished month is not measured against a whole one.
 * Percentages are null when the previous period had nothing to compare with.
 */
data class PeriodComparison(
    val previousSpent: Money,
    val previousIncome: Money,
    val spentChangePercent: Int?,
    val incomeChangePercent: Int?,
    val sameDaysOnly: Boolean,
)

/** Income and spending of one calculation period, for the trend chart. */
data class MonthlyPoint(val periodStart: LocalDate, val income: Money, val spent: Money)

data class OverviewSummary(
    val mainCurrency: Currency,
    val income: Money,
    val spent: Money,
    val bars: List<DayBar>,
    val shares: List<CategoryShare>,
    val dayAverage: Money,
    /** Spending so far per started week of the period. */
    val weekAverage: Money,
    /** Average spending per period over all periods that have expenses. */
    val monthAverage: Money,
    val comparison: PeriodComparison,
    /** The last periods ending with this one, oldest first. */
    val trend: List<MonthlyPoint>,
    /** True when some amounts could not be converted because no exchange rate is known. */
    val hasMissingRates: Boolean,
) {
    val balance: Money get() = income - spent
    val maxBar: Money get() = bars.maxByOrNull { it.total.minorUnits }?.total ?: Money.zero(mainCurrency)
}

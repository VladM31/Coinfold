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

/** A category (null = "other") with its spending and share of all spending in whole percent. */
data class CategoryShare(val category: Category?, val spent: Money, val percent: Int)

data class OverviewSummary(
    val mainCurrency: Currency,
    val income: Money,
    val spent: Money,
    val bars: List<DayBar>,
    val shares: List<CategoryShare>,
    val dayAverage: Money,
    val weekAverage: Money,
    val monthAverage: Money,
    /** True when some amounts could not be converted because no exchange rate is known. */
    val hasMissingRates: Boolean,
) {
    val balance: Money get() = income - spent
    val maxBar: Money get() = bars.maxByOrNull { it.total.minorUnits }?.total ?: Money.zero(mainCurrency)
}

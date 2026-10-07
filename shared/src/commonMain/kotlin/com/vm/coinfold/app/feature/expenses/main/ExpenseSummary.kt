package com.vm.coinfold.app.feature.expenses.main

import com.vm.coinfold.app.feature.currency.main.RateTable
import com.vm.coinfold.app.shared.domain.Currency
import com.vm.coinfold.app.shared.domain.Money
import com.vm.coinfold.app.shared.domain.TransactionType

/** Total of one currency for a transaction type/category in the period (amounts as entered). */
data class PeriodTotal(val type: TransactionType, val categoryId: Long?, val money: Money)

data class CategorySpend(val category: Category, val spent: Money)

/**
 * Everything in [mainCurrency]. [other] is spending without an active category
 * (manual withdrawals and archived categories). [remaining] can be negative.
 */
data class ExpenseSummary(
    val mainCurrency: Currency,
    val income: Money,
    val spent: Money,
    val perCategory: List<CategorySpend>,
    val other: Money,
    val hasMissingRates: Boolean,
) {
    val remaining: Money get() = income - spent
}

fun calculateSummary(
    totals: List<PeriodTotal>,
    categories: List<Category>,
    rates: RateTable,
    mainCurrency: Currency,
): ExpenseSummary {
    val zero = Money.zero(mainCurrency)
    var income = zero
    var other = zero
    val spentByCategory = HashMap<Long, Money>()
    var missing = false
    val activeIds = categories.mapTo(HashSet()) { it.id }

    for (total in totals) {
        val converted = rates.convert(total.money, mainCurrency)?.money
        if (converted == null) {
            missing = true
            continue
        }
        when {
            total.type == TransactionType.INCOME -> income += converted
            total.categoryId != null && total.categoryId in activeIds ->
                spentByCategory[total.categoryId] = (spentByCategory[total.categoryId] ?: zero) + converted
            else -> other += converted
        }
    }

    val perCategory = categories.map { CategorySpend(it, spentByCategory[it.id] ?: zero) }
    val spent = perCategory.fold(other) { acc, item -> acc + item.spent }
    return ExpenseSummary(mainCurrency, income, spent, perCategory, other, missing)
}

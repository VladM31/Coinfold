package com.vm.coinfold.app.feature.expenses.domain.services

import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.expenses.domain.models.CategorySpend
import com.vm.coinfold.app.feature.expenses.domain.models.ExpenseSummary
import com.vm.coinfold.app.feature.expenses.domain.models.PeriodTotal
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType

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

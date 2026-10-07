package com.vm.coinfold.app.feature.expenses.domain.models

import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType

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

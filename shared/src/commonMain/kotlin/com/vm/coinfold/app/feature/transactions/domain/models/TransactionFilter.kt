package com.vm.coinfold.app.feature.transactions.domain.models

import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlinx.datetime.LocalDate

sealed interface PeriodFilter {
    data object All : PeriodFilter

    /** The calculation period that contains today (depends on the period start day setting). */
    data object CurrentPeriod : PeriodFilter

    /** Both dates are inclusive. */
    data class Custom(val from: LocalDate, val to: LocalDate) : PeriodFilter
}

/** Null [accountId]/[categoryId]/[type] mean "no restriction". */
data class TransactionFilter(
    val period: PeriodFilter = PeriodFilter.All,
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val type: TransactionType? = null,
) {
    val isActive: Boolean
        get() = period != PeriodFilter.All || accountId != null || categoryId != null || type != null
}

/** Filter resolved to concrete bounds: [fromMillis] inclusive, [toMillis] exclusive. */
data class TransactionQuery(
    val fromMillis: Long,
    val toMillis: Long,
    val accountId: Long?,
    val categoryId: Long?,
    val type: TransactionType?,
)

package com.vm.coinfold.app.feature.transactions.domain.models

import com.vm.coinfold.app.shared.domain.models.Money
import kotlinx.datetime.LocalDate

/**
 * Transactions of one day. [total] is income minus expenses converted to the main currency;
 * [hasMissingRates] means some items could not be converted and are not part of it.
 */
data class DayGroup(
    val date: LocalDate,
    val items: List<TransactionItem>,
    val total: Money,
    val hasMissingRates: Boolean,
)

enum class UpdateTransactionResult { SUCCESS, NO_RATE, NOT_FOUND }

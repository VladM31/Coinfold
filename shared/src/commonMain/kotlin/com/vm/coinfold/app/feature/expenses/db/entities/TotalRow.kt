package com.vm.coinfold.app.feature.expenses.db.entities

import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.TransactionType

/** Sum of transaction amounts as entered, grouped so each row has a single currency. */
data class TotalRow(
    val type: TransactionType,
    val categoryId: Long?,
    val currency: Currency,
    val total: Long,
)

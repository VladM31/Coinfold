package com.vm.coinfold.app.feature.transactions.db.entities

import androidx.room.Embedded
import com.vm.coinfold.app.shared.domain.models.Currency

/** Transaction joined with the names needed to display it in a list. */
data class TransactionRow(
    @Embedded val transaction: TransactionEntity,
    val categoryName: String?,
    val categoryColor: Long?,
    val categoryIcon: String?,
    val accountName: String,
    val accountCurrency: Currency,
)

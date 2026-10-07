package com.vm.coinfold.app.feature.recurring.db.entities

import androidx.room.Embedded
import com.vm.coinfold.app.shared.domain.models.Currency

/** A schedule joined with the names needed to show it in the list. */
data class RecurringRow(
    @Embedded val recurring: RecurringEntity,
    val accountName: String,
    val accountCurrency: Currency,
    val categoryName: String?,
    val categoryColor: Long?,
    val categoryIcon: String?,
)

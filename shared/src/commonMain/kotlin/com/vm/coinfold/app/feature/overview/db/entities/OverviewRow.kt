package com.vm.coinfold.app.feature.overview.db.entities

import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.TransactionType

/** Minimal projection of a transaction, enough to build the overview statistics. */
data class OverviewRow(
    val type: TransactionType,
    val categoryId: Long?,
    val currency: Currency,
    val amountMinor: Long,
    /** UTC epoch millis. */
    val dateTime: Long,
)

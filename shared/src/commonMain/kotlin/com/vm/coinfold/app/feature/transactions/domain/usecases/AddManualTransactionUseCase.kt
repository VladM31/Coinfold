package com.vm.coinfold.app.feature.transactions.domain.usecases

import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.shared.analytics.Analytics
import com.vm.coinfold.app.shared.analytics.AnalyticsEvent
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType

/** Public entry point for other features (accounts) to record a top-up or a manual withdrawal. */
class AddManualTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(
        type: TransactionType,
        accountId: Long,
        amount: Money,
        source: IncomeSource?,
        note: String,
        dateTime: Long,
        isTransfer: Boolean = false,
    ) {
        repository.addManual(type, accountId, amount, source, note, dateTime, isTransfer)
        if (isTransfer) {
            // Both halves of a transfer come through here; count it once, on the withdrawal.
            if (type == TransactionType.EXPENSE) Analytics.event(AnalyticsEvent.TRANSFER_ADDED)
        } else {
            Analytics.event(AnalyticsEvent.MANUAL_TRANSACTION_ADDED, "type" to type.name.lowercase())
        }
    }
}

package com.vm.coinfold.app.feature.transactions.main

import com.vm.coinfold.app.shared.domain.Money
import com.vm.coinfold.app.shared.domain.TransactionType

/** Public entry point for other features (accounts) to record a top-up or a manual withdrawal. */
class AddManualTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(
        type: TransactionType,
        accountId: Long,
        amount: Money,
        source: IncomeSource?,
        note: String,
        dateTime: Long,
    ) = repository.addManual(type, accountId, amount, source, note, dateTime)
}

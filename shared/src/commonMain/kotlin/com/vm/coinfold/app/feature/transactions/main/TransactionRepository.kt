package com.vm.coinfold.app.feature.transactions.main

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.transactions.db.TransactionDao
import com.vm.coinfold.app.feature.transactions.db.TransactionEntity
import com.vm.coinfold.app.shared.domain.Money
import com.vm.coinfold.app.shared.domain.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    /** Names of custom income sources the user has entered before. */
    val customIncomeSources: Flow<List<String>>

    /**
     * Adds a top-up (INCOME, with [source]) or a manual withdrawal (EXPENSE without category).
     * [amount] is in the currency of the account, so no conversion is involved (rate 1).
     */
    suspend fun addManual(
        type: TransactionType,
        accountId: Long,
        amount: Money,
        source: IncomeSource?,
        note: String,
        dateTime: Long,
    )
}

class TransactionRepositoryImpl(private val dao: TransactionDao) : TransactionRepository {

    override val customIncomeSources: Flow<List<String>> = dao.observeCustomIncomeSources()

    override suspend fun addManual(
        type: TransactionType,
        accountId: Long,
        amount: Money,
        source: IncomeSource?,
        note: String,
        dateTime: Long,
    ) {
        require(amount.minorUnits > 0) { "Amount must be positive" }
        dao.insert(
            TransactionEntity(
                type = type,
                accountId = accountId,
                incomeSource = if (type == TransactionType.INCOME) source?.toStored() else null,
                amountMinor = amount.minorUnits,
                currency = amount.currency,
                accountAmountMinor = amount.minorUnits,
                rate = BigDecimal.ONE.toPlainString(),
                note = note.trim(),
                dateTime = dateTime,
            ),
        )
    }
}

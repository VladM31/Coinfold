package com.vm.coinfold.app.feature.transactions.domain.repositories

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionQuery
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType
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

    /** Adds a categorized expense; [accountAmount] is [amount] converted at [rate] into the account currency. */
    suspend fun addExpense(
        accountId: Long,
        categoryId: Long,
        amount: Money,
        accountAmount: Money,
        rate: BigDecimal,
        note: String,
        dateTime: Long,
    )

    /** Newest first, at most [limit] items matching [query]. */
    fun observeItems(query: TransactionQuery, limit: Int): Flow<List<TransactionItem>>

    /** Overwrites an existing transaction; the account balance follows automatically. */
    suspend fun update(
        id: Long,
        type: TransactionType,
        accountId: Long,
        categoryId: Long?,
        source: IncomeSource?,
        amount: Money,
        accountAmount: Money,
        rate: BigDecimal,
        note: String,
        dateTime: Long,
    )

    suspend fun delete(id: Long)
}

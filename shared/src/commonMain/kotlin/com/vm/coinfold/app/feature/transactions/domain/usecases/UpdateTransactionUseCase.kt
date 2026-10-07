package com.vm.coinfold.app.feature.transactions.domain.usecases

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.currency.domain.usecases.ConvertMoneyUseCase
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.feature.transactions.domain.models.UpdateTransactionResult
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money

/**
 * Saves edits of a transaction. The stored rate is kept as long as the currency pair (operation currency
 * to account currency) did not change, so history is never recalculated at today's rate; a changed pair
 * gets the current rate, and without one the edit is refused.
 */
class UpdateTransactionUseCase(
    private val repository: TransactionRepository,
    private val convert: ConvertMoneyUseCase,
) {
    suspend operator fun invoke(
        original: TransactionItem,
        accountId: Long,
        accountCurrency: Currency,
        categoryId: Long?,
        source: IncomeSource?,
        amount: Money,
        note: String,
        dateTime: Long,
    ): UpdateTransactionResult {
        val accountAmount: Money
        val rate: BigDecimal
        when {
            amount.currency == accountCurrency -> {
                accountAmount = amount
                rate = BigDecimal.ONE
            }
            original.amount.currency == amount.currency && original.accountCurrency == accountCurrency -> {
                rate = original.rate
                accountAmount = amount.convertTo(accountCurrency, rate)
            }
            else -> {
                val conversion = convert(amount, accountCurrency) ?: return UpdateTransactionResult.NO_RATE
                accountAmount = conversion.money
                rate = conversion.rate
            }
        }
        repository.update(
            id = original.id,
            type = original.type,
            accountId = accountId,
            categoryId = categoryId,
            source = source,
            amount = amount,
            // A tiny amount may round to zero in the account currency; never store a zero debit.
            accountAmount = accountAmount.takeIf { it.minorUnits > 0 } ?: Money(1, accountCurrency),
            rate = rate,
            note = note,
            dateTime = dateTime,
        )
        return UpdateTransactionResult.SUCCESS
    }
}

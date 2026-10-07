package com.vm.coinfold.app.feature.transactions.domain.usecases

import com.vm.coinfold.app.feature.currency.domain.usecases.ConvertMoneyUseCase
import com.vm.coinfold.app.feature.transactions.domain.models.AddExpenseResult
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money

/**
 * Records an expense in a category. If [amount] is not in the currency of the account it is converted
 * at the current rate, and that rate is stored with the transaction. Without a known rate the
 * cross-currency expense is refused.
 */
class AddExpenseUseCase(
    private val repository: TransactionRepository,
    private val convert: ConvertMoneyUseCase,
) {
    suspend operator fun invoke(
        accountId: Long,
        accountCurrency: Currency,
        categoryId: Long,
        amount: Money,
        note: String,
        dateTime: Long,
    ): AddExpenseResult {
        val conversion = convert(amount, accountCurrency) ?: return AddExpenseResult.NO_RATE
        // A tiny amount may round to zero in the account currency; never store a zero debit.
        val accountAmount = conversion.money.takeIf { it.minorUnits > 0 } ?: Money(1, accountCurrency)
        repository.addExpense(accountId, categoryId, amount, accountAmount, conversion.rate, note, dateTime)
        return AddExpenseResult.SUCCESS
    }
}

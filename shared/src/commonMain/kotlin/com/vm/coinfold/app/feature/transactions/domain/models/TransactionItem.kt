package com.vm.coinfold.app.feature.transactions.domain.models

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.TransactionType

/** Category of an expense as shown in the list. */
data class TransactionCategory(val id: Long, val name: String, val color: Long, val icon: String)

/**
 * A transaction ready for display. [amount] is as entered (operation currency), [accountAmount] is what
 * actually left/entered the account, [rate] is the rate fixed when the transaction was made.
 * An expense without a [category] is a manual withdrawal.
 */
data class TransactionItem(
    val id: Long,
    val type: TransactionType,
    val accountId: Long,
    val accountName: String,
    val accountCurrency: Currency,
    val category: TransactionCategory?,
    val source: IncomeSource?,
    val note: String,
    val amount: Money,
    val accountAmount: Money,
    val rate: BigDecimal,
    /** UTC epoch millis. */
    val dateTime: Long,
) {
    /** Signed amount in the operation currency: positive for income, negative for expenses. */
    val signedAmount: Money
        get() = if (type == TransactionType.INCOME) amount else -amount
}

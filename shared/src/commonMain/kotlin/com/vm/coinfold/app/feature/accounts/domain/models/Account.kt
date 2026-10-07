package com.vm.coinfold.app.feature.accounts.domain.models

import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money

data class Account(
    val id: Long,
    val name: String,
    val currency: Currency,
    val initialBalance: Money,
    /** ARGB color, optional. */
    val color: Long?,
    val icon: String?,
)

/** [balance] = initial balance + income - expenses, in the currency of the account. */
data class AccountWithBalance(
    val account: Account,
    val balance: Money,
    val transactionCount: Int,
)

/** Data needed to create ([id] == null) or update an account. */
data class AccountDraft(
    val id: Long?,
    val name: String,
    val currency: Currency,
    val initialBalance: Money,
    val color: Long?,
)

enum class DeleteResult { DELETED, ARCHIVED }

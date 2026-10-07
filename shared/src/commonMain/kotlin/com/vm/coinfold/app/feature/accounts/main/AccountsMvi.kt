package com.vm.coinfold.app.feature.accounts.main

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.transactions.main.IncomeSource
import com.vm.coinfold.app.shared.domain.Currency
import com.vm.coinfold.app.shared.domain.TransactionType
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

data class AccountsState(
    val isLoading: Boolean = true,
    val accounts: List<AccountWithBalance> = emptyList(),
    val total: TotalBalance? = null,
    val mainCurrency: Currency = Currency.UAH,
    /** Custom income sources entered earlier, offered as suggestions. */
    val customSources: List<String> = emptyList(),
    val dialog: AccountsDialog? = null,
)

sealed interface AccountsDialog {
    data object AddAccount : AccountsDialog
    data class EditAccount(val item: AccountWithBalance) : AccountsDialog
    data class ConfirmDelete(val item: AccountWithBalance) : AccountsDialog
    data class Operation(val item: AccountWithBalance, val type: TransactionType) : AccountsDialog
}

sealed interface AccountsIntent {
    data object AddAccountClicked : AccountsIntent
    data class EditAccountClicked(val item: AccountWithBalance) : AccountsIntent
    data class DeleteAccountClicked(val item: AccountWithBalance) : AccountsIntent
    data class TopUpClicked(val item: AccountWithBalance) : AccountsIntent
    data class WithdrawClicked(val item: AccountWithBalance) : AccountsIntent
    data object DismissDialog : AccountsIntent
    data object ConfirmDelete : AccountsIntent
    data class MainCurrencySelected(val currency: Currency) : AccountsIntent
    data class SaveAccount(
        val id: Long?,
        val name: String,
        val currency: Currency,
        val initialBalance: BigDecimal,
        val color: Long?,
    ) : AccountsIntent

    data class SaveOperation(
        val amount: BigDecimal,
        val source: IncomeSource?,
        val note: String,
        val date: LocalDate,
    ) : AccountsIntent
}

sealed interface AccountsEffect {
    data class ShowMessage(val message: StringResource) : AccountsEffect
}

package com.vm.coinfold.app.feature.transactions.domain.viewmodels

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.transactions.domain.models.DayGroup
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionFilter
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

data class TransactionsState(
    val isLoading: Boolean = true,
    val groups: List<DayGroup> = emptyList(),
    val filter: TransactionFilter = TransactionFilter(),
    val mainCurrency: Currency = Currency.UAH,
    val accounts: List<AccountWithBalance> = emptyList(),
    val categories: List<Category> = emptyList(),
    val customSources: List<String> = emptyList(),
    /** True while there may be more items than currently loaded. */
    val canLoadMore: Boolean = false,
    val dialog: TransactionsDialog? = null,
)

sealed interface TransactionsDialog {
    data object Filter : TransactionsDialog
    data class Edit(val item: TransactionItem) : TransactionsDialog
    data class ConfirmDelete(val item: TransactionItem) : TransactionsDialog
}

sealed interface TransactionsIntent {
    data object FilterClicked : TransactionsIntent
    data class ApplyFilter(val filter: TransactionFilter) : TransactionsIntent
    data object ResetFilter : TransactionsIntent
    data object LoadMore : TransactionsIntent
    data class ItemClicked(val item: TransactionItem) : TransactionsIntent
    data class DeleteClicked(val item: TransactionItem) : TransactionsIntent
    data object ConfirmDelete : TransactionsIntent
    data object UndoDelete : TransactionsIntent
    data class DuplicateClicked(val item: TransactionItem) : TransactionsIntent
    data object DismissDialog : TransactionsIntent
    data class SaveEdit(
        val amount: BigDecimal,
        val currency: Currency,
        val accountId: Long,
        val categoryId: Long?,
        val source: IncomeSource?,
        val note: String,
        val date: LocalDate,
        /** A rate typed for this transaction, or null to keep/derive it. */
        val rateOverride: BigDecimal? = null,
    ) : TransactionsIntent
}

sealed interface TransactionsEffect {
    data class ShowMessage(val message: StringResource) : TransactionsEffect

    /** A message with an "Undo" action that sends [TransactionsIntent.UndoDelete]. */
    data class ShowUndo(val message: StringResource) : TransactionsEffect
}

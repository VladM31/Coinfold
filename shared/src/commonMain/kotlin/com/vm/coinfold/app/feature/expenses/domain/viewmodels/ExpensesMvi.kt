package com.vm.coinfold.app.feature.expenses.domain.viewmodels

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.expenses.domain.models.ExpenseSummary
import com.vm.coinfold.app.feature.transactions.domain.models.NoteSuggestion
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Period
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

data class ExpensesState(
    val isLoading: Boolean = true,
    val period: Period? = null,
    val mainCurrency: Currency = Currency.UAH,
    val categories: List<Category> = emptyList(),
    val summary: ExpenseSummary? = null,
    val accounts: List<AccountWithBalance> = emptyList(),
    val lastUsedCurrency: Currency = Currency.UAH,
    /** Known exchange rates, used by the expense sheet to show the amount that leaves the account. */
    val rates: RateTable = RateTable.Empty,
    /** Notes used before, for autocomplete and category suggestions in the expense sheet. */
    val noteSuggestions: List<NoteSuggestion> = emptyList(),
    val dialog: ExpensesDialog? = null,
)

sealed interface ExpensesDialog {
    data object AddCategory : ExpensesDialog
    data class EditCategory(val category: Category) : ExpensesDialog
    data class ConfirmDeleteCategory(val category: Category) : ExpensesDialog
    data class AddExpense(val category: Category) : ExpensesDialog
}

sealed interface ExpensesIntent {
    data object PreviousPeriod : ExpensesIntent
    data object NextPeriod : ExpensesIntent
    data object AddCategoryClicked : ExpensesIntent
    data class CategoryClicked(val category: Category) : ExpensesIntent
    data class CategoryLongClicked(val category: Category) : ExpensesIntent
    data object DismissDialog : ExpensesIntent
    data class SaveCategory(val id: Long?, val name: String, val color: Long, val icon: String) : ExpensesIntent
    data class DeleteCategoryClicked(val category: Category) : ExpensesIntent
    data object ConfirmDeleteCategory : ExpensesIntent
    data object UndoDeleteCategory : ExpensesIntent
    data class ReorderCategories(val ids: List<Long>) : ExpensesIntent
    data class SaveExpense(
        val amount: BigDecimal,
        val currency: Currency,
        val accountId: Long,
        /** Usually the tapped category, but the sheet can switch to a suggested one. */
        val categoryId: Long,
        val note: String,
        val date: LocalDate,
        /** A rate typed for this expense, or null to use the bank rate. */
        val rateOverride: BigDecimal? = null,
    ) : ExpensesIntent
}

sealed interface ExpensesEffect {
    data class ShowMessage(val message: StringResource) : ExpensesEffect

    /** A message with an "Undo" action that sends [ExpensesIntent.UndoDeleteCategory]. */
    data class ShowUndo(val message: StringResource) : ExpensesEffect
}

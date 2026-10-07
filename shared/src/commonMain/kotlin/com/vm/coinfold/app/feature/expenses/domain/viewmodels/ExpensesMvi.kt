package com.vm.coinfold.app.feature.expenses.domain.viewmodels

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.expenses.domain.models.ExpenseSummary
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
    data class MoveCategory(val category: Category, val up: Boolean) : ExpensesIntent
    data class SaveExpense(
        val amount: BigDecimal,
        val currency: Currency,
        val accountId: Long,
        val note: String,
        val date: LocalDate,
    ) : ExpensesIntent
}

sealed interface ExpensesEffect {
    data class ShowMessage(val message: StringResource) : ExpensesEffect
}

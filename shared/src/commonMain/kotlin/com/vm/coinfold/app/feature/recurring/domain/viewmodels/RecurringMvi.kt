package com.vm.coinfold.app.feature.recurring.domain.viewmodels

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.recurring.domain.models.Frequency
import com.vm.coinfold.app.feature.recurring.domain.models.RecurringPayment
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.TransactionType
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

data class RecurringState(
    val isLoading: Boolean = true,
    val payments: List<RecurringPayment> = emptyList(),
    val accounts: List<AccountWithBalance> = emptyList(),
    val categories: List<Category> = emptyList(),
    val customSources: List<String> = emptyList(),
    val dialog: RecurringDialog? = null,
)

sealed interface RecurringDialog {
    data object Add : RecurringDialog
    data class Edit(val payment: RecurringPayment) : RecurringDialog
}

sealed interface RecurringIntent {
    data object AddClicked : RecurringIntent
    data class ItemClicked(val payment: RecurringPayment) : RecurringIntent
    data object DismissDialog : RecurringIntent
    data class ToggleActive(val payment: RecurringPayment, val active: Boolean) : RecurringIntent
    data class Delete(val payment: RecurringPayment) : RecurringIntent
    data object UndoDelete : RecurringIntent
    data class Save(
        val id: Long?,
        val type: TransactionType,
        val accountId: Long,
        val categoryId: Long?,
        val source: IncomeSource?,
        val amount: BigDecimal,
        val note: String,
        val frequency: Frequency,
        val firstDate: LocalDate,
        val isActive: Boolean,
    ) : RecurringIntent
}

sealed interface RecurringEffect {
    /** A message with an "Undo" action that sends [RecurringIntent.UndoDelete]. */
    data class ShowUndo(val message: StringResource) : RecurringEffect
}

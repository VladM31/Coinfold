package com.vm.coinfold.app.feature.recurring.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.recurring_deleted
import com.vm.coinfold.app.feature.accounts.domain.repositories.AccountRepository
import com.vm.coinfold.app.feature.expenses.domain.repositories.CategoryRepository
import com.vm.coinfold.app.feature.recurring.domain.models.RecurringDraft
import com.vm.coinfold.app.feature.recurring.domain.repositories.RecurringRepository
import com.vm.coinfold.app.feature.recurring.domain.usecases.ProcessRecurringPaymentsUseCase
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.shared.domain.models.Money
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecurringViewModel(
    private val repository: RecurringRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository,
    private val processRecurring: ProcessRecurringPaymentsUseCase,
) : ViewModel() {

    private val dialog = MutableStateFlow<RecurringDialog?>(null)
    private val effects = Channel<RecurringEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    val state: StateFlow<RecurringState> = combine(
        repository.payments,
        accountRepository.accounts,
        categoryRepository.categories,
        transactionRepository.customIncomeSources,
        dialog,
    ) { payments, accounts, categories, sources, dialog ->
        RecurringState(
            isLoading = false,
            payments = payments,
            accounts = accounts,
            categories = categories,
            customSources = sources,
            dialog = dialog,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecurringState())

    fun onIntent(intent: RecurringIntent) {
        when (intent) {
            RecurringIntent.AddClicked -> dialog.value = RecurringDialog.Add
            is RecurringIntent.ItemClicked -> dialog.value = RecurringDialog.Edit(intent.payment)
            RecurringIntent.DismissDialog -> dialog.value = null
            is RecurringIntent.ToggleActive ->
                viewModelScope.launch { repository.setActive(intent.payment.id, intent.active) }
            is RecurringIntent.Delete -> viewModelScope.launch {
                repository.delete(intent.payment.id)
                dialog.value = null
                effects.send(RecurringEffect.ShowUndo(Res.string.recurring_deleted))
            }
            RecurringIntent.UndoDelete -> viewModelScope.launch { repository.undoDelete() }
            is RecurringIntent.Save -> save(intent)
        }
    }

    private fun save(intent: RecurringIntent.Save) {
        val account = state.value.accounts.firstOrNull { it.account.id == intent.accountId }?.account
            // An existing schedule on an archived account keeps its account.
            ?: return
        val amount = Money.of(intent.amount, account.currency)
        if (amount.minorUnits <= 0) return
        viewModelScope.launch {
            repository.save(
                RecurringDraft(
                    id = intent.id,
                    type = intent.type,
                    accountId = intent.accountId,
                    categoryId = intent.categoryId,
                    source = intent.source,
                    amount = amount,
                    note = intent.note,
                    frequency = intent.frequency,
                    firstDate = intent.firstDate,
                    isActive = intent.isActive,
                ),
            )
            dialog.value = null
            // A schedule that starts today or in the past should create its transactions right away.
            processRecurring()
        }
    }
}

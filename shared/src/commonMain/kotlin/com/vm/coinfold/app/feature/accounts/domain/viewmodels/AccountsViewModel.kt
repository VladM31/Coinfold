package com.vm.coinfold.app.feature.accounts.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_archived
import coinfold.shared.generated.resources.accounts_deleted
import com.vm.coinfold.app.feature.accounts.domain.models.AccountDraft
import com.vm.coinfold.app.feature.accounts.domain.models.DeleteResult
import com.vm.coinfold.app.feature.accounts.domain.repositories.AccountRepository
import com.vm.coinfold.app.feature.accounts.domain.services.calculateTotalBalance
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.feature.transactions.domain.usecases.AddManualTransactionUseCase
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.utils.epochMillisFor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    currencyRepository: CurrencyRepository,
    transactionRepository: TransactionRepository,
    private val addManualTransaction: AddManualTransactionUseCase,
) : ViewModel() {

    private val dialog = MutableStateFlow<AccountsDialog?>(null)
    private val effects = Channel<AccountsEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    val state: StateFlow<AccountsState> = combine(
        accountRepository.accounts,
        settingsRepository.settings,
        currencyRepository.rateTable,
        transactionRepository.customIncomeSources,
        dialog,
    ) { accounts, settings, rates, sources, dialog ->
        AccountsState(
            isLoading = false,
            accounts = accounts,
            total = calculateTotalBalance(accounts, settings.mainCurrency, rates),
            mainCurrency = settings.mainCurrency,
            customSources = sources,
            dialog = dialog,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsState())

    fun onIntent(intent: AccountsIntent) {
        when (intent) {
            AccountsIntent.AddAccountClicked -> dialog.value = AccountsDialog.AddAccount
            is AccountsIntent.EditAccountClicked -> dialog.value = AccountsDialog.EditAccount(intent.item)
            is AccountsIntent.DeleteAccountClicked -> dialog.value = AccountsDialog.ConfirmDelete(intent.item)
            is AccountsIntent.AccountClicked -> dialog.value = AccountsDialog.Operation(intent.item)
            AccountsIntent.DismissDialog -> dialog.value = null
            is AccountsIntent.MainCurrencySelected ->
                viewModelScope.launch { settingsRepository.setMainCurrency(intent.currency) }
            is AccountsIntent.SaveAccount -> saveAccount(intent)
            AccountsIntent.ConfirmDelete -> confirmDelete()
            AccountsIntent.UndoDelete -> viewModelScope.launch { accountRepository.undoDelete() }
            AccountsIntent.QuickAddIncome -> viewModelScope.launch {
                accountRepository.accounts.first().firstOrNull()?.let { dialog.value = AccountsDialog.Operation(it) }
            }
            is AccountsIntent.SaveOperation -> saveOperation(intent)
        }
    }

    private fun saveAccount(intent: AccountsIntent.SaveAccount) {
        val name = intent.name.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            accountRepository.save(
                AccountDraft(
                    id = intent.id,
                    name = name,
                    currency = intent.currency,
                    initialBalance = Money.of(intent.initialBalance, intent.currency),
                    color = intent.color,
                    icon = intent.icon,
                ),
            )
            dialog.value = null
        }
    }

    private fun confirmDelete() {
        val target = (dialog.value as? AccountsDialog.ConfirmDelete)?.item ?: return
        viewModelScope.launch {
            val result = accountRepository.delete(target.account.id)
            dialog.value = null
            val message = when (result) {
                DeleteResult.DELETED -> Res.string.accounts_deleted
                DeleteResult.ARCHIVED -> Res.string.accounts_archived
            }
            effects.send(AccountsEffect.ShowUndo(message))
        }
    }

    private fun saveOperation(intent: AccountsIntent.SaveOperation) {
        val operation = dialog.value as? AccountsDialog.Operation ?: return
        val account = operation.item.account
        val amount = Money.of(intent.amount, account.currency)
        if (amount.minorUnits <= 0) return
        viewModelScope.launch {
            addManualTransaction(
                type = intent.type,
                accountId = account.id,
                amount = amount,
                source = intent.source,
                note = intent.note,
                dateTime = epochMillisFor(intent.date),
            )
            dialog.value = null
        }
    }
}

package com.vm.coinfold.app.feature.expenses.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.category_archived
import coinfold.shared.generated.resources.category_deleted
import coinfold.shared.generated.resources.expense_no_rate
import com.vm.coinfold.app.feature.accounts.main.AccountRepository
import com.vm.coinfold.app.feature.currency.main.CurrencyRepository
import com.vm.coinfold.app.feature.currency.main.RateTable
import com.vm.coinfold.app.feature.settings.main.Settings
import com.vm.coinfold.app.feature.settings.main.SettingsRepository
import com.vm.coinfold.app.feature.transactions.main.AddExpenseResult
import com.vm.coinfold.app.feature.transactions.main.AddExpenseUseCase
import com.vm.coinfold.app.shared.domain.Money
import com.vm.coinfold.app.utils.epochMillisFor
import com.vm.coinfold.app.utils.today
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class PeriodData(
    val period: Period,
    val totals: List<PeriodTotal>,
    val categories: List<Category>,
    val rates: RateTable,
    val settings: Settings,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesViewModel(
    private val categoryRepository: CategoryRepository,
    private val statsRepository: ExpenseStatsRepository,
    private val settingsRepository: SettingsRepository,
    currencyRepository: CurrencyRepository,
    accountRepository: AccountRepository,
    private val addExpense: AddExpenseUseCase,
) : ViewModel() {

    private val dialog = MutableStateFlow<ExpensesDialog?>(null)
    /** How many months the user has navigated away from the current period. */
    private val monthOffset = MutableStateFlow(0)
    private val effects = Channel<ExpensesEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    private val period: Flow<Period> = combine(
        settingsRepository.settings.map { it.periodStartDay }.distinctUntilChanged(),
        monthOffset,
    ) { startDay, offset -> Period.containing(today(), startDay).shiftMonths(offset) }

    private val periodData: Flow<PeriodData> = combine(
        period.flatMapLatest { p -> statsRepository.observeTotals(p).map { p to it } },
        categoryRepository.categories,
        currencyRepository.rateTable,
        settingsRepository.settings,
    ) { (period, totals), categories, rates, settings ->
        PeriodData(period, totals, categories, rates, settings)
    }

    val state: StateFlow<ExpensesState> = combine(
        periodData,
        accountRepository.accounts,
        dialog,
    ) { data, accounts, dialog ->
        ExpensesState(
            isLoading = false,
            period = data.period,
            mainCurrency = data.settings.mainCurrency,
            categories = data.categories,
            summary = calculateSummary(data.totals, data.categories, data.rates, data.settings.mainCurrency),
            accounts = accounts,
            lastUsedCurrency = data.settings.lastUsedCurrency,
            dialog = dialog,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpensesState())

    fun onIntent(intent: ExpensesIntent) {
        when (intent) {
            ExpensesIntent.PreviousPeriod -> monthOffset.value -= 1
            ExpensesIntent.NextPeriod -> monthOffset.value += 1
            ExpensesIntent.AddCategoryClicked -> dialog.value = ExpensesDialog.AddCategory
            is ExpensesIntent.CategoryClicked -> dialog.value = ExpensesDialog.AddExpense(intent.category)
            is ExpensesIntent.CategoryLongClicked -> dialog.value = ExpensesDialog.EditCategory(intent.category)
            ExpensesIntent.DismissDialog -> dialog.value = null
            is ExpensesIntent.SaveCategory -> saveCategory(intent)
            is ExpensesIntent.DeleteCategoryClicked -> dialog.value = ExpensesDialog.ConfirmDeleteCategory(intent.category)
            ExpensesIntent.ConfirmDeleteCategory -> confirmDelete()
            is ExpensesIntent.MoveCategory ->
                viewModelScope.launch { categoryRepository.move(intent.category.id, intent.up) }
            is ExpensesIntent.SaveExpense -> saveExpense(intent)
        }
    }

    private fun saveCategory(intent: ExpensesIntent.SaveCategory) {
        if (intent.name.isBlank()) return
        viewModelScope.launch {
            categoryRepository.save(intent.id, intent.name, intent.color, intent.icon)
            dialog.value = null
        }
    }

    private fun confirmDelete() {
        val category = (dialog.value as? ExpensesDialog.ConfirmDeleteCategory)?.category ?: return
        viewModelScope.launch {
            val result = categoryRepository.delete(category.id)
            dialog.value = null
            val message = when (result) {
                CategoryDeleteResult.DELETED -> Res.string.category_deleted
                CategoryDeleteResult.ARCHIVED -> Res.string.category_archived
            }
            effects.send(ExpensesEffect.ShowMessage(message))
        }
    }

    private fun saveExpense(intent: ExpensesIntent.SaveExpense) {
        val category = (dialog.value as? ExpensesDialog.AddExpense)?.category ?: return
        val account = state.value.accounts.firstOrNull { it.account.id == intent.accountId }?.account ?: return
        val amount = Money.of(intent.amount, intent.currency)
        if (amount.minorUnits <= 0) return
        viewModelScope.launch {
            val result = addExpense(
                accountId = account.id,
                accountCurrency = account.currency,
                categoryId = category.id,
                amount = amount,
                note = intent.note,
                dateTime = epochMillisFor(intent.date),
            )
            when (result) {
                AddExpenseResult.SUCCESS -> {
                    settingsRepository.setLastUsedCurrency(intent.currency)
                    dialog.value = null
                }
                AddExpenseResult.NO_RATE -> effects.send(ExpensesEffect.ShowMessage(Res.string.expense_no_rate))
            }
        }
    }
}

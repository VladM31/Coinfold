package com.vm.coinfold.app.feature.transactions.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.expense_no_rate
import coinfold.shared.generated.resources.transaction_deleted
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.accounts.domain.repositories.AccountRepository
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.expenses.domain.repositories.CategoryRepository
import com.vm.coinfold.app.feature.settings.domain.models.Settings
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.feature.transactions.domain.models.DayGroup
import com.vm.coinfold.app.feature.transactions.domain.models.PeriodFilter
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionFilter
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionQuery
import com.vm.coinfold.app.feature.transactions.domain.models.UpdateTransactionResult
import com.vm.coinfold.app.feature.transactions.domain.repositories.TransactionRepository
import com.vm.coinfold.app.feature.transactions.domain.services.groupByDay
import com.vm.coinfold.app.feature.transactions.domain.usecases.UpdateTransactionUseCase
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.domain.models.Period
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.utils.epochMillisFor
import com.vm.coinfold.app.utils.today
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private const val PAGE_SIZE = 100

private data class ListData(val groups: List<DayGroup>, val itemCount: Int, val settings: Settings)

private data class References(
    val accounts: List<AccountWithBalance>,
    val categories: List<Category>,
    val customSources: List<String>,
)

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository,
    currencyRepository: CurrencyRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    private val updateTransaction: UpdateTransactionUseCase,
) : ViewModel() {

    private val filter = MutableStateFlow(TransactionFilter())
    private val limit = MutableStateFlow(PAGE_SIZE)
    private val dialog = MutableStateFlow<TransactionsDialog?>(null)
    private val effects = Channel<TransactionsEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    private val items: Flow<List<TransactionItem>> = combine(
        filter,
        settingsRepository.settings,
        limit,
    ) { filter, settings, limit -> Triple(filter, settings.periodStartDay, limit) }
        .flatMapLatest { (filter, startDay, limit) ->
            transactionRepository.observeItems(filter.toQuery(startDay), limit)
        }

    private val listData: Flow<ListData> = combine(
        items,
        currencyRepository.rateTable,
        settingsRepository.settings,
    ) { items, rates: RateTable, settings ->
        ListData(
            groups = groupByDay(items, rates, settings.mainCurrency, TimeZone.currentSystemDefault()),
            itemCount = items.size,
            settings = settings,
        )
    }

    private val references: Flow<References> = combine(
        accountRepository.accounts,
        categoryRepository.categories,
        transactionRepository.customIncomeSources,
        ::References,
    )

    val state: StateFlow<TransactionsState> = combine(
        listData,
        references,
        filter,
        limit,
        dialog,
    ) { data, refs, filter, limit, dialog ->
        TransactionsState(
            isLoading = false,
            groups = data.groups,
            filter = filter,
            mainCurrency = data.settings.mainCurrency,
            accounts = refs.accounts,
            categories = refs.categories,
            customSources = refs.customSources,
            canLoadMore = data.itemCount >= limit,
            dialog = dialog,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsState())

    fun onIntent(intent: TransactionsIntent) {
        when (intent) {
            TransactionsIntent.FilterClicked -> dialog.value = TransactionsDialog.Filter
            is TransactionsIntent.ApplyFilter -> {
                filter.value = intent.filter
                limit.value = PAGE_SIZE
                dialog.value = null
            }
            TransactionsIntent.ResetFilter -> {
                filter.value = TransactionFilter()
                limit.value = PAGE_SIZE
            }
            TransactionsIntent.LoadMore -> if (state.value.canLoadMore) limit.value += PAGE_SIZE
            is TransactionsIntent.ItemClicked -> dialog.value = TransactionsDialog.Edit(intent.item)
            is TransactionsIntent.DeleteClicked -> dialog.value = TransactionsDialog.ConfirmDelete(intent.item)
            TransactionsIntent.ConfirmDelete -> confirmDelete()
            TransactionsIntent.DismissDialog -> dialog.value = null
            is TransactionsIntent.SaveEdit -> saveEdit(intent)
        }
    }

    private fun confirmDelete() {
        val item = (dialog.value as? TransactionsDialog.ConfirmDelete)?.item ?: return
        viewModelScope.launch {
            // Balances are derived from the transactions, so removing the row recalculates them.
            transactionRepository.delete(item.id)
            dialog.value = null
            effects.send(TransactionsEffect.ShowMessage(Res.string.transaction_deleted))
        }
    }

    private fun saveEdit(intent: TransactionsIntent.SaveEdit) {
        val original = (dialog.value as? TransactionsDialog.Edit)?.item ?: return
        val accountCurrency = state.value.accounts.firstOrNull { it.account.id == intent.accountId }?.account?.currency
            ?: original.accountCurrency.takeIf { intent.accountId == original.accountId }
            ?: return
        val isCategorized = original.type == TransactionType.EXPENSE && intent.categoryId != null
        // Top-ups and manual withdrawals are always in the currency of the account.
        val currency = if (original.type == TransactionType.INCOME || !isCategorized) accountCurrency else intent.currency
        val amount = Money.of(intent.amount, currency)
        if (amount.minorUnits <= 0) return

        val originalDate = Instant.fromEpochMilliseconds(original.dateTime)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        // Keep the exact time when the day was not changed so the order within the day is stable.
        val dateTime = if (intent.date == originalDate) original.dateTime else epochMillisFor(intent.date)

        viewModelScope.launch {
            val result = updateTransaction(
                original = original,
                accountId = intent.accountId,
                accountCurrency = accountCurrency,
                categoryId = intent.categoryId.takeIf { original.type == TransactionType.EXPENSE },
                source = intent.source.takeIf { original.type == TransactionType.INCOME },
                amount = amount,
                note = intent.note,
                dateTime = dateTime,
            )
            when (result) {
                UpdateTransactionResult.SUCCESS, UpdateTransactionResult.NOT_FOUND -> dialog.value = null
                UpdateTransactionResult.NO_RATE ->
                    effects.send(TransactionsEffect.ShowMessage(Res.string.expense_no_rate))
            }
        }
    }
}

/** Resolves the filter into concrete time bounds using the local time zone. */
private fun TransactionFilter.toQuery(periodStartDay: Int): TransactionQuery {
    val tz = TimeZone.currentSystemDefault()
    val (from, to) = when (val p = period) {
        PeriodFilter.All -> 0L to Long.MAX_VALUE
        PeriodFilter.CurrentPeriod -> {
            val period = Period.containing(today(tz), periodStartDay)
            period.startMillis(tz) to period.endMillis(tz)
        }
        is PeriodFilter.Custom ->
            p.from.atStartOfDayIn(tz).toEpochMilliseconds() to
                p.to.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz).toEpochMilliseconds()
    }
    return TransactionQuery(from, to, accountId, categoryId, type)
}

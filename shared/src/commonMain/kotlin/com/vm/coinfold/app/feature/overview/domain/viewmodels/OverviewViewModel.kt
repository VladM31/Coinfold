package com.vm.coinfold.app.feature.overview.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.expenses.domain.repositories.CategoryRepository
import com.vm.coinfold.app.feature.overview.domain.repositories.OverviewRepository
import com.vm.coinfold.app.feature.overview.domain.services.calculateOverview
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.shared.domain.models.Period
import com.vm.coinfold.app.utils.today
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class OverviewViewModel(
    overviewRepository: OverviewRepository,
    categoryRepository: CategoryRepository,
    currencyRepository: CurrencyRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    /** Months away from the current period. */
    private val monthOffset = MutableStateFlow(0)

    private val period: Flow<Period> = combine(
        settingsRepository.settings.map { it.periodStartDay }.distinctUntilChanged(),
        monthOffset,
    ) { startDay, offset -> Period.containing(today(), startDay).shiftMonths(offset) }

    val state: StateFlow<OverviewState> = combine(
        period.flatMapLatest { p -> overviewRepository.observeTransactions(p).map { p to it } },
        categoryRepository.categories,
        currencyRepository.rateTable,
        settingsRepository.settings,
    ) { (period, transactions), categories, rates, settings ->
        OverviewState(
            isLoading = false,
            period = period,
            summary = calculateOverview(
                transactions = transactions,
                categories = categories,
                rates = rates,
                mainCurrency = settings.mainCurrency,
                period = period,
                today = today(),
                timeZone = TimeZone.currentSystemDefault(),
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OverviewState())

    fun onIntent(intent: OverviewIntent) {
        when (intent) {
            OverviewIntent.PreviousPeriod -> monthOffset.value -= 1
            OverviewIntent.NextPeriod -> monthOffset.value += 1
        }
    }
}

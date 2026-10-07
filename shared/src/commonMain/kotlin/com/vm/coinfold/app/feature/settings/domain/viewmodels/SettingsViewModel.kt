package com.vm.coinfold.app.feature.settings.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.settings_rates_not_updated
import coinfold.shared.generated.resources.settings_rates_updated
import com.vm.coinfold.app.feature.currency.domain.repositories.CurrencyRepository
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val currencyRepository: CurrencyRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    val state: StateFlow<SettingsState> = combine(
        settingsRepository.settings,
        currencyRepository.updatedAt,
        isRefreshing,
    ) { settings, updatedAt, refreshing ->
        SettingsState(settings, updatedAt, refreshing)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun onIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsIntent.ThemeSelected -> settingsRepository.setTheme(intent.theme)
                is SettingsIntent.LanguageSelected -> settingsRepository.setLanguage(intent.language)
                is SettingsIntent.MainCurrencySelected -> settingsRepository.setMainCurrency(intent.currency)
                is SettingsIntent.PeriodStartDaySelected -> settingsRepository.setPeriodStartDay(intent.day)
                SettingsIntent.RefreshRatesClicked -> refreshRates()
            }
        }
    }

    private suspend fun refreshRates() {
        if (isRefreshing.value) return
        isRefreshing.value = true
        try {
            val updated = currencyRepository.refresh()
            val message = if (updated) Res.string.settings_rates_updated else Res.string.settings_rates_not_updated
            effects.send(SettingsEffect.ShowMessage(message))
        } finally {
            isRefreshing.value = false
        }
    }
}

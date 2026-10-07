package com.vm.coinfold.app.feature.settings.domain.viewmodels

import com.vm.coinfold.app.feature.settings.domain.models.AppLanguage
import com.vm.coinfold.app.feature.settings.domain.models.Settings
import com.vm.coinfold.app.feature.settings.domain.models.ThemeMode
import com.vm.coinfold.app.shared.domain.models.Currency
import org.jetbrains.compose.resources.StringResource

data class SettingsState(
    val settings: Settings = Settings(),
    /** Epoch millis of the last successful exchange rate refresh; null if never loaded. */
    val ratesUpdatedAt: Long? = null,
    val isRefreshingRates: Boolean = false,
)

sealed interface SettingsIntent {
    data class ThemeSelected(val theme: ThemeMode) : SettingsIntent
    data class LanguageSelected(val language: AppLanguage) : SettingsIntent
    data class MainCurrencySelected(val currency: Currency) : SettingsIntent
    data class PeriodStartDaySelected(val day: Int) : SettingsIntent
    data object RefreshRatesClicked : SettingsIntent
}

sealed interface SettingsEffect {
    data class ShowMessage(val message: StringResource) : SettingsEffect
}

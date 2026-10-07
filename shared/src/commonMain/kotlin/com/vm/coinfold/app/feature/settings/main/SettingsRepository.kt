package com.vm.coinfold.app.feature.settings.main

import com.vm.coinfold.app.feature.settings.db.SettingsStorage
import com.vm.coinfold.app.feature.settings.db.StoredSettings
import com.vm.coinfold.app.shared.domain.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun setTheme(theme: ThemeMode)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setMainCurrency(currency: Currency)
    suspend fun setPeriodStartDay(day: Int)
    suspend fun setLastUsedCurrency(currency: Currency)
}

class SettingsRepositoryImpl(private val storage: SettingsStorage) : SettingsRepository {

    override val settings: Flow<Settings> = storage.data.map { it.toSettings() }

    override suspend fun setTheme(theme: ThemeMode) = storage.setTheme(theme.name)
    override suspend fun setLanguage(language: AppLanguage) = storage.setLanguage(language.name)
    override suspend fun setMainCurrency(currency: Currency) = storage.setMainCurrency(currency.code)
    override suspend fun setPeriodStartDay(day: Int) = storage.setPeriodStartDay(day.coerceIn(1, 28))
    override suspend fun setLastUsedCurrency(currency: Currency) = storage.setLastUsedCurrency(currency.code)
}

private fun StoredSettings.toSettings(): Settings {
    val defaults = Settings()
    return Settings(
        theme = theme.toEnumOrNull<ThemeMode>() ?: defaults.theme,
        language = language.toEnumOrNull<AppLanguage>() ?: defaults.language,
        mainCurrency = mainCurrency.toCurrencyOrNull() ?: defaults.mainCurrency,
        periodStartDay = periodStartDay ?: defaults.periodStartDay,
        lastUsedCurrency = lastUsedCurrency.toCurrencyOrNull() ?: defaults.lastUsedCurrency,
    )
}

private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
    this?.let { name -> enumValues<T>().firstOrNull { it.name == name } }

private fun String?.toCurrencyOrNull(): Currency? = Currency.entries.firstOrNull { it.code == this }

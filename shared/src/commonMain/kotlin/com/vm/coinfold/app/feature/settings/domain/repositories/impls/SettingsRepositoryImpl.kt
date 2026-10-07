package com.vm.coinfold.app.feature.settings.domain.repositories.impls

import com.vm.coinfold.app.feature.settings.db.entities.StoredSettings
import com.vm.coinfold.app.feature.settings.db.storages.SettingsStorage
import com.vm.coinfold.app.feature.settings.domain.models.AppLanguage
import com.vm.coinfold.app.feature.settings.domain.models.Settings
import com.vm.coinfold.app.feature.settings.domain.models.ThemeMode
import com.vm.coinfold.app.feature.settings.domain.repositories.SettingsRepository
import com.vm.coinfold.app.shared.domain.models.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(private val storage: SettingsStorage) : SettingsRepository {

    override val settings: Flow<Settings> = storage.data.map { it.toSettings() }

    override suspend fun setTheme(theme: ThemeMode) = storage.setTheme(theme.name)
    override suspend fun setLanguage(language: AppLanguage) = storage.setLanguage(language.name)
    override suspend fun setMainCurrency(currency: Currency) = storage.setMainCurrency(currency.code)
    override suspend fun setPeriodStartDay(day: Int) = storage.setPeriodStartDay(day.coerceIn(1, 28))
    override suspend fun setLastUsedCurrency(currency: Currency) = storage.setLastUsedCurrency(currency.code)
    override suspend fun markCategoriesSeeded(): Boolean = storage.markCategoriesSeeded()
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

package com.vm.coinfold.app.feature.settings.domain.repositories

import com.vm.coinfold.app.feature.settings.domain.models.AppLanguage
import com.vm.coinfold.app.feature.settings.domain.models.Settings
import com.vm.coinfold.app.feature.settings.domain.models.ThemeMode
import com.vm.coinfold.app.shared.domain.models.Currency
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun setTheme(theme: ThemeMode)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setMainCurrency(currency: Currency)
    suspend fun setPeriodStartDay(day: Int)
    suspend fun setLastUsedCurrency(currency: Currency)

    /** True if the default categories of [version] were not applied yet (and marks them as applied). */
    suspend fun claimDefaultCategories(version: Int): Boolean
}

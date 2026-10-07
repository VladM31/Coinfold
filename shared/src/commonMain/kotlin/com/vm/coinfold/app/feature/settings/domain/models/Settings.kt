package com.vm.coinfold.app.feature.settings.domain.models

import com.vm.coinfold.app.shared.domain.models.Currency

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class AppLanguage { SYSTEM, EN, UK }

data class Settings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val mainCurrency: Currency = Currency.UAH,
    /** Day of month (1..28) on which the calculation period starts. */
    val periodStartDay: Int = 1,
    val lastUsedCurrency: Currency = Currency.UAH,
)

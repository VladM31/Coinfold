package com.vm.coinfold.app.feature.settings.db.entities

/** Raw stored values; null means "never set". Mapping to domain types happens in `main`. */
data class StoredSettings(
    val theme: String?,
    val language: String?,
    val mainCurrency: String?,
    val periodStartDay: Int?,
    val lastUsedCurrency: String?,
)

package com.vm.coinfold.app.feature.settings.db

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Raw stored values; null means "never set". Mapping to domain types happens in `main`. */
data class StoredSettings(
    val theme: String?,
    val language: String?,
    val mainCurrency: String?,
    val periodStartDay: Int?,
    val lastUsedCurrency: String?,
)

class SettingsStorage(private val dataStore: DataStore<Preferences>) {

    val data: Flow<StoredSettings> = dataStore.data.map { prefs ->
        StoredSettings(
            theme = prefs[THEME],
            language = prefs[LANGUAGE],
            mainCurrency = prefs[MAIN_CURRENCY],
            periodStartDay = prefs[PERIOD_START_DAY],
            lastUsedCurrency = prefs[LAST_USED_CURRENCY],
        )
    }

    suspend fun setTheme(value: String) {
        dataStore.edit { it[THEME] = value }
    }
    suspend fun setLanguage(value: String) {
        dataStore.edit { it[LANGUAGE] = value }
    }
    suspend fun setMainCurrency(value: String) {
        dataStore.edit { it[MAIN_CURRENCY] = value }
    }
    suspend fun setPeriodStartDay(value: Int) {
        dataStore.edit { it[PERIOD_START_DAY] = value }
    }
    suspend fun setLastUsedCurrency(value: String) {
        dataStore.edit { it[LAST_USED_CURRENCY] = value }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val MAIN_CURRENCY = stringPreferencesKey("main_currency")
        val PERIOD_START_DAY = intPreferencesKey("period_start_day")
        val LAST_USED_CURRENCY = stringPreferencesKey("last_used_currency")
    }
}

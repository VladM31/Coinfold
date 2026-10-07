package com.vm.coinfold.app.feature.settings.db.storages

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vm.coinfold.app.feature.settings.db.entities.StoredSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    /** Returns true only the first time it is called, so one-time setup runs exactly once. */
    suspend fun markCategoriesSeeded(): Boolean {
        var first = false
        dataStore.edit {
            if (it[CATEGORIES_SEEDED] != true) {
                it[CATEGORIES_SEEDED] = true
                first = true
            }
        }
        return first
    }

    private companion object {
        val CATEGORIES_SEEDED = booleanPreferencesKey("categories_seeded")
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val MAIN_CURRENCY = stringPreferencesKey("main_currency")
        val PERIOD_START_DAY = intPreferencesKey("period_start_day")
        val LAST_USED_CURRENCY = stringPreferencesKey("last_used_currency")
    }
}

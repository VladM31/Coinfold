package com.vm.coinfold.app.feature.settings.db.storages

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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

    /**
     * Returns true if the default categories of [version] have not been applied yet, and records that they
     * are now. A larger [version] makes this return true once more, which is how the defaults are replaced.
     */
    suspend fun claimDefaultCategories(version: Int): Boolean {
        var claimed = false
        dataStore.edit {
            if ((it[DEFAULT_CATEGORIES_VERSION] ?: 0) < version) {
                it[DEFAULT_CATEGORIES_VERSION] = version
                claimed = true
            }
        }
        return claimed
    }

    private companion object {
        val DEFAULT_CATEGORIES_VERSION = intPreferencesKey("default_categories_version")
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val MAIN_CURRENCY = stringPreferencesKey("main_currency")
        val PERIOD_START_DAY = intPreferencesKey("period_start_day")
        val LAST_USED_CURRENCY = stringPreferencesKey("last_used_currency")
    }
}

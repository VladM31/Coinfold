package com.vm.coinfold.app.feature.policy.db.storages

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Remembers which version of the privacy policy the user accepted. Not part of backups. */
class PolicyStorage(private val dataStore: DataStore<Preferences>) {

    /** The accepted version, or [NONE] if the policy was never accepted. */
    val acceptedVersion: Flow<String> = dataStore.data.map { it[ACCEPTED_VERSION] ?: NONE }

    suspend fun accept(version: String) {
        dataStore.edit { it[ACCEPTED_VERSION] = version }
    }

    companion object {
        const val NONE = "none"
        private val ACCEPTED_VERSION = stringPreferencesKey("accepted_policy_version")
    }
}

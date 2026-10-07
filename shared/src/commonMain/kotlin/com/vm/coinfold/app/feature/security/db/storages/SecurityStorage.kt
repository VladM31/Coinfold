package com.vm.coinfold.app.feature.security.db.storages

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Raw stored lock data. The hash and the failed-attempt counter live in the app-private preferences file. */
data class StoredPin(val hash: String, val salt: String)

class SecurityStorage(private val dataStore: DataStore<Preferences>) {

    val pin: Flow<StoredPin?> = dataStore.data.map { prefs ->
        val hash = prefs[PIN_HASH]
        val salt = prefs[PIN_SALT]
        if (hash != null && salt != null) StoredPin(hash, salt) else null
    }

    val biometricEnabled: Flow<Boolean> = dataStore.data.map { it[BIOMETRIC] ?: false }

    suspend fun setPin(hash: String, salt: String) {
        dataStore.edit {
            it[PIN_HASH] = hash
            it[PIN_SALT] = salt
            it[FAILED] = 0
            it[LOCKED_UNTIL] = 0L
        }
    }

    /** Removes the PIN and everything that depends on it. */
    suspend fun clearPin() {
        dataStore.edit {
            it.remove(PIN_HASH)
            it.remove(PIN_SALT)
            it.remove(BIOMETRIC)
            it[FAILED] = 0
            it[LOCKED_UNTIL] = 0L
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[BIOMETRIC] = enabled }
    }

    suspend fun failedAttempts(): Int = dataStore.data.first()[FAILED] ?: 0

    suspend fun lockedUntil(): Long = dataStore.data.first()[LOCKED_UNTIL] ?: 0L

    suspend fun saveAttempts(failed: Int, lockedUntil: Long) {
        dataStore.edit {
            it[FAILED] = failed
            it[LOCKED_UNTIL] = lockedUntil
        }
    }

    private companion object {
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val BIOMETRIC = booleanPreferencesKey("biometric_unlock")
        val FAILED = intPreferencesKey("pin_failed_attempts")
        val LOCKED_UNTIL = longPreferencesKey("pin_locked_until")
    }
}

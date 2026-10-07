package com.vm.coinfold.app.feature.security.domain.repositories

import com.vm.coinfold.app.feature.security.domain.models.PinCheck
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    val isPinSet: Flow<Boolean>
    val biometricEnabled: Flow<Boolean>

    /** Stores a new PIN (as a salted hash) and resets the failed-attempt counter. */
    suspend fun setPin(pin: String)

    /** Turns the lock off completely, including biometric unlock. */
    suspend fun clearPin()

    suspend fun setBiometricEnabled(enabled: Boolean)

    /** UTC epoch millis until which PIN entry is blocked after too many wrong tries; 0 or in the past = not blocked. */
    suspend fun lockedUntil(): Long

    /** Checks [pin] and keeps count of wrong guesses; blocks entry for a while after repeated failures. */
    suspend fun verifyPin(pin: String, nowMillis: Long): PinCheck
}

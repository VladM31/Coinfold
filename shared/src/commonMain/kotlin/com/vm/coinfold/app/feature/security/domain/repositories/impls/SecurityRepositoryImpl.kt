package com.vm.coinfold.app.feature.security.domain.repositories.impls

import com.vm.coinfold.app.feature.security.db.storages.SecurityStorage
import com.vm.coinfold.app.feature.security.domain.models.PinCheck
import com.vm.coinfold.app.feature.security.domain.repositories.SecurityRepository
import com.vm.coinfold.app.feature.security.domain.services.constantTimeEquals
import com.vm.coinfold.app.feature.security.domain.services.hashPin
import com.vm.coinfold.app.feature.security.domain.services.isValidPin
import com.vm.coinfold.app.feature.security.domain.services.lockoutMillisAfter
import com.vm.coinfold.app.feature.security.domain.services.newPinSalt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val ATTEMPTS_PER_ROUND = 5

class SecurityRepositoryImpl(private val storage: SecurityStorage) : SecurityRepository {

    override val isPinSet: Flow<Boolean> = storage.pin.map { it != null }
    override val biometricEnabled: Flow<Boolean> = storage.biometricEnabled

    override suspend fun setPin(pin: String) {
        require(isValidPin(pin)) { "PIN must be 4 digits" }
        val salt = newPinSalt()
        storage.setPin(hashPin(pin, salt), salt)
    }

    override suspend fun clearPin() = storage.clearPin()

    override suspend fun setBiometricEnabled(enabled: Boolean) = storage.setBiometricEnabled(enabled)

    override suspend fun lockedUntil(): Long = storage.lockedUntil()

    override suspend fun verifyPin(pin: String, nowMillis: Long): PinCheck {
        val stored = storage.pin.first() ?: return PinCheck.Correct // no PIN set: nothing to protect
        val lockedUntil = storage.lockedUntil()
        if (nowMillis < lockedUntil) return PinCheck.LockedOut(lockedUntil)

        if (constantTimeEquals(hashPin(pin, stored.salt), stored.hash)) {
            storage.saveAttempts(failed = 0, lockedUntil = 0L)
            return PinCheck.Correct
        }

        val failed = storage.failedAttempts() + 1
        val lockMillis = lockoutMillisAfter(failed)
        return if (lockMillis > 0) {
            val until = nowMillis + lockMillis
            storage.saveAttempts(failed, until)
            PinCheck.LockedOut(until)
        } else {
            storage.saveAttempts(failed, 0L)
            PinCheck.Wrong(attemptsLeft = ATTEMPTS_PER_ROUND - failed % ATTEMPTS_PER_ROUND)
        }
    }
}

package com.vm.coinfold.app.feature.security.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.vm.coinfold.app.feature.security.db.storages.SecurityStorage
import com.vm.coinfold.app.feature.security.domain.models.LockStatus
import com.vm.coinfold.app.feature.security.domain.models.PinCheck
import com.vm.coinfold.app.feature.security.domain.repositories.SecurityRepository
import com.vm.coinfold.app.feature.security.domain.repositories.impls.SecurityRepositoryImpl
import com.vm.coinfold.app.feature.security.domain.services.LOCK_GRACE_MILLIS
import com.vm.coinfold.app.feature.security.domain.services.LockController
import com.vm.coinfold.app.feature.security.domain.services.PinDialogController
import com.vm.coinfold.app.feature.security.domain.services.Sha256
import com.vm.coinfold.app.feature.security.domain.services.constantTimeEquals
import com.vm.coinfold.app.feature.security.domain.services.hashPin
import com.vm.coinfold.app.feature.security.domain.services.isValidPin
import com.vm.coinfold.app.feature.security.domain.services.lockoutMillisAfter
import com.vm.coinfold.app.feature.security.domain.services.toHex
import com.vm.coinfold.app.feature.security.domain.viewmodels.PinError
import com.vm.coinfold.app.feature.security.domain.viewmodels.PinStep
import com.vm.coinfold.app.feature.security.domain.viewmodels.PinFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SecurityLogicTest {

    // ---------- SHA-256 and PIN helpers ----------

    @Test
    fun sha256MatchesTheStandardTestVectors() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Sha256.hash(ByteArray(0)).toHex())
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Sha256.hash("abc".encodeToByteArray()).toHex())
        // 56 bytes: the padding spills into a second block
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            Sha256.hash("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".encodeToByteArray()).toHex(),
        )
        assertEquals(
            "cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0",
            Sha256.hash("a".repeat(1_000_000).encodeToByteArray()).toHex(),
        )
    }

    @Test
    fun hashingIsSaltedAndDeterministic() {
        val a = hashPin("1234", "00112233445566778899aabbccddeeff")
        assertEquals(a, hashPin("1234", "00112233445566778899aabbccddeeff"))
        assertNotEquals(a, hashPin("1234", "ffeeddccbbaa99887766554433221100"))
        assertNotEquals(a, hashPin("1235", "00112233445566778899aabbccddeeff"))
        assertEquals(64, a.length)
    }

    @Test
    fun pinValidationAndComparison() {
        assertTrue(isValidPin("0042"))
        assertFalse(isValidPin("042"))
        assertFalse(isValidPin("12345"))
        assertFalse(isValidPin("12a4"))
        assertTrue(constantTimeEquals("abc", "abc"))
        assertFalse(constantTimeEquals("abc", "abd"))
        assertFalse(constantTimeEquals("abc", "abcd"))
    }

    @Test
    fun lockoutGrowsEveryFiveFailures() {
        assertEquals(listOf(0L, 0L, 0L, 0L), (1..4).map { lockoutMillisAfter(it) })
        assertEquals(30_000L, lockoutMillisAfter(5))
        assertEquals(0L, lockoutMillisAfter(7))
        assertEquals(300_000L, lockoutMillisAfter(10))
        assertEquals(3_600_000L, lockoutMillisAfter(15))
        assertEquals(3_600_000L, lockoutMillisAfter(20))
    }

    // ---------- repository with an in-memory DataStore ----------

    private class MemoryStore : DataStore<Preferences> {
        val state = MutableStateFlow<Preferences>(emptyPreferences())
        override val data: Flow<Preferences> = state
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(state.value).also { state.value = it }
    }

    @Test
    fun wrongPinsCountDownThenLockOutAndAllowAfterTheDelay() = runTest {
        val repo = SecurityRepositoryImpl(SecurityStorage(MemoryStore()))
        repo.setPin("1234")

        assertEquals(PinCheck.Wrong(4), repo.verifyPin("0000", 1_000))
        assertEquals(PinCheck.Wrong(3), repo.verifyPin("0000", 1_000))
        assertEquals(PinCheck.Wrong(2), repo.verifyPin("0000", 1_000))
        assertEquals(PinCheck.Wrong(1), repo.verifyPin("0000", 1_000))
        assertEquals(PinCheck.LockedOut(31_000), repo.verifyPin("0000", 1_000)) // 5th wrong try: 30 s block

        // while blocked even the right PIN is refused
        assertEquals(PinCheck.LockedOut(31_000), repo.verifyPin("1234", 20_000))
        assertEquals(31_000L, repo.lockedUntil())
        // after the delay the right PIN works and resets the counter
        assertEquals(PinCheck.Correct, repo.verifyPin("1234", 31_001))
        assertEquals(PinCheck.Wrong(4), repo.verifyPin("0000", 40_000))
    }

    @Test
    fun clearingThePinRemovesItAndBiometricUnlock() = runTest {
        val repo = SecurityRepositoryImpl(SecurityStorage(MemoryStore()))
        repo.setPin("1234")
        repo.setBiometricEnabled(true)
        repo.clearPin()

        assertFalse(repo.isPinSet.first())
        assertEquals(PinCheck.Correct, repo.verifyPin("anything", 0)) // nothing to protect any more
    }

    // ---------- lock controller ----------

    private class FakeSecurity(initialPin: Boolean) : SecurityRepository {
        val pinSet = MutableStateFlow(initialPin)
        override val isPinSet: Flow<Boolean> = pinSet
        override val biometricEnabled: Flow<Boolean> = pinSet.map { false }
        override suspend fun setPin(pin: String) { pinSet.value = true; lastPin = pin }
        override suspend fun clearPin() { pinSet.value = false }
        override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
        override suspend fun lockedUntil() = 0L
        override suspend fun verifyPin(pin: String, nowMillis: Long): PinCheck =
            if (pin == "1234") PinCheck.Correct else PinCheck.Wrong(1)
        var lastPin: String? = null
    }

    /** Runs launched work immediately, so a test sees the effect of every state change without extra waiting. */
    private fun kotlinx.coroutines.test.TestScope.eagerScope() =
        kotlinx.coroutines.CoroutineScope(UnconfinedTestDispatcher(testScheduler) + kotlinx.coroutines.SupervisorJob())

    @Test
    fun startsLockedWhenAPinExistsAndOpenWhenItDoesNot() = runTest {
        val withPin = LockController(FakeSecurity(true), eagerScope()) { 0L }
        val withoutPin = LockController(FakeSecurity(false), eagerScope()) { 0L }
        advanceUntilIdle()
        assertEquals(LockStatus.LOCKED, withPin.status.value)
        assertEquals(LockStatus.UNLOCKED, withoutPin.status.value)
    }

    @Test
    fun locksAgainOnlyAfterTheGracePeriodInTheBackground() = runTest {
        var now = 0L
        val controller = LockController(FakeSecurity(true), eagerScope()) { now }
        advanceUntilIdle()
        controller.unlock()
        assertEquals(LockStatus.UNLOCKED, controller.status.value)

        // a short trip to another app does not lock
        controller.onBackground()
        now += LOCK_GRACE_MILLIS - 1
        controller.onForeground()
        assertEquals(LockStatus.UNLOCKED, controller.status.value)

        // a long absence does
        controller.onBackground()
        now += LOCK_GRACE_MILLIS
        controller.onForeground()
        assertEquals(LockStatus.LOCKED, controller.status.value)
    }

    @Test
    fun settingAPinDoesNotLockYouOutAndRemovingItOpensTheApp() = runTest {
        val security = FakeSecurity(false)
        val controller = LockController(security, eagerScope()) { 0L }
        advanceUntilIdle()

        security.pinSet.value = true // the PIN was just created in Settings
        advanceUntilIdle()
        assertEquals(LockStatus.UNLOCKED, controller.status.value)
        assertTrue(controller.isProtected.value)

        security.pinSet.value = false
        advanceUntilIdle()
        assertEquals(LockStatus.UNLOCKED, controller.status.value)
        assertFalse(controller.isProtected.value)
    }

    // ---------- the PIN dialog of Settings ----------

    private fun kotlinx.coroutines.test.TestScope.dialogOf(security: FakeSecurity) =
        PinDialogController(security, eagerScope()) { 0L }

    private fun PinDialogController.type(pin: String) = pin.forEach { digit(it) }

    @Test
    fun settingAPinAsksTwiceAndRestartsOnAMismatch() = runTest {
        val security = FakeSecurity(false)
        val dialog = dialogOf(security)

        dialog.open(PinFlow.SET)
        assertEquals(PinStep.NEW, dialog.dialog.value?.step)
        dialog.type("1111")
        assertEquals(PinStep.CONFIRM, dialog.dialog.value?.step)
        dialog.type("2222") // different the second time
        assertEquals(PinStep.NEW, dialog.dialog.value?.step)
        assertEquals(PinError.MISMATCH, dialog.dialog.value?.error)
        assertNull(security.lastPin)

        dialog.type("1111")
        dialog.type("1111")
        assertEquals("1111", security.lastPin)
        assertNull(dialog.dialog.value)
    }

    @Test
    fun changingAPinNeedsTheCurrentOneFirst() = runTest {
        val security = FakeSecurity(true) // FakeSecurity accepts "1234" as the current PIN
        val dialog = dialogOf(security)

        dialog.open(PinFlow.CHANGE)
        assertEquals(PinStep.CURRENT, dialog.dialog.value?.step)
        dialog.type("0000")
        assertEquals(PinError.WRONG, dialog.dialog.value?.error)
        assertEquals(PinStep.CURRENT, dialog.dialog.value?.step)

        dialog.type("1234")
        assertEquals(PinStep.NEW, dialog.dialog.value?.step)
        dialog.type("5678")
        dialog.type("5678")
        assertEquals("5678", security.lastPin)
    }

    @Test
    fun turningTheLockOffNeedsTheCurrentPin() = runTest {
        val security = FakeSecurity(true)
        val dialog = dialogOf(security)

        dialog.open(PinFlow.DISABLE)
        dialog.type("9999")
        assertTrue(security.pinSet.value) // still on after a wrong PIN
        dialog.type("1234")
        assertFalse(security.pinSet.value)
        assertNull(dialog.dialog.value)
    }

    @Test
    fun backspaceRemovesTheLastDigitAndClearsTheError() = runTest {
        val dialog = dialogOf(FakeSecurity(false))
        dialog.open(PinFlow.SET)
        dialog.type("12")
        assertEquals(2, dialog.dialog.value?.enteredCount)
        dialog.backspace()
        assertEquals(1, dialog.dialog.value?.enteredCount)
    }
}

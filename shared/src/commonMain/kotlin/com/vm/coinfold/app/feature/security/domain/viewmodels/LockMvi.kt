package com.vm.coinfold.app.feature.security.domain.viewmodels

data class LockState(
    /** Digits typed so far (shown as dots); at most PIN_LENGTH. */
    val enteredCount: Int = 0,
    /** Set after a wrong PIN until the next key press. */
    val wrongPin: Boolean = false,
    val attemptsLeft: Int? = null,
    /** While in the future, keys are disabled; the UI counts down to it. */
    val lockedUntil: Long = 0L,
    val biometricEnabled: Boolean = false,
)

sealed interface LockIntent {
    data class Digit(val digit: Char) : LockIntent
    data object Backspace : LockIntent

    /** The biometric prompt (run by the UI) accepted the user. */
    data object BiometricSucceeded : LockIntent

    /** The lockout time has passed. */
    data object LockoutElapsed : LockIntent
}

package com.vm.coinfold.app.feature.security.domain.viewmodels

/** The "Security" section in Settings and the PIN dialog it opens. */
data class SecurityState(
    val pinSet: Boolean = false,
    val biometricEnabled: Boolean = false,
    val dialog: PinDialogState? = null,
)

enum class PinFlow { SET, CHANGE, DISABLE }

enum class PinStep { CURRENT, NEW, CONFIRM }

enum class PinError { WRONG, MISMATCH, LOCKED }

/** Which question the PIN dialog asks now; [enteredCount] fills the dots. */
data class PinDialogState(
    val flow: PinFlow,
    val step: PinStep,
    val enteredCount: Int = 0,
    val error: PinError? = null,
    /** For [PinError.LOCKED]: until when entry is blocked. */
    val lockedUntil: Long = 0L,
)

sealed interface SecurityIntent {
    data object EnablePinClicked : SecurityIntent
    data object ChangePinClicked : SecurityIntent
    data object DisablePinClicked : SecurityIntent
    data class Digit(val digit: Char) : SecurityIntent
    data object Backspace : SecurityIntent
    data object DismissDialog : SecurityIntent
    data class BiometricToggled(val enabled: Boolean) : SecurityIntent
}

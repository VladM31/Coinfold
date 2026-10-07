package com.vm.coinfold.app.feature.security.domain.services

import com.vm.coinfold.app.feature.security.domain.models.PinCheck
import com.vm.coinfold.app.feature.security.domain.repositories.SecurityRepository
import com.vm.coinfold.app.feature.security.domain.viewmodels.PinDialogState
import com.vm.coinfold.app.feature.security.domain.viewmodels.PinError
import com.vm.coinfold.app.feature.security.domain.viewmodels.PinFlow
import com.vm.coinfold.app.feature.security.domain.viewmodels.PinStep
import com.vm.coinfold.app.utils.nowMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The state machine behind the PIN dialog of Settings:
 * - SET: new PIN, repeat it; a mismatch starts over;
 * - CHANGE: current PIN first, then like SET;
 * - DISABLE: current PIN, then the lock is removed.
 */
class PinDialogController(
    private val repository: SecurityRepository,
    private val scope: CoroutineScope,
    private val now: () -> Long = ::nowMillis,
) {
    private val _dialog = MutableStateFlow<PinDialogState?>(null)
    val dialog: StateFlow<PinDialogState?> = _dialog.asStateFlow()

    private var entered = ""

    /** The new PIN typed on the NEW step, compared with what is typed on the CONFIRM step. */
    private var firstEntry = ""

    fun open(flow: PinFlow) {
        entered = ""
        firstEntry = ""
        _dialog.value = PinDialogState(flow, if (flow == PinFlow.SET) PinStep.NEW else PinStep.CURRENT)
    }

    fun close() {
        entered = ""
        firstEntry = ""
        _dialog.value = null
    }

    fun backspace() {
        entered = entered.dropLast(1)
        _dialog.value = _dialog.value?.copy(enteredCount = entered.length, error = null)
    }

    fun digit(digit: Char) {
        val current = _dialog.value ?: return
        if (entered.length >= PIN_LENGTH || (current.error == PinError.LOCKED && current.lockedUntil > now())) return
        entered += digit
        _dialog.value = current.copy(enteredCount = entered.length, error = null)
        if (entered.length < PIN_LENGTH) return

        val pin = entered
        scope.launch {
            when (current.step) {
                PinStep.CURRENT -> verifyCurrent(current, pin)
                PinStep.NEW -> {
                    firstEntry = pin
                    entered = ""
                    _dialog.value = current.copy(step = PinStep.CONFIRM, enteredCount = 0, error = null)
                }
                PinStep.CONFIRM -> {
                    if (pin == firstEntry) {
                        repository.setPin(pin)
                        close()
                    } else {
                        // Typed differently the second time: start over from the new PIN.
                        firstEntry = ""
                        entered = ""
                        _dialog.value = current.copy(step = PinStep.NEW, enteredCount = 0, error = PinError.MISMATCH)
                    }
                }
            }
        }
    }

    private suspend fun verifyCurrent(current: PinDialogState, pin: String) {
        entered = ""
        when (val result = repository.verifyPin(pin, now())) {
            PinCheck.Correct -> when (current.flow) {
                PinFlow.DISABLE -> {
                    repository.clearPin()
                    close()
                }
                else -> _dialog.value = current.copy(step = PinStep.NEW, enteredCount = 0, error = null)
            }
            is PinCheck.Wrong -> _dialog.value = current.copy(enteredCount = 0, error = PinError.WRONG)
            is PinCheck.LockedOut ->
                _dialog.value = current.copy(enteredCount = 0, error = PinError.LOCKED, lockedUntil = result.untilMillis)
        }
    }
}

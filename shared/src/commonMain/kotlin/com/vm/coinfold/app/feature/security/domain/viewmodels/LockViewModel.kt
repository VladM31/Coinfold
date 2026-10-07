package com.vm.coinfold.app.feature.security.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vm.coinfold.app.feature.security.domain.models.PinCheck
import com.vm.coinfold.app.feature.security.domain.repositories.SecurityRepository
import com.vm.coinfold.app.feature.security.domain.services.LockController
import com.vm.coinfold.app.feature.security.domain.services.PIN_LENGTH
import com.vm.coinfold.app.utils.nowMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Behind the lock screen: collects the digits, checks the PIN and unlocks the app. */
class LockViewModel(
    private val repository: SecurityRepository,
    private val controller: LockController,
) : ViewModel() {

    private val _state = MutableStateFlow(LockState())
    val state: StateFlow<LockState> = _state.asStateFlow()

    private var entered = ""

    init {
        viewModelScope.launch {
            val biometric = repository.biometricEnabled.first()
            val until = repository.lockedUntil()
            _state.update { it.copy(biometricEnabled = biometric, lockedUntil = until.takeIf { t -> t > nowMillis() } ?: 0L) }
        }
    }

    fun onIntent(intent: LockIntent) {
        when (intent) {
            is LockIntent.Digit -> onDigit(intent.digit)
            LockIntent.Backspace -> {
                entered = entered.dropLast(1)
                _state.update { it.copy(enteredCount = entered.length, wrongPin = false) }
            }
            LockIntent.BiometricSucceeded -> controller.unlock()
            LockIntent.LockoutElapsed -> _state.update { it.copy(lockedUntil = 0L, wrongPin = false, attemptsLeft = null) }
        }
    }

    private fun onDigit(digit: Char) {
        if (_state.value.lockedUntil > nowMillis() || entered.length >= PIN_LENGTH) return
        entered += digit
        _state.update { it.copy(enteredCount = entered.length, wrongPin = false) }
        if (entered.length < PIN_LENGTH) return

        val pin = entered
        viewModelScope.launch {
            when (val result = repository.verifyPin(pin, nowMillis())) {
                PinCheck.Correct -> {
                    entered = ""
                    _state.update { LockState(biometricEnabled = it.biometricEnabled) }
                    controller.unlock()
                }
                is PinCheck.Wrong -> {
                    entered = ""
                    _state.update { it.copy(enteredCount = 0, wrongPin = true, attemptsLeft = result.attemptsLeft) }
                }
                is PinCheck.LockedOut -> {
                    entered = ""
                    _state.update { it.copy(enteredCount = 0, wrongPin = true, attemptsLeft = null, lockedUntil = result.untilMillis) }
                }
            }
        }
    }
}

package com.vm.coinfold.app.feature.security.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vm.coinfold.app.feature.security.domain.repositories.SecurityRepository
import com.vm.coinfold.app.feature.security.domain.services.PinDialogController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SecurityViewModel(private val repository: SecurityRepository) : ViewModel() {

    private val pinDialog = PinDialogController(repository, viewModelScope)

    val state: StateFlow<SecurityState> = combine(
        repository.isPinSet,
        repository.biometricEnabled,
        pinDialog.dialog,
    ) { pinSet, biometric, dialog ->
        SecurityState(pinSet = pinSet, biometricEnabled = biometric, dialog = dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SecurityState())

    fun onIntent(intent: SecurityIntent) {
        when (intent) {
            SecurityIntent.EnablePinClicked -> pinDialog.open(PinFlow.SET)
            SecurityIntent.ChangePinClicked -> pinDialog.open(PinFlow.CHANGE)
            SecurityIntent.DisablePinClicked -> pinDialog.open(PinFlow.DISABLE)
            is SecurityIntent.Digit -> pinDialog.digit(intent.digit)
            SecurityIntent.Backspace -> pinDialog.backspace()
            SecurityIntent.DismissDialog -> pinDialog.close()
            is SecurityIntent.BiometricToggled ->
                viewModelScope.launch { repository.setBiometricEnabled(intent.enabled) }
        }
    }
}

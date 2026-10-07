package com.vm.coinfold.app.feature.security.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.biometric_cancel
import coinfold.shared.generated.resources.biometric_subtitle
import coinfold.shared.generated.resources.biometric_title
import coinfold.shared.generated.resources.lock_locked
import coinfold.shared.generated.resources.lock_title
import coinfold.shared.generated.resources.lock_wrong
import com.vm.coinfold.app.feature.security.domain.services.PIN_LENGTH
import com.vm.coinfold.app.feature.security.domain.viewmodels.LockIntent
import com.vm.coinfold.app.feature.security.domain.viewmodels.LockViewModel
import com.vm.coinfold.app.shared.platform.rememberBiometricAuthenticator
import com.vm.coinfold.app.shared.ui.components.PinPad
import com.vm.coinfold.app.utils.nowMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Covers the whole app until the PIN (or biometrics) is accepted. It is drawn on top of the app instead of
 * replacing it, so the screen the user was on is still there after unlocking.
 */
@Composable
fun LockScreen(viewModel: LockViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val biometrics = rememberBiometricAuthenticator()

    val title = stringResource(Res.string.biometric_title)
    val subtitle = stringResource(Res.string.biometric_subtitle)
    val cancel = stringResource(Res.string.biometric_cancel)
    val scope = rememberCoroutineScope()
    val canUseBiometrics = state.biometricEnabled && biometrics.isAvailable

    // Offer the biometric prompt right away, like most apps; the PIN pad stays available underneath.
    LaunchedEffect(canUseBiometrics) {
        if (canUseBiometrics && biometrics.authenticate(title, subtitle, cancel)) {
            viewModel.onIntent(LockIntent.BiometricSucceeded)
        }
    }

    // Counts down a lockout after too many wrong tries.
    var now by remember { mutableStateOf(nowMillis()) }
    LaunchedEffect(state.lockedUntil) {
        if (state.lockedUntil > 0L) {
            while (nowMillis() < state.lockedUntil) {
                now = nowMillis()
                delay(500)
            }
            viewModel.onIntent(LockIntent.LockoutElapsed)
        }
    }
    val lockedFor = ((state.lockedUntil - now) / 1000).coerceAtLeast(0)
    val isLocked = state.lockedUntil > now

    val message = when {
        isLocked -> stringResource(Res.string.lock_locked, formatSeconds(lockedFor))
        state.wrongPin && state.attemptsLeft != null -> stringResource(Res.string.lock_wrong, state.attemptsLeft.toString())
        else -> null
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().systemBarsPadding(), contentAlignment = Alignment.Center) {
            PinPad(
                title = stringResource(Res.string.lock_title),
                filled = state.enteredCount,
                length = PIN_LENGTH,
                onDigit = { viewModel.onIntent(LockIntent.Digit(it)) },
                onBackspace = { viewModel.onIntent(LockIntent.Backspace) },
                message = message,
                isError = isLocked || state.wrongPin,
                enabled = !isLocked,
                extraKey = if (canUseBiometrics) {
                    {
                        // The prompt can be opened again by hand after it was cancelled.
                        IconButton(onClick = {
                            scope.launch {
                                if (biometrics.authenticate(title, subtitle, cancel)) {
                                    viewModel.onIntent(LockIntent.BiometricSucceeded)
                                }
                            }
                        }) { Icon(Icons.Outlined.Fingerprint, contentDescription = null) }
                    }
                } else {
                    null
                },
            )
        }
    }
}

/** `1:05` for 65 seconds. */
private fun formatSeconds(total: Long): String {
    val minutes = total / 60
    val seconds = total % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

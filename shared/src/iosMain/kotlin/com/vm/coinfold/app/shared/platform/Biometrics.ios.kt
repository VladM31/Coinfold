package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator = remember { IosBiometricAuthenticator() }

@OptIn(ExperimentalForeignApi::class)
private class IosBiometricAuthenticator : BiometricAuthenticator {

    override val isAvailable: Boolean
        get() = LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error = null)

    override suspend fun authenticate(title: String, subtitle: String, cancelText: String): Boolean {
        val context = LAContext()
        context.localizedCancelTitle = cancelText
        return suspendCancellableCoroutine { continuation ->
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = subtitle,
            ) { success, _ ->
                if (continuation.isActive) continuation.resume(success)
            }
        }
    }
}

// iOS hides the app in the app switcher with the privacy cover that App draws while the app is not active.
@Composable
actual fun SecureWindowEffect(enabled: Boolean) = Unit

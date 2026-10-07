package com.vm.coinfold.app.shared.platform

import androidx.compose.runtime.Composable

/** Fingerprint / face unlock of the device. */
interface BiometricAuthenticator {
    /** True if the device has biometrics enrolled and the app may use them. */
    val isAvailable: Boolean

    /**
     * Shows the system biometric prompt. Returns true only if the user was recognised; cancelling, failing
     * too often or having no biometrics all return false.
     */
    suspend fun authenticate(title: String, subtitle: String, cancelText: String): Boolean
}

@Composable
expect fun rememberBiometricAuthenticator(): BiometricAuthenticator

/**
 * While [enabled], hides the app content in the recent-apps switcher and blocks screenshots (Android
 * FLAG_SECURE). On iOS the privacy cover drawn by the app does this job, so nothing is needed there.
 */
@Composable
expect fun SecureWindowEffect(enabled: Boolean)

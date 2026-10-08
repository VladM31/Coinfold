package com.vm.coinfold.app.feature.security.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.security_biometric
import coinfold.shared.generated.resources.security_biometric_unavailable
import coinfold.shared.generated.resources.security_change_pin
import coinfold.shared.generated.resources.security_hint
import coinfold.shared.generated.resources.security_pin_on
import coinfold.shared.generated.resources.security_set_pin
import coinfold.shared.generated.resources.security_title
import coinfold.shared.generated.resources.security_turn_off
import com.vm.coinfold.app.feature.security.domain.viewmodels.SecurityIntent
import com.vm.coinfold.app.feature.security.domain.viewmodels.SecurityState
import org.jetbrains.compose.resources.stringResource

/** The "Security" block of Settings: PIN lock and, where the device supports it, biometric unlock. */
@Composable
fun SecuritySection(
    state: SecurityState,
    biometricAvailable: Boolean,
    onIntent: (SecurityIntent) -> Unit,
    /** Turning it on first asks the device to recognise the user once, to prove it works. */
    onBiometricToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.security_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(Res.string.security_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!state.pinSet) {
            Button(onClick = { onIntent(SecurityIntent.EnablePinClicked) }) {
                Text(stringResource(Res.string.security_set_pin))
            }
        } else {
            Text(stringResource(Res.string.security_pin_on), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onIntent(SecurityIntent.ChangePinClicked) }) {
                    Text(stringResource(Res.string.security_change_pin))
                }
                OutlinedButton(onClick = { onIntent(SecurityIntent.DisablePinClicked) }) {
                    Text(stringResource(Res.string.security_turn_off), color = MaterialTheme.colorScheme.error)
                }
            }
            // Always shown; it is only usable when the device has a fingerprint or face enrolled.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.security_biometric), style = MaterialTheme.typography.bodyMedium)
                    if (!biometricAvailable) {
                        Text(
                            stringResource(Res.string.security_biometric_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(
                    checked = state.biometricEnabled && biometricAvailable,
                    enabled = biometricAvailable,
                    onCheckedChange = onBiometricToggle,
                )
            }
        }
    }
}

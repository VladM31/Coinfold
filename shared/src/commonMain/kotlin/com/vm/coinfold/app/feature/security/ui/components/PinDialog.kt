package com.vm.coinfold.app.feature.security.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_cancel
import coinfold.shared.generated.resources.pin_confirm
import coinfold.shared.generated.resources.pin_current
import coinfold.shared.generated.resources.pin_locked
import coinfold.shared.generated.resources.pin_mismatch
import coinfold.shared.generated.resources.pin_new
import coinfold.shared.generated.resources.pin_wrong
import com.vm.coinfold.app.feature.security.domain.services.PIN_LENGTH
import com.vm.coinfold.app.feature.security.domain.viewmodels.PinDialogState
import com.vm.coinfold.app.feature.security.domain.viewmodels.PinError
import com.vm.coinfold.app.feature.security.domain.viewmodels.PinStep
import com.vm.coinfold.app.shared.ui.components.PinPad
import org.jetbrains.compose.resources.stringResource

/** The PIN questions of Settings: current PIN, new PIN, repeat the new PIN, one step at a time. */
@Composable
fun PinDialog(
    state: PinDialogState,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onDismiss: () -> Unit,
) {
    val title = when (state.step) {
        PinStep.CURRENT -> stringResource(Res.string.pin_current)
        PinStep.NEW -> stringResource(Res.string.pin_new)
        PinStep.CONFIRM -> stringResource(Res.string.pin_confirm)
    }
    val message = when (state.error) {
        PinError.WRONG -> stringResource(Res.string.pin_wrong)
        PinError.MISMATCH -> stringResource(Res.string.pin_mismatch)
        PinError.LOCKED -> stringResource(Res.string.pin_locked)
        null -> null
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PinPad(
                    title = title,
                    filled = state.enteredCount,
                    length = PIN_LENGTH,
                    onDigit = onDigit,
                    onBackspace = onBackspace,
                    message = message,
                    isError = state.error != null,
                )
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) } },
    )
}

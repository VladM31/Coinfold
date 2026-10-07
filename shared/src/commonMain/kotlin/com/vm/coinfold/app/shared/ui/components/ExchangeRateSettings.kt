package com.vm.coinfold.app.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_cancel
import coinfold.shared.generated.resources.action_save
import coinfold.shared.generated.resources.rate_bank
import coinfold.shared.generated.resources.rate_button_bank
import coinfold.shared.generated.resources.rate_button_custom
import coinfold.shared.generated.resources.rate_dialog_hint
import coinfold.shared.generated.resources.rate_dialog_title
import coinfold.shared.generated.resources.rate_label
import coinfold.shared.generated.resources.rate_use_bank
import com.vm.coinfold.app.utils.parseRate
import org.jetbrains.compose.resources.stringResource

/**
 * A small, quiet button for the exchange rate of one operation. It reads "Exchange rate: bank" until the
 * user typed their own rate, then shows that rate. It opens [ExchangeRateDialog].
 */
@Composable
fun RateSettingsButton(customRate: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(
            text = " " + if (customRate.isBlank()) {
                stringResource(Res.string.rate_button_bank)
            } else {
                stringResource(Res.string.rate_button_custom, customRate)
            },
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * Lets the user type the rate used for this operation only: 1 [fromCode] = … [toCode]. An empty value means
 * "use the bank rate"; [bankRate] is shown as a reference when known.
 */
@Composable
fun ExchangeRateDialog(
    fromCode: String,
    toCode: String,
    bankRate: String?,
    current: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(current) }
    val valid = text.isBlank() || parseRate(text) != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.rate_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(Res.string.rate_dialog_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(Res.string.rate_label, fromCode, toCode)) },
                    singleLine = true,
                    isError = !valid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (bankRate != null) stringResource(Res.string.rate_bank, bankRate) else "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { text = "" }) { Text(stringResource(Res.string.rate_use_bank)) }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onConfirm(text.trim()) }) {
                Text(stringResource(Res.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) } },
    )
}

package com.vm.coinfold.app.feature.accounts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.accounts_currency_locked
import coinfold.shared.generated.resources.accounts_edit_title
import coinfold.shared.generated.resources.accounts_initial_balance
import coinfold.shared.generated.resources.accounts_new_title
import coinfold.shared.generated.resources.action_delete
import coinfold.shared.generated.resources.action_save
import coinfold.shared.generated.resources.field_currency
import coinfold.shared.generated.resources.field_name
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.ui.components.CategoryBadge
import com.vm.coinfold.app.shared.ui.components.CurrencySelector
import com.vm.coinfold.app.shared.ui.components.IconColorPicker
import com.vm.coinfold.app.utils.parseAmount
import org.jetbrains.compose.resources.stringResource

/**
 * Create ([item] == null) or edit an account (opened by a long press on it). Name, currency and initial
 * balance are fields; icon and color are two tabs under the actions, like in the category form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountFormSheet(
    item: AccountWithBalance?,
    onSave: (name: String, currency: Currency, initialBalance: BigDecimal, color: Long, icon: String) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val account = item?.account
    var name by remember { mutableStateOf(account?.name.orEmpty()) }
    var currency by remember { mutableStateOf(account?.currency ?: Currency.UAH) }
    var initial by remember {
        mutableStateOf(account?.initialBalance?.toBigDecimal()?.toPlainString() ?: "0")
    }
    var color by remember { mutableStateOf(account?.color ?: DEFAULT_ACCOUNT_COLOR) }
    var icon by remember { mutableStateOf(account?.icon ?: DEFAULT_ACCOUNT_ICON) }
    val currencyLocked = (item?.transactionCount ?: 0) > 0

    val initialValue = parseAmount(initial, allowNegative = true)
    val canSave = name.isNotBlank() && initialValue != null

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(if (item == null) Res.string.accounts_new_title else Res.string.accounts_edit_title),
                style = MaterialTheme.typography.titleLarge,
            )
            CategoryBadge(icon, Color(color), size = 64, modifier = Modifier.align(Alignment.CenterHorizontally))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.field_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(Res.string.field_currency), style = MaterialTheme.typography.labelLarge)
            CurrencySelector(currency, onSelected = { currency = it }, enabled = !currencyLocked)
            if (currencyLocked) {
                Text(
                    stringResource(Res.string.accounts_currency_locked),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedTextField(
                value = initial,
                onValueChange = { initial = it },
                label = { Text(stringResource(Res.string.accounts_initial_balance)) },
                singleLine = true,
                isError = initialValue == null,
                // Plain text keyboard so a leading minus can be typed.
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
            )
            // Actions sit above the long icon/color lists so they are always reachable without scrolling.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Spacer(Modifier)
                }
                Button(
                    enabled = canSave,
                    onClick = { initialValue?.let { onSave(name, currency, it, color, icon) } },
                ) { Text(stringResource(Res.string.action_save)) }
            }
            IconColorPicker(
                icon = icon,
                color = color,
                onIconChange = { icon = it },
                onColorChange = { color = it },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

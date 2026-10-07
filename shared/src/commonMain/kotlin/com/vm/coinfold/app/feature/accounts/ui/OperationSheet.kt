package com.vm.coinfold.app.feature.accounts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import coinfold.shared.generated.resources.accounts_source
import coinfold.shared.generated.resources.accounts_source_custom
import coinfold.shared.generated.resources.accounts_top_up_title
import coinfold.shared.generated.resources.accounts_withdraw_title
import coinfold.shared.generated.resources.action_save
import coinfold.shared.generated.resources.field_amount
import coinfold.shared.generated.resources.field_note
import coinfold.shared.generated.resources.source_debt_return
import coinfold.shared.generated.resources.source_gift
import coinfold.shared.generated.resources.source_other
import coinfold.shared.generated.resources.source_salary
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.main.AccountWithBalance
import com.vm.coinfold.app.feature.transactions.main.IncomeSource
import com.vm.coinfold.app.shared.domain.TransactionType
import com.vm.coinfold.app.shared.ui.DateField
import com.vm.coinfold.app.utils.parseAmount
import com.vm.coinfold.app.utils.today
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private fun IncomeSource.Preset.label(): StringResource = when (this) {
    IncomeSource.Preset.SALARY -> Res.string.source_salary
    IncomeSource.Preset.DEBT_RETURN -> Res.string.source_debt_return
    IncomeSource.Preset.GIFT -> Res.string.source_gift
    IncomeSource.Preset.OTHER -> Res.string.source_other
}

/** Top-up (income with a source) or manual withdrawal for one account. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OperationSheet(
    item: AccountWithBalance,
    type: TransactionType,
    customSources: List<String>,
    onSave: (amount: BigDecimal, source: IncomeSource?, note: String, date: LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    var note by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf<IncomeSource>(IncomeSource.Preset.SALARY) }
    var customText by remember { mutableStateOf("") }

    val amountValue = parseAmount(amount)
    val isIncome = type == TransactionType.INCOME
    val canSave = amountValue != null && amountValue > BigDecimal.ZERO

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(
                    if (isIncome) Res.string.accounts_top_up_title else Res.string.accounts_withdraw_title,
                    item.account.name,
                ),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(Res.string.field_amount) + ", " + item.account.currency.code) },
                singleLine = true,
                isError = amount.isNotEmpty() && amountValue == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            DateField(date, onDateChange = { date = it })

            if (isIncome) {
                Text(stringResource(Res.string.accounts_source), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IncomeSource.Preset.entries.forEach { preset ->
                        FilterChip(
                            selected = customText.isBlank() && selectedSource == preset,
                            onClick = { selectedSource = preset; customText = "" },
                            label = { Text(stringResource(preset.label())) },
                        )
                    }
                    customSources.forEach { name ->
                        val source = IncomeSource.Custom(name)
                        FilterChip(
                            selected = customText.isBlank() && selectedSource == source,
                            onClick = { selectedSource = source; customText = "" },
                            label = { Text(name) },
                        )
                    }
                }
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    label = { Text(stringResource(Res.string.accounts_source_custom)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(Res.string.field_note)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                enabled = canSave,
                onClick = {
                    val source = if (isIncome) {
                        customText.trim().takeIf { it.isNotEmpty() }?.let { IncomeSource.Custom(it) } ?: selectedSource
                    } else {
                        null
                    }
                    amountValue?.let { onSave(it, source, note, date) }
                },
                modifier = Modifier.align(Alignment.End),
            ) { Text(stringResource(Res.string.action_save)) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

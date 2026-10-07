package com.vm.coinfold.app.feature.recurring.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_delete
import coinfold.shared.generated.resources.action_save
import coinfold.shared.generated.resources.expense_account
import coinfold.shared.generated.resources.field_amount
import coinfold.shared.generated.resources.field_note
import coinfold.shared.generated.resources.filter_category
import coinfold.shared.generated.resources.recurring_edit_title
import coinfold.shared.generated.resources.recurring_first_date
import coinfold.shared.generated.resources.recurring_new_title
import coinfold.shared.generated.resources.recurring_repeat
import coinfold.shared.generated.resources.type_expense
import coinfold.shared.generated.resources.type_income
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.recurring.domain.models.Frequency
import com.vm.coinfold.app.feature.recurring.domain.models.RecurringPayment
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.shared.ui.components.CategoryIcon
import com.vm.coinfold.app.shared.ui.components.DateField
import com.vm.coinfold.app.shared.ui.components.IncomeSourcePicker
import com.vm.coinfold.app.shared.ui.components.resolveSource
import com.vm.coinfold.app.utils.parseAmount
import com.vm.coinfold.app.utils.today
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/**
 * Create ([payment] == null) or edit a schedule. Amount, note and the actions are at the top; the amount is
 * always in the currency of the chosen account.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecurringFormSheet(
    payment: RecurringPayment?,
    accounts: List<AccountWithBalance>,
    categories: List<Category>,
    customSources: List<String>,
    onSave: (
        type: TransactionType,
        accountId: Long,
        categoryId: Long?,
        source: IncomeSource?,
        amount: BigDecimal,
        note: String,
        frequency: Frequency,
        firstDate: LocalDate,
    ) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var type by remember { mutableStateOf(payment?.type ?: TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf(payment?.amount?.toBigDecimal()?.toPlainString() ?: "") }
    var note by remember { mutableStateOf(payment?.note.orEmpty()) }
    var accountId by remember { mutableStateOf(payment?.accountId ?: accounts.firstOrNull()?.account?.id) }
    var categoryId by remember { mutableStateOf(payment?.category?.id) }
    var selectedSource by remember { mutableStateOf(payment?.source ?: IncomeSource.Preset.SALARY) }
    var customText by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(payment?.frequency ?: Frequency.MONTHLY) }
    var firstDate by remember { mutableStateOf(payment?.nextDate ?: today()) }

    val isIncome = type == TransactionType.INCOME
    val currencyCode = accounts.firstOrNull { it.account.id == accountId }?.account?.currency?.code
        ?: payment?.amount?.currency?.code.orEmpty()
    val amountValue = parseAmount(amount)
    val canSave = amountValue != null && amountValue > BigDecimal.ZERO && accountId != null

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(if (payment == null) Res.string.recurring_new_title else Res.string.recurring_edit_title),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(Res.string.field_amount) + ", " + currencyCode) },
                singleLine = true,
                isError = amount.isNotEmpty() && amountValue == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(Res.string.field_note)) },
                modifier = Modifier.fillMaxWidth(),
            )
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
                    onClick = {
                        val id = accountId
                        if (amountValue != null && id != null) {
                            onSave(
                                type,
                                id,
                                categoryId.takeIf { !isIncome },
                                if (isIncome) resolveSource(selectedSource, customText) else null,
                                amountValue,
                                note,
                                frequency,
                                firstDate,
                            )
                        }
                    },
                ) { Text(stringResource(Res.string.action_save)) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isIncome,
                    onClick = { type = TransactionType.EXPENSE },
                    label = { Text(stringResource(Res.string.type_expense)) },
                )
                FilterChip(
                    selected = isIncome,
                    onClick = { type = TransactionType.INCOME },
                    label = { Text(stringResource(Res.string.type_income)) },
                )
            }

            Text(stringResource(Res.string.expense_account), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // A schedule on an archived account keeps showing that account.
                val options = accounts.map { it.account.id to "${it.account.name} · ${it.account.currency.code}" }
                    .let { list ->
                        if (payment != null && list.none { it.first == payment.accountId }) {
                            list + (payment.accountId to payment.accountName)
                        } else {
                            list
                        }
                    }
                options.forEach { (id, label) ->
                    FilterChip(selected = accountId == id, onClick = { accountId = id }, label = { Text(label) })
                }
            }

            if (isIncome) {
                IncomeSourcePicker(
                    selected = selectedSource,
                    customText = customText,
                    customSources = customSources,
                    onSelected = { selectedSource = it },
                    onCustomTextChange = { customText = it },
                )
            } else {
                Text(stringResource(Res.string.filter_category), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = categoryId == category.id,
                            // Tapping the selected category again clears it (a plain withdrawal).
                            onClick = { categoryId = if (categoryId == category.id) null else category.id },
                            label = { Text(category.name) },
                            leadingIcon = {
                                CategoryIcon(category.icon, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18)
                            },
                        )
                    }
                }
            }

            Text(stringResource(Res.string.recurring_repeat), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Frequency.entries.forEach { option ->
                    FilterChip(
                        selected = frequency == option,
                        onClick = { frequency = option },
                        label = { Text(stringResource(option.label())) },
                    )
                }
            }
            Text(stringResource(Res.string.recurring_first_date), style = MaterialTheme.typography.labelLarge)
            DateField(firstDate, onDateChange = { firstDate = it })
            Spacer(Modifier.height(16.dp))
        }
    }
}

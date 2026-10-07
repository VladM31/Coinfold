package com.vm.coinfold.app.feature.transactions.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.action_delete
import coinfold.shared.generated.resources.action_save
import coinfold.shared.generated.resources.expense_account
import coinfold.shared.generated.resources.field_amount
import coinfold.shared.generated.resources.field_currency
import coinfold.shared.generated.resources.field_note
import coinfold.shared.generated.resources.filter_category
import coinfold.shared.generated.resources.transaction_edit_title
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.feature.transactions.domain.models.TransactionItem
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.IncomeSource
import com.vm.coinfold.app.shared.domain.models.TransactionType
import com.vm.coinfold.app.shared.ui.components.CategoryIcon
import com.vm.coinfold.app.shared.ui.components.CurrencySelector
import com.vm.coinfold.app.shared.ui.components.DateField
import com.vm.coinfold.app.shared.ui.components.IncomeSourcePicker
import com.vm.coinfold.app.shared.ui.components.resolveSource
import com.vm.coinfold.app.utils.parseAmount
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

private data class AccountOption(val id: Long, val name: String, val currency: Currency)

/**
 * Edit any transaction. Top-ups and manual withdrawals are always in the currency of the account;
 * categorized expenses also let you choose the operation currency and the category.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalTime::class)
@Composable
fun EditTransactionSheet(
    item: TransactionItem,
    accounts: List<AccountWithBalance>,
    categories: List<Category>,
    customSources: List<String>,
    onSave: (
        amount: BigDecimal,
        currency: Currency,
        accountId: Long,
        categoryId: Long?,
        source: IncomeSource?,
        note: String,
        date: LocalDate,
    ) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isIncome = item.type == TransactionType.INCOME
    val isCategorized = item.category != null

    // The original account may be archived and therefore missing from the active list.
    val accountOptions = remember(accounts, item) {
        val options = accounts.map { AccountOption(it.account.id, it.account.name, it.account.currency) }
        if (options.any { it.id == item.accountId }) options
        else options + AccountOption(item.accountId, item.accountName, item.accountCurrency)
    }

    var amount by remember { mutableStateOf(item.amount.toBigDecimal().toPlainString()) }
    var currency by remember { mutableStateOf(item.amount.currency) }
    var accountId by remember { mutableStateOf(item.accountId) }
    var categoryId by remember { mutableStateOf(item.category?.id) }
    var note by remember { mutableStateOf(item.note) }
    var date by remember {
        mutableStateOf(
            Instant.fromEpochMilliseconds(item.dateTime).toLocalDateTime(TimeZone.currentSystemDefault()).date,
        )
    }
    var selectedSource by remember { mutableStateOf(item.source ?: IncomeSource.Preset.OTHER) }
    var customText by remember { mutableStateOf("") }

    val accountCurrency = accountOptions.first { it.id == accountId }.currency
    val shownCurrency = if (isCategorized) currency else accountCurrency
    val amountValue = parseAmount(amount)
    val canSave = amountValue != null && amountValue > BigDecimal.ZERO

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(Res.string.transaction_edit_title), style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(Res.string.field_amount) + ", " + shownCurrency.code) },
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
            // Amount, note and the actions are at the top; everything else follows below.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onDelete) {
                    Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
                Button(
                    enabled = canSave,
                    onClick = {
                        amountValue?.let {
                            onSave(
                                it,
                                shownCurrency,
                                accountId,
                                categoryId.takeIf { isCategorized },
                                if (isIncome) resolveSource(selectedSource, customText) else null,
                                note,
                                date,
                            )
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                ) { Text(stringResource(Res.string.action_save)) }
            }
            if (isCategorized) {
                Text(stringResource(Res.string.field_currency), style = MaterialTheme.typography.labelLarge)
                CurrencySelector(currency, onSelected = { currency = it })

                Text(stringResource(Res.string.filter_category), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Keep the current category selectable even if it was archived since.
                    val options = categories.map { Triple(it.id, it.icon, it.name) }.let { list ->
                        val current = item.category
                        if (current != null && list.none { it.first == current.id }) {
                            list + Triple(current.id, current.icon, current.name)
                        } else {
                            list
                        }
                    }
                    options.forEach { (id, icon, name) ->
                        FilterChip(
                            selected = categoryId == id,
                            onClick = { categoryId = id },
                            label = { Text(name) },
                            leadingIcon = {
                                CategoryIcon(icon, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18)
                            },
                        )
                    }
                }
            }

            Text(stringResource(Res.string.expense_account), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                accountOptions.forEach { option ->
                    FilterChip(
                        selected = accountId == option.id,
                        onClick = { accountId = option.id },
                        label = { Text("${option.name} · ${option.currency.code}") },
                    )
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
            }

            DateField(date, onDateChange = { date = it })

            Spacer(Modifier.height(16.dp))
        }
    }
}

package com.vm.coinfold.app.feature.expenses.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coinfold.shared.generated.resources.Res
import coinfold.shared.generated.resources.date_today
import coinfold.shared.generated.resources.expense_amount_label
import coinfold.shared.generated.resources.expense_no_accounts
import coinfold.shared.generated.resources.expense_notes
import coinfold.shared.generated.resources.expense_withdrawal
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.vm.coinfold.app.feature.accounts.domain.models.AccountWithBalance
import com.vm.coinfold.app.feature.currency.domain.models.RateTable
import com.vm.coinfold.app.feature.expenses.domain.models.Category
import com.vm.coinfold.app.shared.domain.models.Currency
import com.vm.coinfold.app.shared.domain.models.Money
import com.vm.coinfold.app.shared.ui.components.DatePickerDialogHost
import com.vm.coinfold.app.shared.ui.components.LocalAppLanguage
import com.vm.coinfold.app.utils.Calculator
import com.vm.coinfold.app.utils.CalculatorInput
import com.vm.coinfold.app.utils.OP_DIVIDE
import com.vm.coinfold.app.utils.OP_MINUS
import com.vm.coinfold.app.utils.OP_PLUS
import com.vm.coinfold.app.utils.OP_TIMES
import com.vm.coinfold.app.utils.format
import com.vm.coinfold.app.utils.today
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

private const val OPERATORS = "$OP_PLUS$OP_MINUS$OP_TIMES$OP_DIVIDE"

/**
 * Bottom sheet opened from a category circle. Only what is needed to record an expense: the amount
 * (with a calculator), its currency, the account and an optional note; the date is one key away.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    category: Category,
    accounts: List<AccountWithBalance>,
    rates: RateTable,
    initialCurrency: Currency,
    onSave: (amount: BigDecimal, currency: Currency, accountId: Long, note: String, date: LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val language = LocalAppLanguage.current
    var expression by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(initialCurrency) }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id) }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    var datePickerOpen by remember { mutableStateOf(false) }

    val account = accounts.firstOrNull { it.account.id == accountId }
    val categoryColor = Color(category.color)

    // The typed expression is evaluated live; it is valid only if it gives a positive amount.
    val result = Calculator.evaluate(expression)
    val entered = result?.let { Money.of(it, currency) }?.takeIf { it.minorUnits > 0 }
    // What leaves the account: the same amount, or the converted one when the currencies differ.
    val fromAccount = if (entered != null && account != null) {
        rates.convert(entered, account.account.currency)?.money
    } else {
        null
    }
    val canSave = entered != null && fromAccount != null && account != null
    val hasOperator = expression.drop(1).any { it in OPERATORS }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
    ) {
        Column(Modifier.navigationBarsPadding().verticalScroll(rememberScrollState())) {
            if (accounts.isEmpty()) {
                Text(
                    stringResource(Res.string.expense_no_accounts),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp),
                )
                return@Column
            }

            ExpenseSheetHeader(
                accounts = accounts,
                selectedAccount = account,
                category = category,
                onAccountSelected = { accountId = it.account.id },
            )

            Column(
                Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AmountCard(
                        label = stringResource(Res.string.expense_withdrawal),
                        value = fromAccount?.format(language) ?: "—",
                        subtitle = null,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        content = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    AmountCard(
                        label = stringResource(Res.string.expense_amount_label),
                        value = "${expression.ifEmpty { "0" }} ${currency.code}",
                        subtitle = if (hasOperator && entered != null) "= ${entered.format(language)}" else null,
                        container = categoryColor.copy(alpha = 0.16f),
                        content = categoryColor,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text(stringResource(Res.string.expense_notes)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                ExpenseKeypad(
                    currency = currency,
                    accent = categoryColor,
                    confirmEnabled = canSave,
                    onKey = { expression = CalculatorInput.append(expression, it) },
                    onBackspace = { expression = CalculatorInput.backspace(expression) },
                    onCurrencyClick = { currency = Currency.entries[(currency.ordinal + 1) % Currency.entries.size] },
                    onDateClick = { datePickerOpen = true },
                    onConfirm = {
                        val id = accountId
                        if (result != null && id != null) onSave(result, currency, id, note, date)
                    },
                )

                Text(
                    text = if (date == today()) {
                        stringResource(Res.string.date_today) + ", " + date.format()
                    } else {
                        date.format()
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }

    if (datePickerOpen) {
        DatePickerDialogHost(date, onDateChange = { date = it }, onDismiss = { datePickerOpen = false })
    }
}

@Composable
private fun AmountCard(
    label: String,
    value: String,
    subtitle: String?,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = container) {
        Column(
            Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = content)
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = content)
            }
        }
    }
}
